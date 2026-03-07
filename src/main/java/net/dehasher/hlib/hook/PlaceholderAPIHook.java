package net.dehasher.hlib.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook {
    public static String setPlaceholders(Player player, String input) {
        return PlaceholderAPI.setPlaceholders(player, input);
    }
}