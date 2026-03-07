package net.dehasher.hlib;

import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.data.Platform;
import net.dehasher.hlib.platform.velocity.HLib;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Scheduler {
    @Getter
    private static final Set<Integer> tasks = ConcurrentHashMap.newKeySet();

    public static class Bukkit {
        public static void doAsync(Runnable runnable) {
            org.bukkit.Bukkit.getScheduler().runTaskAsynchronously(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable);
        }

        public static void doAsyncLater(Runnable runnable, long delay) {
            org.bukkit.Bukkit.getScheduler().runTaskLaterAsynchronously(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable, delay);
        }

        public static int doAsyncLaterWithId(Runnable runnable, long delay) {
            int id = org.bukkit.Bukkit.getScheduler().runTaskLaterAsynchronously(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable, delay).getTaskId();
            getTasks().add(id);
            return id;
        }

        public static void doAsyncRepeat(Runnable runnable, long delay, long period) {
            org.bukkit.Bukkit.getScheduler().runTaskTimerAsynchronously(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable, delay, period);
        }

        public static int doAsyncRepeatWithId(Runnable runnable, long delay, long period) {
            int id = org.bukkit.Bukkit.getScheduler().runTaskTimerAsynchronously(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable, delay, period).getTaskId();
            getTasks().add(id);
            return id;
        }

        public static void doSync(Runnable runnable) {
            org.bukkit.Bukkit.getScheduler().runTask(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable);
        }

        public static void doSyncLater(Runnable runnable, long delay) {
            org.bukkit.Bukkit.getScheduler().runTaskLater(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable, delay);
        }

        public static int doSyncLaterWithId(Runnable runnable, long delay) {
            int id = org.bukkit.Bukkit.getScheduler().runTaskLater(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable, delay).getTaskId();
            getTasks().add(id);
            return id;
        }

        public static void doSyncRepeat(Runnable runnable, long delay, long period) {
            org.bukkit.Bukkit.getScheduler().runTaskTimer(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable, delay, period);
        }

        public static int doSyncRepeatWithId(Runnable runnable, long delay, long period) {
            int id = org.bukkit.Bukkit.getScheduler().runTaskTimer(net.dehasher.hlib.platform.bukkit.HLib.getInstance(), runnable, delay, period).getTaskId();
            getTasks().add(id);
            return id;
        }

        public static void stop(Integer id) {
            if (id == null || !getTasks().contains(id)) return;
            org.bukkit.Bukkit.getScheduler().cancelTask(id);
            getTasks().remove(id);
        }
    }

    public static class Velocity {
        @Getter
        private static final Map<Integer, ScheduledTask> storage = new ConcurrentHashMap<>();
        @Getter
        @Setter
        private static int last = 0;

        public static void doAsync(Runnable runnable) {
            HLib.getProxy().getScheduler().buildTask(HLib.getInstance(), runnable).schedule();
        }

        public static void doAsyncLater(Runnable runnable, long delay) {
            HLib.getProxy().getScheduler().buildTask(HLib.getInstance(), runnable).delay(delay * 50, TimeUnit.MILLISECONDS).schedule();
        }

        public static int doAsyncLaterWithId(Runnable runnable, long delay) {
            setLast(getLast() + 1);
            getStorage().put(getLast(), HLib.getProxy().getScheduler().buildTask(HLib.getInstance(), runnable).delay(delay * 50, TimeUnit.MILLISECONDS).schedule());
            getTasks().add(getLast());
            return getLast();
        }

        public static void doAsyncRepeat(Runnable runnable, long delay, long period) {
            HLib.getProxy().getScheduler().buildTask(HLib.getInstance(), runnable).delay(delay * 50, TimeUnit.MILLISECONDS).repeat(period * 50, TimeUnit.MILLISECONDS).schedule();
        }

        public static int doAsyncRepeatWithId(Runnable runnable, long delay, long period) {
            setLast(getLast() + 1);
            getStorage().put(getLast(), HLib.getProxy().getScheduler().buildTask(HLib.getInstance(), runnable).delay(delay * 50, TimeUnit.MILLISECONDS).repeat(period * 50, TimeUnit.MILLISECONDS).schedule());
            getTasks().add(getLast());
            return getLast();
        }

        public static void stop(Integer id) {
            if (id == null || !getStorage().containsKey(id)) return;
            getStorage().get(id).cancel();
            getStorage().remove(id);
            getTasks().remove(id);
        }
    }

    public static void doAsync(Runnable runnable) {
        try {
            inject(Runnable.class).invoke(void.class, runnable);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void doAsyncLater(Runnable runnable, long delay) {
        try {
            inject(Runnable.class, long.class).invoke(void.class, runnable, delay);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static int doAsyncLaterWithId(Runnable runnable, long delay) {
        try {
            return (int) inject(Runnable.class, long.class).invoke(int.class, runnable, delay);
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public static void doAsyncRepeat(Runnable runnable, long delay, long period) {
        try {
            inject(Runnable.class, long.class, long.class).invoke(void.class, runnable, delay, period);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static int doAsyncRepeatWithId(Runnable runnable, long delay, long period) {
        try {
            return (int) inject(Runnable.class, long.class, long.class).invoke(int.class, runnable, delay, period);
        } catch (Throwable t) {
            t.printStackTrace();
            return 0;
        }
    }

    public static void stop(Integer id) {
        try {
            inject(Integer.class).invoke(void.class, id);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SuppressWarnings("DataFlowIssue")
    private static Method inject(Class<?>... parametrs) throws NoSuchMethodException {
        return Stream.of(Scheduler.class.getDeclaredClasses())
                .filter(clazz -> clazz.getSimpleName().equalsIgnoreCase(Platform.get().name())).findFirst().orElse(null)
                .getDeclaredMethod(Thread.currentThread().getStackTrace()[2].getMethodName(), parametrs);
    }

    public static boolean contains(Integer id) {
        if (id == null) return false;
        return getTasks().contains(id);
    }
}