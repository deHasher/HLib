package net.dehasher.hlib.hook;

import me.clip.placeholderapi.PlaceholderAPI;
import net.dehasher.hlib.data.Plugin;
import org.bukkit.entity.Player;
import java.util.List;

public class PlaceholderAPIHook {
	public static String setPlaceholders(Player player, String input) {
		if (!Plugin.PLACEHOLDER_API.isEnabled()) return input;
		return PlaceholderAPI.setPlaceholders(player, input);
	}

	public static List<String> setPlaceholders(Player player, List<String> input) {
		if (!Plugin.PLACEHOLDER_API.isEnabled()) return input;
		return PlaceholderAPI.setPlaceholders(player, input);
	}
}