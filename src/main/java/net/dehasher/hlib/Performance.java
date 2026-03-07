package net.dehasher.hlib;

import com.google.common.collect.Lists;
import lombok.Getter;
import net.dehasher.hlib.data.BukkitVersion;
import net.dehasher.hlib.data.Plugin;
import redis.clients.jedis.Jedis;
import net.dehasher.hlib.data.Platform;
import net.dehasher.hlib.database.Redis;
import net.dehasher.hlib.hook.SparkHook;
import java.io.File;
import java.text.DecimalFormat;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.time.format.DateTimeFormatter;

public class Performance {
    @Getter
    private static final Map<String, Integer> online = new ConcurrentHashMap<>();
    @Getter
    private static final Map<String, Integer> maxOnline = new ConcurrentHashMap<>();
    @Getter
    private static final Map<String, Integer> CPUProcess = new ConcurrentHashMap<>();
    @Getter
    private static final Map<String, Integer> CPUSystem = new ConcurrentHashMap<>();

    @Getter
    private static final Map<String, Double> RAMTotal = new ConcurrentHashMap<>();
    @Getter
    private static final Map<String, Double> RAMMax = new ConcurrentHashMap<>();
    @Getter
    private static final Map<String, Double> disk = new ConcurrentHashMap<>();
    @Getter
    private static final Map<String, Double> TPS = new ConcurrentHashMap<>();
    @Getter
    private static final Set<String> servers = ConcurrentHashMap.newKeySet();

    private static final Calendar CALENDAR = Calendar.getInstance();
    private static final ArrayList<Integer> AVERAGE_ONLINE = Lists.newArrayList();
    private static Double THIS_DISK = 0.0;
    private static Integer MAX_CURRENT_ONLINE = 0;

    public static void init() {
        Scheduler.doAsync(() -> {
            if (Tools.getRedis() == null) return;
            try (Jedis jedis = Tools.getRedis().getPool().getResource()) {
                jedis.subscribe(new Redis.PubSub() {
                    @Override
                    public void onMessage(String channel, String message) {
                        try {
                            if (!Tools.isHCoreEnabled()) return;
                            String name = message.split(":")[0];
                            if (Tools.getServerID().equalsIgnoreCase(name)) return;
                            put(message);
                            servers.add(name);
                            Cooldowner.start(name, Cooldowner.Type.PERFORMANCE, 3);
                        } catch (Throwable t) {
                            Informer.send("An error occurred while processing Redis data! message: " + message);
                            t.printStackTrace();
                        }
                    }
                }, Performance.class.getSimpleName());
            } catch (Throwable t) {
                t.printStackTrace();
                Tools.shutdown();
            }
        });

        Scheduler.doAsyncRepeat(() -> {
            File file = new File("/");
            int i = (int) (file.getTotalSpace() / 1024L / 1024L / 1024L);
            int j = (int) (file.getFreeSpace()  / 1024L / 1024L / 1024L);
            DecimalFormat decimalFormat = new DecimalFormat("0.00");
            THIS_DISK = Tools.parseDouble(decimalFormat.format((i - j) * 100.0D / i).replace(",", "."));
        }, 0L, 60 * 20L);
    }

    public static void update() { // Вызывается каждую секунду.
        try {
            int online      = calcOnline();
            int maxOnline   = calcMaxOnline(online);
            int cpuProcess  = calcCPUProcess();
            int cpuSystem   = calcCPUSystem();

            double ramTotal = calcRamTotal();
            double ramMax   = calcRamMax();
            double disk     = calcDisk();
            double tps      = calcTPS();

            // Сохраняем данные в память у текущего сервера.
            put(Tools.join(":", Tools.getServerID(), online, maxOnline, cpuProcess, cpuSystem, ramTotal, ramMax, disk, tps));

            if (!Tools.isHCoreEnabled() || Tools.getRedis() == null) return;

            // Перебор всех серверов, и если сервер не пинговал 5 секунд - сбрасываем значения в памяти конкретного сервера.
            servers.removeIf(name -> {
                if (!Cooldowner.inCooldown(name, Cooldowner.Type.PERFORMANCE)) {
                    put(Tools.join(":", name, -1, -1, -1, -1, -1, -1, -1, -1));
                    return true;
                }
                return false;
            });

            // Отправляем временные данные на 5 секунд для парсинга на сайте.
            try (Jedis jedis = Tools.getRedis().getPool().getResource()) {
                jedis.setex(Tools.getServerID(), 5, Tools.join(":", online, maxOnline, cpuProcess, cpuSystem, ramTotal, ramMax, disk, tps));
                if (Platform.get().isProxy()) {
                    LocalDateTime now = LocalDateTime.now();
                    CALENDAR.setTime(Date.from(now.toInstant(ZoneOffset.from(ZoneId.systemDefault().getRules().getOffset(Instant.now())))));
                    int minutes = CALENDAR.get(Calendar.MINUTE);
                    int hours   = CALENDAR.get(Calendar.HOUR);
                    if (minutes == 0 && hours == 0) MAX_CURRENT_ONLINE = online;
                    if (minutes % 10 == 0) jedis.setex(Tools.join("-", "HStats", "current", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"))), 60 * 60 * 24, String.valueOf(online));
                    AVERAGE_ONLINE.add(online);
                    jedis.set(Tools.join("-", "HStats", "daily", now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))), Tools.join(":", String.valueOf(getAverageOnline()), calcMaxOnline(-1)));
                }
            }

            // Отправляем данные в pub sub redis для обработки на других серверах.
            Tools.getRedis().publish(Performance.class.getSimpleName(), Tools.join(":", Tools.getServerID(), online, maxOnline, cpuProcess, cpuSystem, ramTotal, ramMax, disk, tps));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static void put(String info) {
        String[] data = info.split(":");
        String name   = data[0];
        getOnline()    .put(name, Integer.valueOf(data[1]));
        getMaxOnline() .put(name, Integer.valueOf(data[2]));
        getCPUProcess().put(name, Integer.valueOf(data[3]));
        getCPUSystem() .put(name, Integer.valueOf(data[4]));
        getRAMTotal()  .put(name, Double .valueOf(data[5]));
        getRAMMax()    .put(name, Double .valueOf(data[6]));
        getDisk()      .put(name, Double .valueOf(data[7]));
        getTPS()       .put(name, Double .valueOf(data[8]));
    }

    public static void gc() {
        if (!Tools.requireBukkitVersion(BukkitVersion.V1_18)) System.gc();
    }

    private static int calcOnline() {
        return Tools.getOnline();
    }

    private static int calcMaxOnline(int online) {
        if (online > MAX_CURRENT_ONLINE) MAX_CURRENT_ONLINE = online;
        return MAX_CURRENT_ONLINE;
    }

    public static int getAverageOnline() {
        int sum = 0;
        for (Integer integer : AVERAGE_ONLINE) sum += integer;
        return Math.round((float) sum / AVERAGE_ONLINE.size());
    }

    private static int calcCPUProcess() {
        return Plugin.SPARK.isEnabled() ? SparkHook.getCPUProcess() : -1;
    }

    private static int calcCPUSystem() {
        return Plugin.SPARK.isEnabled() ? SparkHook.getCPUSystem() : -1;
    }

    private static double calcTPS() {
        return Plugin.SPARK.isEnabled() ? SparkHook.getTPS() : -1;
    }

    private static double calcDisk() {
        return THIS_DISK;
    }

    private static double calcRamTotal() {
        return Tools.round((double) Runtime.getRuntime().totalMemory() / 1024L / 1024L / 1024L, 1);
    }

    private static double calcRamMax() {
        return Tools.round((double) Runtime.getRuntime().maxMemory() / 1024L / 1024L / 1024L, 1);
    }
}