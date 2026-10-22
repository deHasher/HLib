package net.dehasher.hlib.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.config.Info;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Getter(AccessLevel.PRIVATE)
public final class TelegramClient implements AutoCloseable {

	@Getter(AccessLevel.PRIVATE)
	private static final Gson gson = new Gson();
	private final String baseUrl;
	private final Object lock = new Object();
	private final Set<HttpPost> requests = ConcurrentHashMap.newKeySet();
	private final CloseableHttpClient client;
	private final ScheduledThreadPoolExecutor deadlines;
	@Setter(AccessLevel.PRIVATE)
	private boolean closed;

	public TelegramClient(String token) {
		if (token == null || !token.matches("[A-Za-z0-9_:-]+")) throw new IllegalArgumentException("Некорректный токен Telegram.");
		this.baseUrl = "https://api.telegram.org/bot" + token + "/";
		String type = Info.TelegramProxy.type.toUpperCase();
		if (!Set.of("NO_PROXY", "HTTP", "SOCKS4", "SOCKS5").contains(type)) throw new IllegalArgumentException("Неизвестный тип прокси Telegram.");
		boolean socks = type.startsWith("SOCKS");
		Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
				.register("http", PlainConnectionSocketFactory.getSocketFactory())
				.register("https", socks ? new SocksFactory(type, Info.TelegramProxy.host, Info.TelegramProxy.port,
						Info.TelegramProxy.username, Info.TelegramProxy.password) : SSLConnectionSocketFactory.getSocketFactory())
				.build();
		PoolingHttpClientConnectionManager manager = socks
				? new PoolingHttpClientConnectionManager(registry, host -> new InetAddress[]{InetAddress.getLoopbackAddress()})
				: new PoolingHttpClientConnectionManager(registry);
		manager.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(30000).build());
		manager.setMaxTotal(4);
		manager.setDefaultMaxPerRoute(4);
		manager.setValidateAfterInactivity(1000);
		HttpClientBuilder builder = HttpClientBuilder.create()
				.setConnectionManager(manager)
				.setDefaultRequestConfig(RequestConfig.custom()
						.setConnectTimeout(10000)
						.setConnectionRequestTimeout(10000)
						.setSocketTimeout(30000)
						.setRedirectsEnabled(false)
						.build())
				.disableAutomaticRetries()
				.disableRedirectHandling()
				.disableCookieManagement();
		if (type.equals("HTTP")) {
			builder.setProxy(new HttpHost(Info.TelegramProxy.host, Info.TelegramProxy.port));
			if (!Info.TelegramProxy.username.isEmpty()) {
				BasicCredentialsProvider credentials = new BasicCredentialsProvider();
				credentials.setCredentials(new AuthScope(Info.TelegramProxy.host, Info.TelegramProxy.port),
						new UsernamePasswordCredentials(Info.TelegramProxy.username, Info.TelegramProxy.password));
				builder.setDefaultCredentialsProvider(credentials);
			}
		}
		this.client = builder.build();
		this.deadlines = new ScheduledThreadPoolExecutor(1, task -> {
			Thread thread = new Thread(task, "HLib Telegram Deadline");
			thread.setDaemon(true);
			return thread;
		});
		getDeadlines().setRemoveOnCancelPolicy(true);
	}

	public JsonObject execute(String method, JsonObject payload) throws IOException {
		if (method == null || !method.matches("[A-Za-z][A-Za-z0-9]*")) throw new IllegalArgumentException("Некорректный метод Telegram.");
		HttpPost request = new HttpPost(getBaseUrl() + method);
		request.setEntity(new StringEntity(getGson().toJson(payload), ContentType.APPLICATION_JSON));
		ScheduledFuture<?> deadline;
		synchronized (getLock()) {
			if (isClosed()) throw new IOException("Клиент Telegram закрыт.");
			getRequests().add(request);
			deadline = getDeadlines().schedule(request::abort, 45, TimeUnit.SECONDS);
		}
		try (CloseableHttpResponse response = getClient().execute(request)) {
			if (response.getEntity() == null) throw new IOException("Пустой ответ Telegram.");
			try (java.io.InputStream stream = response.getEntity().getContent()) {
				byte[] data = stream.readNBytes(4194305);
				if (data.length > 4194304) throw new IOException("Превышен размер ответа Telegram.");
				return JsonParser.parseString(new String(data, StandardCharsets.UTF_8)).getAsJsonObject();
			}
		} catch (IOException | RuntimeException exception) {
			throw new IOException("Не удалось выполнить запрос Telegram.");
		} finally {
			deadline.cancel(false);
			synchronized (getLock()) {
				getRequests().remove(request);
			}
		}
	}

	@Override
	public void close() {
		HttpPost[] active;
		synchronized (getLock()) {
			if (isClosed()) return;
			setClosed(true);
			active = getRequests().toArray(HttpPost[]::new);
			getRequests().clear();
		}
		Stream.of(active).forEach(HttpPost::abort);
		getDeadlines().shutdownNow();
		try {
			getClient().close();
		} catch (IOException exception) {
			Informer.send("Не удалось закрыть клиент Telegram.");
		}
	}

	@Getter(AccessLevel.PRIVATE)
	private static final class SocksFactory extends SSLConnectionSocketFactory {
		private final String type;
		private final InetSocketAddress proxy;
		private final String username;
		private final String password;

		private SocksFactory(String type, String host, int port, String username, String password) {
			super(SSLContexts.createSystemDefault());
			this.type = type;
			this.proxy = new InetSocketAddress(host, port);
			this.username = username;
			this.password = password;
		}

		@Override
		public Socket createSocket(HttpContext context) {
			return new Socket(Proxy.NO_PROXY);
		}

		@Override
		public Socket connectSocket(int timeout, Socket socket, HttpHost host, InetSocketAddress remote,
				InetSocketAddress local, HttpContext context) throws IOException {
			Socket tunnel = socket == null ? createSocket(context) : socket;
			try {
				if (local != null) tunnel.bind(local);
				tunnel.setSoTimeout(30000);
				tunnel.connect(getProxy(), timeout);
				DataInputStream input = new DataInputStream(tunnel.getInputStream());
				DataOutputStream output = new DataOutputStream(tunnel.getOutputStream());
				if (getType().equals("SOCKS4")) connect4(input, output, host);
				else connect5(input, output, host);
				return createLayeredSocket(tunnel, host.getHostName(), host.getPort(), context);
			} catch (IOException | RuntimeException exception) {
				try {
					tunnel.close();
				} catch (IOException closeException) {
					exception.addSuppressed(closeException);
				}
				throw exception;
			}
		}

		private void connect4(DataInputStream input, DataOutputStream output, HttpHost host) throws IOException {
			if (getUsername().indexOf(0) != -1) throw new IOException("Invalid SOCKS4 username");
			output.writeByte(4);
			output.writeByte(1);
			output.writeShort(host.getPort());
			output.writeInt(1);
			output.write(getUsername().getBytes(StandardCharsets.UTF_8));
			output.writeByte(0);
			output.write(host.getHostName().getBytes(StandardCharsets.US_ASCII));
			output.writeByte(0);
			output.flush();
			if (input.readUnsignedByte() != 0 || input.readUnsignedByte() != 90) throw new IOException("SOCKS4 refused connection");
			input.readFully(new byte[6]);
		}

		private void connect5(DataInputStream input, DataOutputStream output, HttpHost host) throws IOException {
			boolean authenticate = !getUsername().isEmpty();
			output.write(new byte[]{5, 1, (byte) (authenticate ? 2 : 0)});
			output.flush();
			if (input.readUnsignedByte() != 5 || input.readUnsignedByte() != (authenticate ? 2 : 0)) throw new IOException("SOCKS5 authentication rejected");
			if (authenticate) {
				byte[] user = getUsername().getBytes(StandardCharsets.UTF_8);
				byte[] secret = getPassword().getBytes(StandardCharsets.UTF_8);
				if (user.length > 255 || secret.length > 255 || secret.length == 0) throw new IOException("Invalid SOCKS5 credentials");
				output.writeByte(1);
				output.writeByte(user.length);
				output.write(user);
				output.writeByte(secret.length);
				output.write(secret);
				output.flush();
				if (input.readUnsignedByte() != 1 || input.readUnsignedByte() != 0) throw new IOException("SOCKS5 authentication failed");
			}
			byte[] domain = host.getHostName().getBytes(StandardCharsets.US_ASCII);
			output.write(new byte[]{5, 1, 0, 3, (byte) domain.length});
			output.write(domain);
			output.writeShort(host.getPort());
			output.flush();
			if (input.readUnsignedByte() != 5 || input.readUnsignedByte() != 0 || input.readUnsignedByte() != 0) throw new IOException("SOCKS5 refused connection");
			int length = switch (input.readUnsignedByte()) {
				case 1 -> 4;
				case 3 -> input.readUnsignedByte();
				case 4 -> 16;
				default -> throw new IOException("Invalid SOCKS5 address");
			};
			input.readFully(new byte[length + 2]);
		}
	}
}