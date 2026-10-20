package net.dehasher.hlib.command;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class HCommandPreflight {
	public static Prepared prepare(CommandSender sender, List<String> commands) {
		requireMainThread();
		Objects.requireNonNull(sender);
		List<Invocation> invocations = Lists.newArrayList();
		List.copyOf(commands).forEach(configured -> {
			String line = configured.trim();
			if (line.startsWith("/")) line = line.substring(1);
			String[] parts = line.split(" ");
			String label = parts[0].toLowerCase(Locale.ROOT);
			invocations.add(new Invocation(label.isEmpty() ? null : Bukkit.getCommandMap().getCommand(label), label, List.copyOf(List.of(parts).subList(1, parts.length))));
		});
		return new Prepared(sender, List.copyOf(invocations));
	}

	private static void requireMainThread() {
		if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Command preflight requires the main thread");
	}

	public enum Result {
		ALLOWED, DENIED, UNAVAILABLE
	}

	@Getter(AccessLevel.PRIVATE)
	public static final class Prepared {
		private final CommandSender sender;
		private final List<Invocation> invocations;

		private Prepared(CommandSender sender, List<Invocation> invocations) {
			this.sender = sender;
			this.invocations = invocations;
		}

		public Result check() {
			requireMainThread();
			if (getInvocations().isEmpty()) return Result.UNAVAILABLE;
			if (getInvocations().stream().anyMatch(invocation -> invocation.command() == null || invocation.command() != Bukkit.getCommandMap().getCommand(invocation.label()) || invocation.command() instanceof PluginIdentifiableCommand identified && !identified.getPlugin().isEnabled())) return Result.UNAVAILABLE;
			return getInvocations().stream().allMatch(invocation -> !(invocation.command() instanceof HCommandBukkit command) || command.canExecute(getSender(), invocation.label(), invocation.arguments().toArray(String[]::new))) ? Result.ALLOWED : Result.DENIED;
		}
	}

	private record Invocation(Command command, String label, List<String> arguments) {}
}