package net.dehasher.hlib.controller;

import com.google.common.collect.Lists;
import net.dehasher.hlib.Scheduler;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.data.HPlayer;
import net.dehasher.hlib.data.Platform;
import net.dehasher.hlib.data.Table;
import net.dehasher.hlib.database.MySQL;
import org.bukkit.Location;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LogController {
    private static final Map<Type, LogHandler> HANDLERS = new EnumMap<>(Type.class);

    static {
        Scheduler.doAsync(() -> {
            if (Tools.getMySQL() == null) throw new RuntimeException("MySQL has not been initialized");
            Tools.getMySQL().query(Table.LOG_MESSAGE).execute();
            Tools.getMySQL().query(Table.LOG_COMMAND).execute();
            Tools.getMySQL().query(Table.LOG_SIGN).execute();
            Tools.getMySQL().query(Table.LOG_CANCELLED_PACKET).execute();
            Tools.getMySQL().query(Table.LOG_AI).execute();

            Tools.getMySQL().query(Table.LOG_MESSAGE_CONSTRAINT).execute();
            Tools.getMySQL().query(Table.LOG_COMMAND_CONSTRAINT).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_CONSTRAINT).execute();
            Tools.getMySQL().query(Table.LOG_CANCELLED_PACKET_CONSTRAINT).execute();
            Tools.getMySQL().query(Table.LOG_AI_CONSTRAINT).execute();

            Tools.getMySQL().query(Table.LOG_MESSAGE_INDEX_DATETIME).execute();
            Tools.getMySQL().query(Table.LOG_MESSAGE_INDEX_VALUE).execute();
            Tools.getMySQL().query(Table.LOG_MESSAGE_INDEX_WORLD).execute();
            Tools.getMySQL().query(Table.LOG_MESSAGE_INDEX_PROXY_SERVER).execute();
            Tools.getMySQL().query(Table.LOG_MESSAGE_INDEX_SERVER_ID).execute();
            Tools.getMySQL().query(Table.LOG_MESSAGE_INDEX_VALUE_FULLTEXT).execute();

            Tools.getMySQL().query(Table.LOG_COMMAND_INDEX_DATETIME).execute();
            Tools.getMySQL().query(Table.LOG_COMMAND_INDEX_VALUE).execute();
            Tools.getMySQL().query(Table.LOG_COMMAND_INDEX_WORLD).execute();
            Tools.getMySQL().query(Table.LOG_COMMAND_INDEX_PROXY_SERVER).execute();
            Tools.getMySQL().query(Table.LOG_COMMAND_INDEX_SERVER_ID).execute();
            Tools.getMySQL().query(Table.LOG_COMMAND_INDEX_VALUE_FULLTEXT).execute();

            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_DATETIME).execute();

            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_1).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_2).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_3).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_4).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_5).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_6).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_7).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_8).execute();

            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_1_FULLTEXT).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_2_FULLTEXT).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_3_FULLTEXT).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_4_FULLTEXT).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_5_FULLTEXT).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_6_FULLTEXT).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_7_FULLTEXT).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_LINE_8_FULLTEXT).execute();

            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_WORLD).execute();
            Tools.getMySQL().query(Table.LOG_SIGN_INDEX_SERVER_ID).execute();

            Tools.getMySQL().query(Table.LOG_CANCELLED_PACKET_INDEX_DATETIME).execute();
            Tools.getMySQL().query(Table.LOG_CANCELLED_PACKET_INDEX_VALUE).execute();
            Tools.getMySQL().query(Table.LOG_CANCELLED_PACKET_INDEX_SERVER_ID).execute();

            Tools.getMySQL().query(Table.LOG_AI_INDEX_DATETIME).execute();
            Tools.getMySQL().query(Table.LOG_AI_INDEX_VALUE).execute();
            Tools.getMySQL().query(Table.LOG_AI_INDEX_IP).execute();
            Tools.getMySQL().query(Table.LOG_AI_INDEX_VALUE_FULLTEXT).execute();
        });

        List.of(Type.values()).forEach(type -> HANDLERS.put(type, createHandler(type)));
        Scheduler.doAsyncRepeat(() -> HANDLERS.values().forEach(LogHandler::executeBatch), 20L, 20L);
    }

    public static void logMessage(HPlayer player, String value, String server, Location location) {
        ((MessageHandler) HANDLERS.get(Type.MESSAGE)).add(player, value, server, location);
    }

    public static void logCommand(HPlayer player, String value, String server, Location location) {
        ((CommandHandler) HANDLERS.get(Type.COMMAND)).add(player, value, server, location);
    }

    public static void logSign(HPlayer player, List<String> lines, Location location) {
        ((SignHandler) HANDLERS.get(Type.SIGN)).add(player, lines, location);
    }

    public static void logCancelledPacket(HPlayer player, String value) {
        ((CancelledPacketHandler) HANDLERS.get(Type.CANCELLED_PACKET)).add(player, value);
    }

    public static void logAI(HPlayer player, String value, String ip) {
        ((AIHandler) HANDLERS.get(Type.AI)).add(player, value, ip);
    }

    private static LogHandler createHandler(Type type) {
        return switch (type) {
            case MESSAGE -> new MessageHandler();
            case COMMAND -> new CommandHandler();
            case SIGN -> new SignHandler();
            case CANCELLED_PACKET -> new CancelledPacketHandler();
            case AI -> new AIHandler();
        };
    }

    private static abstract class LogHandler {
        protected final Queue<Object[]> batchQueue = new ConcurrentLinkedQueue<>();
        protected final String sql;

        protected LogHandler(String sql) {
            this.sql = sql;
        }

        protected void addBatch(Object... args) {
            batchQueue.add(args);
        }

        public void executeBatch() {
            if (batchQueue.isEmpty()) return;
            if (Tools.getMySQL() == null) return;

            MySQL.Query query = Tools.getMySQL().query(sql);
            List<Object[]> batchArgs = Lists.newArrayListWithCapacity(batchQueue.size());
            Object[] data;
            while ((data = batchQueue.poll()) != null) batchArgs.add(data);
            if (batchArgs.isEmpty()) return;
            batchArgs.forEach(query::addBatch);
            query.executeBatch();
        }
    }

    private static class MessageHandler extends LogHandler {
        private MessageHandler() {
            super(Platform.get().isProxy() ?
                    "INSERT INTO hcore_log_message (player_id, value, proxy_server, server_id) VALUES (?, ?, ?, ?)" :
                    "INSERT INTO hcore_log_message (player_id, value, x, y, z, world, server_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
        }

        @SuppressWarnings("DuplicatedCode")
        public void add(HPlayer player, String value, String server, Location location) {
            Object[] args = Platform.get().isProxy() ?
                    new Object[]{
                            player.id(),
                            value,
                            server,
                            Tools.getServerID()
                    } :
                    new Object[]{
                            player.id(),
                            value,
                            location != null ? location.getX() : null,
                            location != null ? location.getY() : null,
                            location != null ? location.getZ() : null,
                            location != null && location.getWorld() != null ? location.getWorld().getName() : null,
                            Tools.getServerID()
                    };
            addBatch(args);
        }
    }

    private static class CommandHandler extends LogHandler {
        private CommandHandler() {
            super(Platform.get().isProxy() ?
                    "INSERT INTO hcore_log_command (player_id, value, proxy_server, server_id) VALUES (?, ?, ?, ?)" :
                    "INSERT INTO hcore_log_command (player_id, value, x, y, z, world, server_id) VALUES (?, ?, ?, ?, ?, ?, ?)");
        }

        @SuppressWarnings("DuplicatedCode")
        public void add(HPlayer player, String value, String server, Location location) {
            Object[] args = Platform.get().isProxy() ?
                    new Object[]{
                            player.id(),
                            value,
                            server,
                            Tools.getServerID()
                    } :
                    new Object[]{
                            player.id(),
                            value,
                            location != null ? location.getX() : null,
                            location != null ? location.getY() : null,
                            location != null ? location.getZ() : null,
                            location != null && location.getWorld() != null ? location.getWorld().getName() : null,
                            Tools.getServerID()
                    };
            addBatch(args);
        }
    }

    private static class SignHandler extends LogHandler {
        private SignHandler() {
            super("INSERT INTO hcore_log_sign (player_id, line_1, line_2, line_3, line_4, line_5, line_6, line_7, line_8, x, y, z, world, server_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        }

        public void add(HPlayer player, List<String> lines, Location location) {
            Object[] args = new Object[]{
                    player.id(),
                    getLine(lines, 0),
                    getLine(lines, 1),
                    getLine(lines, 2),
                    getLine(lines, 3),
                    getLine(lines, 4),
                    getLine(lines, 5),
                    getLine(lines, 6),
                    getLine(lines, 7),
                    location.getX(),
                    location.getY(),
                    location.getZ(),
                    location.getWorld().getName(),
                    Tools.getServerID()
            };
            addBatch(args);
        }

        private String getLine(List<String> lines, int index) {
            return index < lines.size() ? (lines.get(index).isEmpty() ? null : lines.get(index)) : null;
        }
    }

    private static class CancelledPacketHandler extends LogHandler {
        private CancelledPacketHandler() {
            super("INSERT INTO hcore_log_cancelled_packet (player_id, value, server_id) VALUES (?, ?, ?)");
        }

        public void add(HPlayer player, String value) {
            Object[] args = new Object[]{
                    player.id(),
                    value,
                    Tools.getServerID()
            };
            addBatch(args);
        }
    }

    private static class AIHandler extends LogHandler {
        private AIHandler() {
            super("INSERT INTO hcore_log_ai (player_id, value, ip) VALUES (?, ?, ?)");
        }

        public void add(HPlayer player, String value, String ip) {
            Object[] args = new Object[]{
                    player.id(),
                    value,
                    ip
            };
            addBatch(args);
        }
    }

    public enum Type { MESSAGE, COMMAND, SIGN, CANCELLED_PACKET, AI }
}