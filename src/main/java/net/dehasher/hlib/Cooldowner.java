package net.dehasher.hlib;

import net.dehasher.hlib.data.Table;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class Cooldowner {
    private static final Map<String, Long> cooldowns = new ConcurrentHashMap<>();

    // Создаём кулдаун.
    public static void start(String name, Type type, long time) {
        start(name, type, time, TimeUnit.SECONDS);
    }

    // Создаём кулдаун.
    public static void start(String name, Type type, long time, TimeUnit timeUnit) {
        String key   = key(name, type);
        long addTime = timeUnit.toMillis(time);
        long endTime = System.currentTimeMillis() + addTime;
        cooldowns.put(key, endTime);
        if (!type.isStrict()) return;
        if (Tools.getMySQL() == null) throw new RuntimeException("MySQL has not been initialized");
        Scheduler.doAsync(() -> Tools.getMySQL().query("INSERT IGNORE INTO hcore_cooldown (cooldown_id, server_id, until) VALUES (?, ?, ?)")
                .setArgs(key, Tools.getServerID(), DateTimeFormatter.DATE_TIME.format(new Date(endTime)))
                .execute());
    }

    // Убираем кулдаун.
    public static void remove(String name, Type type) {
        if (name == null || type == null) return;
        String key = key(name, type);
        cooldowns.remove(key);
        if (!type.isStrict()) return;
        if (Tools.getMySQL() == null) throw new RuntimeException("MySQL has not been initialized");
        Scheduler.doAsync(() -> Tools.getMySQL().query("DELETE FROM hcore_cooldown WHERE cooldown_id = ? AND server_id = ?")
                .setArgs(key, Tools.getServerID())
                .execute());
    }

    // Подгружаем базу данных.
    public static void initMySQL() {
        Scheduler.doAsync(() -> {
            if (Tools.getMySQL() == null) throw new RuntimeException("MySQL has not been initialized");
            Tools.getMySQL().query(Table.COOLDOWN)
                    .execute();
            Tools.getMySQL().query("DELETE FROM hcore_cooldown WHERE until < NOW()")
                    .execute();
            Tools.getMySQL().query("SELECT * FROM hcore_cooldown WHERE until > NOW() AND server_id = ?")
                    .setArgs(Tools.getServerID())
                    .setResult(resultSet -> {
                        while (resultSet.next()) cooldowns.put(resultSet.getString("cooldown_id"), resultSet.getTimestamp("until").getTime());
                    })
                    .execute();
        });
    }

    // Проверяем существует ли кулдаун.
    public static boolean inCooldown(String name, Type type) {
        return getTimeLeft(name, type, TimeUnit.MILLISECONDS) > 0;
    }

    // Получаем оставшееся время кулдауна.
    public static long getTimeLeft(String name, Type type) {
        return getTimeLeft(name, type, TimeUnit.SECONDS);
    }

    // Получаем оставшееся время кулдауна.
    public static long getTimeLeft(String name, Type type, TimeUnit timeUnit) {
        Long endTime = cooldowns.get(key(name, type));
        if (endTime == null) return 0;
        long now = System.currentTimeMillis();
        long timeLeft = endTime - now;
        if (timeLeft > 0) return timeUnit.convert(timeLeft, TimeUnit.MILLISECONDS);
        remove(name, type);
        return 0;
    }

    // Создаём уникальный ключ. (если нужно указать более одного элемента ключа)
    private static String key(String name, Type type) {
        return key(name, type.name());
    }

    // Создаём уникальный ключ. (если нужно указать более одного элемента ключа)
    public static String key(String... part) {
        return Tools.join(Tools.getLineSeparator(), List.of(part));
    }

    // Типы задержек.
    public enum Type {
        ONE_SECOND_MESSAGE(false), // Общий кулдаун для сообщений, которые могут спамить в чате.
        REQUESTOR_CINEMA(false),
        REQUESTOR_MARRY(false),
        REQUESTOR_CLANS(false),
        MARRY_PVP(false),
        MARRY_GIFT(false),
        MARRY_HEAL(false),
        CLANS_PVP(false),
        CLANS_TOP_REWARD(true),
        CLANS_TOP_REWARD_PLAYER(true),
        PAPI_TOP_REWARD(true),
        EGG_COOLDOWN(false),
        EGG_COOLDOWN_MSG(false),
        KILL(false),
        DRILL3X3(false),
        SWAP(false),
        RAPE_EFFECTS(false),
        CRATE_OPEN(false),
        SWAP_INVENTORY(false),
        PVP(false),
        PVP_ARENA_LEAVE(false),
        STAND(false),
        COMMAND(false),
        CONTRACTS(false),
        KALIAN(false),
        DEATH(false),
        PROTECT(false),
        CRASH(false),
        AI(false),
        AI_CONVERSATION(false),
        PISS_EXHAUSTION(false),
        PISS_SOUND(false),
        CRY_OTHER(false),
        CRY_SOUND(false),
        VOMIT_SOUND(false),
        PERFORMANCE(false),
        THROWN_FILTER(false),
        REDSTONE(false),
        ANTIRELOG_TOTEM_OF_UNDYING(false),
        ANTIRELOG_CHORUS_FRUIT(false),
        ANTIRELOG_GOLDEN_APPLE(false),
        ANTIRELOG_ENCHANTED_GOLDEN_APPLE(false),
        ANTIRELOG_ENDER_PEARL(false),
        ANTIRELOG_TRIDENT(false);

        private final boolean strict;

        Type(boolean strict) {
            this.strict = strict;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean isStrict() {
            return strict;
        }
    }
}