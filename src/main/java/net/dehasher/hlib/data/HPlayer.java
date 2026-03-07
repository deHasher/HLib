package net.dehasher.hlib.data;

import net.dehasher.hlib.Tools;
import net.dehasher.hlib.config.Info;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public record HPlayer(int id, String name) {
    private static final Map<Integer, HPlayer> PLAYERS_BY_ID = new ConcurrentHashMap<>();
    private static final Map<String, HPlayer> PLAYERS_BY_NAME = new ConcurrentHashMap<>();

    public HPlayer(int id, String name) {
        if (name == null || (!name.equals(Info.consoleName) && !Tools.validateNickname(name))) throw new IllegalArgumentException("Invalid player name: " + name);
        this.id = id;
        this.name = name;
        PLAYERS_BY_ID.put(id, this);
        PLAYERS_BY_NAME.put(name.toLowerCase(), this);
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
        List.of(Info.consoleName, "${author}")
                .forEach(name -> Tools.getMySQL().query("SELECT id FROM hcore_player WHERE name = ?")
                        .setArgs(name)
                        .setResult(resultSet -> {
                            if (!resultSet.next()) Tools.getMySQL().query("INSERT INTO hcore_player (name) VALUES (?)")
                                    .setArgs(name)
                                    .execute();
                        })
                        .execute());
        Tools.getMySQL().query("SELECT * FROM hcore_player")
                .setResult(resultSet -> {
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        if (!name.equals(Info.consoleName) && !Tools.validateNickname(name)) {
                            Tools.getMySQL().query("DELETE FROM hcore_player WHERE id = ?")
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
    public static HPlayer createHPlayer(String name) {
        HPlayer existing = PLAYERS_BY_NAME.get(name.toLowerCase());
        if (existing != null) return existing;

        int id = getHPlayerId(name);
        if (id == 0) {
            if (Tools.getMySQL() == null) throw new RuntimeException("MySQL has not been initialized");
            Optional<Integer> insertId = Tools.getMySQL().query("INSERT IGNORE INTO hcore_player (name) VALUES (?)")
                    .setArgs(name)
                    .executeInsert();
            id = insertId.orElseGet(() -> getHPlayerId(name));
        }
        if (id == 0) throw new NullPointerException();
        return createHPlayer(id, name);
    }

    private static Integer getHPlayerId(String name) {
        AtomicInteger id = new AtomicInteger(0);
        if (Tools.getMySQL() == null) throw new RuntimeException("MySQL has not been initialized");
        Tools.getMySQL().query("SELECT id FROM hcore_player WHERE name = ?")
                .setArgs(name)
                .setResult(resultSet -> {
                    while (resultSet.next()) id.set(resultSet.getInt("id"));
                })
                .execute();
        return id.get();
    }

    private static HPlayer createHPlayer(int id, String name) {
        if (id < 0) throw new IllegalArgumentException("Invalid player ID: " + id);
        if (PLAYERS_BY_ID.containsKey(id)) throw new IllegalStateException("Player with ID " + id + " already exists.");
        if (PLAYERS_BY_NAME.containsKey(name.toLowerCase())) throw new IllegalStateException("Player with name " + name + " already exists.");
        return new HPlayer(id, name);
    }
}