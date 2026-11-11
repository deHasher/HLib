package net.dehasher.hlib.data;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import net.dehasher.hlib.Tools;

// Attribute незя тут обработать из-за несоответствия классов...
public class NMS {
	public static final String VERSION;

	static {
		VERSION = detectNMSVersion();
	}

	public static class Packets {
		public static final int END_PORTAL_CREATION_EVENT_ID = 1038;
		public static final int ARMOR_STAND_META_INDEX = 15;
	}

	public static Sound getSound(String name) {
		if (name == null) return null;

		NamespacedKey key = NamespacedKey.fromString(name.toLowerCase());
		if (key != null) {
			Sound sound = Registry.SOUNDS.get(key);
			if (sound != null) return sound;
		}

		try {
			return (Sound) Sound.class.getField(name.toUpperCase().replace(".", "_")).get(null);
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}

	private static String detectNMSVersion() {
		String version = Tools.getBukkitVersionAsMinor().replace(".", "_") + "_R";
		for (int r = 1; r <= 20; r++) {
			String candidate = "org.bukkit.craftbukkit.v" + version + r + ".CraftWorld";
			try {
				Class.forName(candidate);
				return version + r;
			} catch (ClassNotFoundException ignore) {}
		}
		return "UNKNOWN";
	}
}