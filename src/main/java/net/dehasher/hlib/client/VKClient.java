package net.dehasher.hlib.client;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.Tools;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Getter(AccessLevel.PRIVATE)
public final class VKClient implements AutoCloseable {

	private final String token;
	@Getter
	private final String version;
	private final Object lock = new Object();
	private final Set<HttpRequestBase> requests = ConcurrentHashMap.newKeySet();
	private final CloseableHttpClient client;
	private final ScheduledThreadPoolExecutor deadlines;
	@Setter(AccessLevel.PRIVATE)
	private boolean closed;

	public VKClient(String token) {
		this(token, "${api_version_vk}");
	}

	public VKClient(String token, String version) {
		if (token == null || token.isBlank()) throw new IllegalArgumentException("Не указан токен VK.");
		if (version == null || !version.matches("[0-9]+\\.[0-9]+")) throw new IllegalArgumentException("Некорректная версия API VK.");
		this.token = token;
		this.version = version;
		this.client = HttpClientBuilder.create()
				.setMaxConnTotal(4)
				.setMaxConnPerRoute(4)
				.setDefaultRequestConfig(RequestConfig.custom()
						.setConnectTimeout(10000)
						.setConnectionRequestTimeout(10000)
						.setSocketTimeout(30000)
						.setRedirectsEnabled(false)
						.build())
				.disableAutomaticRetries()
				.disableRedirectHandling()
				.disableCookieManagement()
				.build();
		this.deadlines = new ScheduledThreadPoolExecutor(1, task -> {
			Thread thread = new Thread(task, "HLib VK Deadline");
			thread.setDaemon(true);
			return thread;
		});
		getDeadlines().setRemoveOnCancelPolicy(true);
	}

	public JsonElement execute(String method, JsonObject parameters) throws IOException {
		if (method == null || !method.matches("[A-Za-z][A-Za-z0-9]*(\\.[A-Za-z][A-Za-z0-9]*)?")) throw new IllegalArgumentException("Некорректный метод VK.");
		JsonObject query = parameters == null ? new JsonObject() : parameters.deepCopy();
		query.addProperty("access_token", getToken());
		query.addProperty("v", getVersion());
		HttpPost request = new HttpPost("${url_vk_api}/method/" + method);
		request.setEntity(new StringEntity(encode(query), ContentType.create("application/x-www-form-urlencoded", StandardCharsets.UTF_8)));
		JsonObject response = request(request);
		try {
			if (response.has("error")) {
				JsonObject error = response.getAsJsonObject("error");
				throw new ApiException(error.get("error_code").getAsInt());
			}
			if (!response.has("response") || response.get("response").isJsonNull()) throw new IOException("Пустой результат API VK.");
			return response.get("response");
		} catch (RuntimeException exception) {
			throw new IOException("Некорректный ответ API VK.");
		}
	}

	public JsonObject longPoll(URI endpoint, JsonObject parameters) throws IOException {
		String host = endpoint == null ? null : endpoint.getHost();
		if (host != null) host = host.toLowerCase();
		if (endpoint == null || !"https".equalsIgnoreCase(endpoint.getScheme()) || endpoint.getUserInfo() != null || endpoint.getFragment() != null || endpoint.getPort() != -1 && endpoint.getPort() != 443 || host == null || !(host.endsWith(".vk.com") || host.endsWith(".vk.ru"))) {
			throw new IllegalArgumentException("Некорректный адрес Long Poll VK.");
		}
		String query = encode(parameters == null ? new JsonObject() : parameters);
		return request(new HttpGet(endpoint + (endpoint.getRawQuery() == null ? "?" : "&") + query));
	}

	private JsonObject request(HttpRequestBase request) throws IOException {
		ScheduledFuture<?> deadline;
		synchronized (getLock()) {
			if (isClosed()) throw new IOException("Клиент VK закрыт.");
			getRequests().add(request);
			deadline = getDeadlines().schedule(request::abort, 45, TimeUnit.SECONDS);
		}
		try (CloseableHttpResponse response = getClient().execute(request)) {
			if (response.getStatusLine().getStatusCode() != 200 || response.getEntity() == null) throw new IOException("Ответ VK отклонён.");
			try (InputStream stream = response.getEntity().getContent()) {
				byte[] data = stream.readNBytes(2097153);
				if (data.length > 2097152) {
					request.abort();
					throw new IOException("Превышен размер ответа VK.");
				}
				return JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
			}
		} catch (IOException | RuntimeException exception) {
			request.abort();
			throw new IOException("Не удалось выполнить запрос VK.");
		} finally {
			deadline.cancel(false);
			synchronized (getLock()) {
				getRequests().remove(request);
			}
		}
	}

	private static String encode(JsonObject parameters) {
		return Tools.join("&", parameters.entrySet().stream().filter(entry -> !entry.getValue().isJsonNull()).map(entry -> {
			JsonElement value = entry.getValue();
			return URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(value.isJsonPrimitive() ? value.getAsString() : value.toString(), StandardCharsets.UTF_8);
		}).toList());
	}

	@Override
	public void close() {
		HttpRequestBase[] active;
		synchronized (getLock()) {
			if (isClosed()) return;
			setClosed(true);
			active = getRequests().toArray(HttpRequestBase[]::new);
			getRequests().clear();
		}
		Stream.of(active).forEach(HttpRequestBase::abort);
		getDeadlines().shutdownNow();
		try {
			getClient().close();
		} catch (IOException exception) {
			Informer.send("Не удалось закрыть клиент VK.");
		}
	}

	public static final class ApiException extends IOException {
		@Serial
		private static final long serialVersionUID = 1L;
		@Getter
		private final int code;

		private ApiException(int code) {
			super("API VK отклонил запрос (код " + code + ").");
			this.code = code;
		}
	}
}