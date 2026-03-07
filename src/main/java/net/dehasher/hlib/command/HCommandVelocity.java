package net.dehasher.hlib.command;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import lombok.Getter;
import lombok.Setter;
import net.dehasher.hlib.Cooldowner;
import net.dehasher.hlib.DateTimeFormatter;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.config.Info;
import net.dehasher.hlib.controller.StorageController;
import net.dehasher.hlib.data.Permission;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Setter
@Getter
public abstract class HCommandVelocity implements SimpleCommand {
    private String commandName;
    private String permission;
    private int cooldown;
    private int limit;
    private final Map<String, Integer> count = new ConcurrentHashMap<>();
    private final List<String> aliases;
    @Getter
    private static Set<HCommandVelocity> registeredCommands = ConcurrentHashMap.newKeySet();

    protected HCommandVelocity(Object plugin, String name, List<String> aliases, int cooldown, int limit) {
        this.aliases = aliases
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        registeredCommands.add(this);

        setCommandName(name);
        setPermission(Tools.join(".command.", plugin.getClass().getSimpleName().toLowerCase().replace("loader", ""), getCommandName().toLowerCase()));
        setCooldown(cooldown);
        setLimit(limit);
    }

    @SuppressWarnings("DuplicatedCode")
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String alias = invocation.alias();
        String[] arguments = invocation.arguments();

        String key = getSourceName(source);
        getCount().putIfAbsent(key, 0);
        int count = getCount().get(key);

        if (!Tools.isPerm(source, Permission.getEnum(getPermission()))) {
            Informer.send(source, StorageController.getNoPermMessage());
            return;
        }

        if (isPlayer(source)) {
            if (Cooldowner.inCooldown(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND) && !Tools.isPerm(source, Permission.HCORE_BYPASS_COOLDOWN_COMMAND)) {
                Informer.send(source, StorageController.getCommandCooldownMessage()
                        .replace("{time}", DateTimeFormatter.format(Cooldowner.getTimeLeft(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND))));
                return;
            }

            if (getLimit() > 0 && count >= getLimit() && !Tools.isPerm(source, Permission.HCORE_BYPASS_LIMIT_COMMAND)) {
                Informer.send(source, StorageController.getCommandLimitMessage());
                return;
            }
        }

        if (run(source, alias, arguments)) {
            if (getLimit() > 0) getCount().merge(key, 1, Integer::sum);
            if (getCooldown() > 0 && !Tools.isPerm(invocation.source(), Permission.HCORE_BYPASS_COOLDOWN_COMMAND)) {
                Cooldowner.start(Cooldowner.key(key, getPermission()), Cooldowner.Type.COMMAND, getCooldown());
            }
        }
    }



    protected abstract boolean run(CommandSource source, String alias, String[] arguments);
    protected abstract List<String> tab(CommandSource source, String alias, String[] arguments);

    @Override
    public List<String> suggest(Invocation invocation) {
        return tab(invocation.source(), invocation.alias(), invocation.arguments());
    }

    protected boolean isPlayer(CommandSource source) {
        return source instanceof Player;
    }

    protected String getSourceName(CommandSource source) {
        return source instanceof Player player ? player.getUsername() : Info.consoleName;
    }
}