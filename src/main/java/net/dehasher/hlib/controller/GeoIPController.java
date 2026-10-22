package net.dehasher.hlib.controller;

import com.google.common.net.InetAddresses;
import com.maxmind.db.CHMCache;
import com.maxmind.db.Reader.FileMode;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.config.Info;
import net.dehasher.hlib.data.Geo;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class GeoIPController {
	@Getter(AccessLevel.PRIVATE)
	private static final Object lock = new Object();
	@Getter(AccessLevel.PRIVATE)
	@Setter(AccessLevel.PRIVATE)
	private static DatabaseReader reader;
	@Getter(AccessLevel.PRIVATE)
	@Setter(AccessLevel.PRIVATE)
	private static ExecutorService executor;
	@Getter(AccessLevel.PRIVATE)
	@Setter(AccessLevel.PRIVATE)
	private static HttpURLConnection connection;

	private GeoIPController() {}

	public static void start(Path directory) {
		synchronized (getLock()) {
			if (getExecutor() != null) return;
			Path database = directory.toAbsolutePath().resolve("GeoLite2-City.mmdb");
			String locale = Info.GeoIP.locale;
			if (Files.isRegularFile(database)) {
				try {
					setReader(open(database, locale));
				} catch (IOException exception) {
					Informer.send("Не удалось открыть GeoLite2 HLib; база будет загружена заново.");
				}
			}
			setExecutor(Executors.newSingleThreadExecutor(task -> {
				Thread thread = new Thread(task, "HLib GeoIP Update");
				thread.setDaemon(true);
				return thread;
			}));
			getExecutor().execute(() -> update(database, locale, Info.GeoIP.downloadUrl));
		}
	}

	public static Geo get(String address) {
		if (address == null) return null;
		try {
			return get(InetAddresses.forString(address));
		} catch (IllegalArgumentException exception) {
			return null;
		}
	}

	public static Geo get(InetAddress address) {
		synchronized (getLock()) {
			if (address == null || getReader() == null) return null;
			try {
				CityResponse response = getReader().city(address);
				return new Geo(
						Objects.toString(response.getCountry().getIsoCode(), ""),
						Objects.toString(response.getCountry().getName(), ""),
						Objects.toString(response.getMostSpecificSubdivision().getName(), ""),
						Objects.toString(response.getCity().getName(), ""),
						response.getLocation().getLatitude(), response.getLocation().getLongitude()
				);
			} catch (IOException | GeoIp2Exception exception) {
				return null;
			}
		}
	}

	public static void close() {
		synchronized (getLock()) {
			if (getExecutor() != null) getExecutor().shutdownNow();
			if (getConnection() != null) getConnection().disconnect();
			close(getReader());
			setReader(null);
		}
	}

	private static void update(Path database, String locale, String source) {
		Path temporary = null;
		DatabaseReader replacement = null;
		HttpURLConnection request = null;
		try {
			URI uri = URI.create(source);
			if (!"https".equalsIgnoreCase(uri.getScheme())) throw new IOException("GeoIP requires HTTPS");
			Path directory = Objects.requireNonNull(database.getParent());
			Files.createDirectories(directory);
			temporary = Files.createTempFile(directory, ".GeoLite2-", ".mmdb");
			request = (HttpURLConnection) uri.toURL().openConnection();
			request.setConnectTimeout(10000);
			request.setReadTimeout(30000);
			request.setInstanceFollowRedirects(false);
			synchronized (getLock()) {
				if (getExecutor().isShutdown()) return;
				setConnection(request);
			}
			if (request.getResponseCode() != HttpURLConnection.HTTP_OK) throw new IOException("GeoIP download failed");
			try (InputStream input = request.getInputStream(); OutputStream output = Files.newOutputStream(temporary)) {
				byte[] buffer = new byte[65536];
				long size = 0;
				int length;
				while ((length = input.read(buffer)) != -1) {
					size += length;
					if (size > 268435456L || Thread.currentThread().isInterrupted()) throw new IOException("GeoIP download interrupted");
					output.write(buffer, 0, length);
				}
			}
			replacement = open(temporary, locale);
			synchronized (getLock()) {
				if (getExecutor().isShutdown()) return;
				try {
					Files.move(temporary, database, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
				} catch (AtomicMoveNotSupportedException exception) {
					Files.move(temporary, database, StandardCopyOption.REPLACE_EXISTING);
				}
				DatabaseReader previous = getReader();
				setReader(replacement);
				replacement = null;
				close(previous);
			}
		} catch (IOException | IllegalArgumentException exception) {
			synchronized (getLock()) {
				if (!getExecutor().isShutdown()) Informer.send("Не удалось обновить GeoLite2 HLib. Сохранённая база остаётся доступной, если была загружена.");
			}
		} finally {
			close(replacement);
			if (request != null) request.disconnect();
			synchronized (getLock()) {
				setConnection(null);
			}
			if (temporary != null) {
				try {
					Files.deleteIfExists(temporary);
				} catch (IOException exception) {
					Informer.send("Не удалось удалить временный файл GeoLite2 HLib.");
				}
			}
		}
	}

	private static DatabaseReader open(Path file, String locale) throws IOException {
		DatabaseReader database = new DatabaseReader.Builder(file.toFile())
				.fileMode(FileMode.MEMORY)
				.withCache(new CHMCache(16384))
				.locales(List.of(locale, "en"))
				.build();
		if (!"GeoLite2-City".equals(database.getMetadata().getDatabaseType())) {
			database.close();
			throw new IOException("Invalid GeoLite2 database type");
		}
		return database;
	}

	private static void close(DatabaseReader database) {
		if (database == null) return;
		try {
			database.close();
		} catch (IOException exception) {
			Informer.send("Не удалось закрыть базу GeoLite2 HLib.");
		}
	}
}