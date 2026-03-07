package net.dehasher.hlib.hook;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook {
    private static final RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager().getRegistration(Economy.class);

    public static Economy getPlugin() {
        if (economyProvider == null) return null;
        return economyProvider.getProvider();
    }
}