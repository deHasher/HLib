package net.dehasher.hlib;

import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// Надстройка над Cooldowner'ом, после завершения кулдауна выполняется произвольный код.
@Getter
public class Requestor {
    private final String target;
    private final String sender;
    private final long time;
    private final TimeUnit timeUnit;
    private final int task;
    private final String key;
    private final Cooldowner.Type type;
    private final Runnable runnable;
    @Getter(AccessLevel.PRIVATE)
    private static final Map<String, Requestor> requests = new ConcurrentHashMap<>();

    // sender   - Тот, кто пригласил.
    // target   - Тот, кого пригласили.
    // runnable - Код, который будет выполнен, если запрос будет проигнорирован.
    private Requestor(String sender, String target, Cooldowner.Type type, long time, TimeUnit timeUnit, Runnable runnable) {
        this.sender   = sender;
        this.target   = target;
        this.time     = time;
        this.timeUnit = timeUnit;
        this.type     = type;
        this.runnable = runnable;
        this.key      = Cooldowner.key(sender, target, type.name());
        Cooldowner.start(key, type, time, timeUnit);
        getRequests().put(key, this);
        this.task = Scheduler.doAsyncLaterWithId(() -> {
            if (getRequests().get(key) != null) runnable.run();
            getRequests().remove(key);
        }, timeUnit.convert(time, TimeUnit.SECONDS) * 20L);
    }

    private static String getKey(String sender, String target, Cooldowner.Type type) {
        return Cooldowner.key(sender, target, type.name());
    }

    public void accept() {
        getRequests().remove(key);
        Scheduler.stop(task);
    }

    public void cancel() {
        accept();
        Scheduler.doAsync(runnable);
    }

    public static Requestor createRequest(Player sender, Player target, Cooldowner.Type type, long time, TimeUnit timeUnit, Runnable runnable) {
        return createRequest(sender.getName(), target.getName(), type, time, timeUnit, runnable);
    }

    public static Requestor createRequest(String sender, String target, Cooldowner.Type type, long time, TimeUnit timeUnit, Runnable runnable) {
        Requestor request = getRequest(sender, target, type);
        if (request != null) return request;
        String key = getKey(sender, target, type);
        getRequests().put(key, new Requestor(sender, target, type, time, timeUnit, runnable));
        return getRequests().get(key);
    }

    public static Requestor getRequest(Player sender, Player target, Cooldowner.Type type) throws NullPointerException {
        return getRequest(sender.getName(), target.getName(), type);
    }

    public static Requestor getRequest(String sender, String target, Cooldowner.Type type) throws NullPointerException {
        return getRequests().get(getKey(sender, target, type));
    }

    public static Set<Requestor> getRequests(Cooldowner.Type type) {
        return getRequests().values()
                .stream()
                .filter(request -> request.getType() == type)
                .collect(Collectors.toSet());
    }
}