package net.dehasher.hlib.hook;

import litebans.api.Database;
import litebans.api.Entry;
import org.bukkit.entity.Player;
import java.util.UUID;

public class LiteBansHook {
    public static Database getPlugin() {
        return Database.get();
    }

    public static boolean isPlayerBanned(Player player, String ip) {
        UUID uuid = player != null ? player.getUniqueId() : null;
        return getPlugin().isPlayerBanned(uuid, ip);
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isPlayerMuted(Player player, String ip) {
        UUID uuid = player != null ? player.getUniqueId() : null;
        return getPlugin().isPlayerMuted(uuid, ip);
    }

    public static Entry getBan(Player player, String ip) {
        UUID uuid = player != null ? player.getUniqueId() : null;
        return getPlugin().getBan(uuid, ip, Database.ANY_SERVER_SCOPE);
    }

    public static Entry getMute(Player player, String ip) {
        UUID uuid = player != null ? player.getUniqueId() : null;
        return getPlugin().getMute(uuid, ip, Database.ANY_SERVER_SCOPE);
    }

    public static Entry getWarn(Player player, String ip) {
        UUID uuid = player != null ? player.getUniqueId() : null;
        return getPlugin().getWarning(uuid, ip, Database.ANY_SERVER_SCOPE);
    }

    public static Entry getKick(Player player, String ip) {
        UUID uuid = player != null ? player.getUniqueId() : null;
        return getPlugin().getKick(uuid, ip, Database.ANY_SERVER_SCOPE);
    }
}