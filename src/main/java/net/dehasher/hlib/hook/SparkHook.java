package net.dehasher.hlib.hook;

import me.lucko.spark.api.Spark;
import me.lucko.spark.api.SparkProvider;
import me.lucko.spark.api.statistic.StatisticWindow;
import me.lucko.spark.api.statistic.misc.DoubleAverageInfo;
import me.lucko.spark.api.statistic.types.DoubleStatistic;
import me.lucko.spark.api.statistic.types.GenericStatistic;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.data.Platform;

public class SparkHook {
    public static Spark getPlugin() {
        try {
            return SparkProvider.get();
        } catch (Throwable t) {
            return null;
        }
    }

    public static int getCPUProcess() {
        if (getPlugin() == null) return -1;
        return (int) (getPlugin().cpuProcess().poll(StatisticWindow.CpuUsage.SECONDS_10) * 100);
    }

    public static int getCPUSystem() {
        if (getPlugin() == null) return -1;
        return (int) (getPlugin().cpuSystem().poll(StatisticWindow.CpuUsage.SECONDS_10) * 100);
    }

    public static double getTPS() {
        if (getPlugin() == null || Platform.get() != Platform.BUKKIT) return -1;
        DoubleStatistic<StatisticWindow.TicksPerSecond> tps = getPlugin().tps();
        if (tps == null) return -1;
        return Math.min(Tools.round(tps.poll(StatisticWindow.TicksPerSecond.SECONDS_10), 2), 20); // 5 секунд изредка дропает тпс до 16, хз почему...
    }

    public static double getMSPT() {
        if (getPlugin() == null || Platform.get() != Platform.BUKKIT) return -1;
        GenericStatistic<DoubleAverageInfo, StatisticWindow.MillisPerTick> mspt = getPlugin().mspt();
        if (mspt == null) return -1;
        return Tools.round(mspt.poll(StatisticWindow.MillisPerTick.SECONDS_10).mean(), 2);
    }
}