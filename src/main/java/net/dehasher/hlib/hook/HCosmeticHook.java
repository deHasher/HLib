package net.dehasher.hlib.hook;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public final class HCosmeticHook {
	public static Provider getProvider() {
		RegisteredServiceProvider<Provider> registration = Bukkit.getServicesManager().getRegistration(Provider.class);
		return registration == null || !registration.getPlugin().isEnabled() ? null : registration.getProvider();
	}

	public static boolean isAvailable() {
		return getProvider() != null;
	}

	public static void unequipGadget(Player player) {
		Provider provider = getProvider();
		if (provider != null) provider.unequipGadget(player);
	}

	public static void unequipSuit(Player player) {
		Provider provider = getProvider();
		if (provider != null) provider.unequipSuit(player);
	}

	public interface Provider {
		void unequipGadget(Player player);
		void unequipSuit(Player player);
	}
}