package net.dehasher.hlib.command;

import lombok.Getter;
import net.dehasher.hlib.Scheduler;
import org.bukkit.entity.Player;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// Если переопределять тут методы, то и код, который в них находится нужно тоже копировать.
// Не относится к startAnimation(Player player) {}
@Getter
public abstract class HCommandAnimatedBukkit extends HCommandBukkit {
    private final Map<Player, Integer> animatedPlayers = new ConcurrentHashMap<>();
    private final Set<Player> remotePlayers = ConcurrentHashMap.newKeySet();
    @Getter
    private static final Set<HCommandAnimatedBukkit> commands = ConcurrentHashMap.newKeySet();

    public HCommandAnimatedBukkit(Object plugin, String name, List<String> aliases, int cooldown, int limit) {
        super(plugin, name, aliases, cooldown, limit);
        getCommands().add(this);
    }

    public void startAnimation(Player player) {}

    public void startAnimation(Player player, int remove) {
        startAnimation(player);
        if (remove == 0) return;
        getRemotePlayers().add(player);
        Scheduler.doAsyncLater(() -> getRemotePlayers().remove(player), 20L * remove);
    }

    public boolean inAnimation(Player player) {
        return getAnimatedPlayers().containsKey(player);
    }

    public void stopAnimation(Player player) {
        Scheduler.stop(getAnimatedPlayers().get(player));
        getAnimatedPlayers().remove(player);
    }

    public static boolean inAnyAnimation(Player player) {
        return getCommands().stream().anyMatch(command -> command.inAnimation(player));
    }

    public static void stopAllAnimation(Player player) {
        if (!inAnyAnimation(player)) return;
        getCommands().forEach(command -> command.stopAnimation(player));
    }
}