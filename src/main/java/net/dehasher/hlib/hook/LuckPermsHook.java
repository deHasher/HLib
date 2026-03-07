package net.dehasher.hlib.hook;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.context.DefaultContextKeys;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.PrefixNode;
import net.luckperms.api.node.types.SuffixNode;
import net.luckperms.api.node.types.InheritanceNode;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.data.Permission;
import net.luckperms.api.query.QueryOptions;
import net.luckperms.api.util.Tristate;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class LuckPermsHook {
    private static final int PRIORITY = 1000;
    private static final int MAX_PERMISSION_LENGTH = 200;

    public static LuckPerms getPlugin() {
        return LuckPermsProvider.get();
    }

    public static int getMaxPermissionLength() {
        return MAX_PERMISSION_LENGTH;
    }

    public static int getChatMetaLength(String string) {
        // prefix и suffix имеют одинаковую длину.
        return Tools.join(".", "prefix", PRIORITY, string).length();
    }

    public static boolean isChatMetaTooLarge(String string) {
        return getChatMetaLength(string) > MAX_PERMISSION_LENGTH;
    }

    public static void setPrefix(org.bukkit.entity.Player player, String string) {
        modifyUser(player.getUniqueId(), user -> {
            user.data().clear(node -> node.getType().equals(NodeType.PREFIX));
            if (string != null) user.data().add(PrefixNode.builder(string, PRIORITY).build());
        });
    }

    public static void setSuffix(org.bukkit.entity.Player player, String string) {
        modifyUser(player.getUniqueId(), user -> {
            user.data().clear(node -> node.getType().equals(NodeType.SUFFIX));
            if (string != null) user.data().add(SuffixNode.builder(string, PRIORITY).build());
        });
    }

    public static void addPermission(org.bukkit.entity.Player player, Permission permission, boolean value) {
        addPermission(player.getUniqueId(), permission, value);
    }

    public static void addPermission(UUID uuid, Permission permission, boolean value) {
        getPlugin().getUserManager().modifyUser(uuid, user -> user.data().add(Node.builder(permission.getValue()).value(value).build()));
    }

    public static void addPermissionExpiry(UUID uuid, Permission permission, boolean value, long duration) {
        if (duration < 1) {
            addPermission(uuid, permission, value);
        } else {
            modifyUser(uuid, user -> user.data().add(Node.builder(permission.getValue()).value(value).expiry(duration, TimeUnit.MILLISECONDS).build()));
        }
    }

    public static void removePermission(org.bukkit.entity.Player player, Permission permission) {
        removePermission(player.getUniqueId(), permission);
    }

    public static void removePermission(UUID uuid, Permission permission) {
        modifyUser(uuid, user -> user.data().clear(node -> node.getKey().equalsIgnoreCase(permission.getValue())));
    }

    public static void modifyUser(UUID uuid, Consumer<? super User> action) {
        getPlugin().getUserManager().modifyUser(uuid, action);
    }

    public static TreeMap<String, GroupExpiryPair> getUserGroups(String player) { // SERVER:[GROUP & EXPIRY]
        TreeMap<String, GroupExpiryPair> data = new TreeMap<>();
        if (player == null) return data;

        try {
            User user = getPlugin().getUserManager().loadUser(Tools.offlineUUID(player)).get();
            if (user == null) return data;

            user.getNodes(NodeType.INHERITANCE).forEach(node -> {
                Optional<String> context = node.getContexts().getAnyValue(DefaultContextKeys.SERVER_KEY);
                if (context.isEmpty()) {
                    context = Optional.of("global");
                    if (node.getGroupName().equalsIgnoreCase("default")) return;
                }

                if (data.containsKey(context.get()) && getGroupWeight(data.get(context.get()).group()) > getGroupWeight(node.getGroupName())) return;
                data.put(context.get(), new GroupExpiryPair(node.getGroupName(), node.hasExpiry() ? node.getExpiryDuration() : null));
            });
        } catch (Throwable t) {
            t.printStackTrace();
            return data;
        }

        return data;
    }

    public static boolean hasPermission(String player, String permission) {
        return hasPermission(Tools.offlineUUID(player), permission);
    }

    public static boolean hasPermission(UUID uuid, Permission permission) {
        return hasPermission(uuid, permission.getValue());
    }

    public static boolean hasPermission(String player, Permission permission) {
        return hasPermission(Tools.offlineUUID(player), permission.getValue());
    }

    public static boolean hasPermission(UUID uuid, String permission) {
        try {
            User user = getPlugin().getUserManager().loadUser(uuid).join();
            if (user == null) return false;
            ImmutableContextSet staticContext = getPlugin().getContextManager().getStaticContext();
            QueryOptions qo = staticContext.isEmpty() ? QueryOptions.nonContextual() : QueryOptions.contextual(staticContext);
            CachedPermissionData data = user.getCachedData().getPermissionData(qo);
            Tristate result = data.checkPermission(permission);
            return result.asBoolean();
        } catch (Throwable t) {
            t.printStackTrace();
            return false;
        }
    }

    public static CompletableFuture<Boolean> hasPermissionAsync(String player, Permission permission) {
        return hasPermissionAsync(Tools.offlineUUID(player), permission.getValue());
    }

    public static CompletableFuture<Boolean> hasPermissionAsync(String player, String permission) {
        return hasPermissionAsync(Tools.offlineUUID(player), permission);
    }

    public static CompletableFuture<Boolean> hasPermissionAsync(UUID uuid, Permission permission) {
        return hasPermissionAsync(uuid, permission.getValue());
    }

    public static CompletableFuture<Boolean> hasPermissionAsync(UUID uuid, String permission) {
        QueryOptions queryOptions = getPlugin().getContextManager().getQueryOptions(getPlugin().getContextManager().getStaticContext());
        return getPlugin().getUserManager().loadUser(uuid).thenApply(user -> {
            if (user == null) return false;
            return user.getCachedData().getPermissionData(queryOptions).checkPermission(permission).asBoolean();
        });
    }

    public record GroupExpiryPair(String group, Duration duration) {}

    public static int getGroupWeight(String name) {
        Group group = getPlugin().getGroupManager().getGroup(name);
        if (group == null || group.getWeight().isEmpty()) return 0;
        return group.getWeight().getAsInt();
    }

    public static String getUserGroup(org.bukkit.entity.Player player) {
        @Nullable User user = getPlugin().getUserManager().getUser(player.getName());
        if (user == null) return "";
        return user.getNodes(NodeType.INHERITANCE).stream()
                .filter(n -> getPlugin().getContextManager().getQueryOptions(player).satisfies(n.getContexts()))
                .map(InheritanceNode::getGroupName)
                .map(n -> getPlugin().getGroupManager().getGroup(n))
                .filter(Objects::nonNull)
                .max(Comparator.comparingInt(g -> g.getWeight().orElse(0)))
                .map(Group::getName)
                .map(LuckPermsHook::convertGroupDisplayName)
                .orElse("");
    }

    private static String convertGroupDisplayName(String input) {
        if (input == null) return null;
        Group group = getPlugin().getGroupManager().getGroup(input);
        if (group != null) input = group.getFriendlyName();
        return input;
    }
}