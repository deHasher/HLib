package net.dehasher.hlib.hook;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.LibsDisguises;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import org.bukkit.entity.Player;

public class LibsDisguisesHook {
    public static LibsDisguises getPlugin() {
        return LibsDisguises.getInstance();
    }

    public static void offDisguise(Player player) {
        DisguiseAPI.undisguiseToAll(player);
    }

    public static boolean isDisguised(Player player) {
        return DisguiseAPI.getDisguises(player).length != 0;
    }

    public static boolean isPlayerDisguise(Player player) {
        return DisguiseAPI.getDisguise(player).getType() == DisguiseType.PLAYER;
    }

    public static String getDisguiseName(Player player) {
        return DisguiseAPI.getDisguise(player).getDisguiseName();
    }
}