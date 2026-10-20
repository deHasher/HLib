package net.dehasher.hlib.hook;

import net.dehasher.hlib.data.HPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import java.util.concurrent.CompletableFuture;

public final class HTokensHook {
	public static Provider getProvider() {
		RegisteredServiceProvider<Provider> registration = Bukkit.getServicesManager().getRegistration(Provider.class);
		return registration == null || !registration.getPlugin().isEnabled() ? null : registration.getProvider();
	}

	public static boolean isAvailable() {
		return getProvider() != null;
	}

	public static CompletableFuture<Long> getBalance(HPlayer player) {
		return getBalance(player.name());
	}

	public static CompletableFuture<Long> getBalance(Player player) {
		return getBalance(player.getName());
	}

	public static CompletableFuture<Long> getBalance(String player) {
		Provider provider = getProvider();
		return provider == null ? CompletableFuture.failedFuture(new IllegalStateException("HTokens is not available")) : provider.getBalance(player);
	}

	public static CompletableFuture<Boolean> withdraw(Player player, long amount) {
		Provider provider = getProvider();
		return provider == null ? CompletableFuture.completedFuture(false) : provider.withdraw(player, amount);
	}

	public static CompletableFuture<Boolean> deposit(Player player, long amount) {
		Provider provider = getProvider();
		return provider == null ? CompletableFuture.completedFuture(false) : provider.deposit(player, amount);
	}

	public interface Provider {
		CompletableFuture<Long> getBalance(String player);
		CompletableFuture<Boolean> withdraw(Player player, long amount);
		CompletableFuture<Boolean> deposit(Player player, long amount);
	}
}