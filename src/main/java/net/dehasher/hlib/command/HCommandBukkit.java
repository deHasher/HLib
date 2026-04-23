package net.dehasher.hlib.command;

import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.*;
import net.dehasher.hlib.config.Info;
import net.dehasher.hlib.controller.StorageController;
import net.dehasher.hlib.data.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Getter
@Setter
public abstract class HCommandBukkit extends BukkitCommand {
    private String commandName;
    private String permission;
    private int cooldown;
    private int limit;
    private final Map<String, Integer> count = new ConcurrentHashMap<>();
    @Getter
    private static Set<HCommandBukkit> registeredCommands = ConcurrentHashMap.newKeySet();

    protected HCommandBukkit(Object plugin, String name, List<String> aliases, int cooldown, int limit) {
        super(name);
        setCommandName(name);
        setPermission(Tools.join(".command.", plugin.getClass().getSimpleName().toLowerCase().replace("loader", ""), getCommandName().toLowerCase()));

        Set<String> rus = ConcurrentHashMap.newKeySet();
        if (!aliases.isEmpty()) rus = aliases.stream()
                .filter(alias -> !Tools.isCyrillic(alias))
                .map(Translator::toRussianKeymap)
                .collect(Collectors.toSet());
        rus.add(Translator.toRussianKeymap(name));

        aliases.addAll(rus);
        setAliases(aliases
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList()));

        setCooldown(cooldown);
        setLimit(limit);

        registeredCommands.add(this);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public final boolean execute(@NonNull CommandSender sender, @NonNull String alias, String[] arguments) {
        String key = getSenderName(sender);
        getCount().putIfAbsent(key, 0);
        int count = getCount().get(key);

        if (!Tools.isPerm(sender, Permission.getEnum(getPermission()))) {
            Informer.send(sender, StorageController.getNoPermMessage());
            return false;
        }

        if (isPlayer(sender)) {
            if (Cooldowner.inCooldown(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND) && !Tools.isPerm(sender, Permission.HCORE_BYPASS_COOLDOWN_COMMAND)) {
                Informer.send(sender, StorageController.getCommandCooldownMessage()
                        .replace("{time}", DateTimeFormatter.format(Cooldowner.getTimeLeft(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND))));
                return false;
            }

            if (getLimit() > 0 && count >= getLimit() && !Tools.isPerm(sender, Permission.HCORE_BYPASS_LIMIT_COMMAND)) {
                Informer.send(sender, StorageController.getCommandLimitMessage());
                return false;
            }
        }

        if (run(sender, alias, arguments)) {
            if (getLimit() > 0) getCount().merge(key, 1, Integer::sum);
            if (getCooldown() > 0 && !Tools.isPerm(sender, Permission.HCORE_BYPASS_COOLDOWN_COMMAND)) {
                Cooldowner.start(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND, getCooldown());
            }
        }

        return true;
    }

    protected abstract boolean run(CommandSender sender, String alias, String[] arguments);
    protected abstract List<String> tab(CommandSender sender, String alias, String[] arguments);

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandSender sender, @NonNull String alias, String[] arguments) throws IllegalArgumentException {
        return tab(sender, alias, arguments);
    }

    protected boolean isPlayer(CommandSender sender) {
        return sender instanceof Player;
    }

    protected String getSenderName(CommandSender sender) {
        return sender instanceof Player ? sender.getName() : Info.consoleName;
    }
}