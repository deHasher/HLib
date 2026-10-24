package net.dehasher.hlib.data;

import net.dehasher.hlib.Tools;
import net.dehasher.hlib.config.Info;
import net.dehasher.hlib.database.MySQL;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public record HPlayer(int id, String name) {
	private static final Object LOCK = new Object();
	private static final Map<Integer, HPlayer> PLAYERS_BY_ID = new ConcurrentHashMap<>();
	private static final Map<String, HPlayer> PLAYERS_BY_NAME = new ConcurrentHashMap<>();

	public HPlayer(int id, String name) {
		if (id < 0) throw new IllegalArgumentException("Invalid player ID: " + id);
		if (name == null || (!name.equals(Info.consoleName) && !Tools.validateNickname(name))) throw new IllegalArgumentException("Invalid player name: " + name);
		this.id = id;
		this.name = name;
		synchronized (LOCK) {
			HPlayer byId = PLAYERS_BY_ID.get(id);
			HPlayer byName = PLAYERS_BY_NAME.get(name.toLowerCase());
			if (byId != null && !byId.name().equals(name)) throw new IllegalStateException("Player ID is already associated with another name");
			if (byName != null && byName.id() != id) throw new IllegalStateException("Player name is already associated with another ID");
			HPlayer cached = byId != null ? byId : this;
			PLAYERS_BY_ID.put(id, cached);
			PLAYERS_BY_NAME.put(name.toLowerCase(), cached);
		}
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public static void initMySQL() {
		if (Tools.getMySQL() == null) throw new RuntimeException("MySQL has not been initialized");
		Tools.getMySQL().query(Table.PLAYER)
				.execute();
		List.of(Info.consoleName, "${author}").forEach(HPlayer::createHPlayer);
		Tools.getMySQL().query("SELECT * FROM hlib_player")
				.setResult(resultSet -> {
					while (resultSet.next()) {
						int id = resultSet.getInt("id");
						String name = resultSet.getString("name");
						if (!name.equals(Info.consoleName) && !Tools.validateNickname(name)) {
							if (Tools.getMySQL() == null) throw new RuntimeException("MySQL has not been initialized");
							Tools.getMySQL().query("DELETE FROM hlib_player WHERE id = ?")
									.setArgs(id)
									.execute();
							continue;
						}
						createHPlayer(id, name);
					}
				})
				.execute();
	}

	public static Collection<HPlayer> getPlayers() {
		return Collections.unmodifiableCollection(PLAYERS_BY_ID.values());
	}

	public static HPlayer getHPlayer(String name) {
		return name != null ? PLAYERS_BY_NAME.get(name.toLowerCase()) : null;
	}

	public static HPlayer getHPlayer(int id) {
		return PLAYERS_BY_ID.get(id);
	}

	// Вызывать только 1 раз при подключении игрока к серверу.
	@SuppressWarnings("UnusedReturnValue")
	public static HPlayer createHPlayer(String name) {
		if (name == null || (!name.equals(Info.consoleName) && !Tools.validateNickname(name))) throw new IllegalArgumentException("Invalid player name: " + name);
		synchronized (LOCK) {
			HPlayer existing = PLAYERS_BY_NAME.get(name.toLowerCase());
			if (existing != null) return existing;
			MySQL mysql = Tools.getMySQL();
			if (mysql == null) throw new IllegalStateException("MySQL has not been initialized");
			Identity stored;
			try {
				stored = mysql.transaction(transaction -> {
					transaction.update("INSERT INTO hlib_player (name) VALUES (?) ON DUPLICATE KEY UPDATE id = id", name);
					return transaction.query("SELECT id, name FROM hlib_player WHERE name = ? FOR UPDATE", result -> {
						if (!result.next()) throw new SQLException("Cannot load player identity");
						return new Identity(result.getInt("id"), result.getString("name"));
					}, name);
				});
			} catch (SQLException exception) {
				throw new IllegalStateException("Cannot load player identity", exception);
			}
			return createHPlayer(stored.id(), stored.name());
		}
	}

	private static HPlayer createHPlayer(int id, String name) {
		synchronized (LOCK) {
			HPlayer existing = PLAYERS_BY_ID.get(id);
			if (existing != null && existing.name().equals(name)) return existing;
			return new HPlayer(id, name);
		}
	}

	private record Identity(int id, String name) {}
}