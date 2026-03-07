package net.dehasher.hlib.hook;

import me.neznamy.tab.api.TabPlayer;
import me.neznamy.tab.shared.TAB;
import net.dehasher.hlib.Scheduler;
import net.dehasher.hlib.data.Plugin;
import org.bukkit.entity.Player;
import java.util.Objects;

public class TABHook {
    private static final String DEFAULT = "_DEFAULT_";
    private static final String ANIMATED = "%animation:";

    public static TAB getPlugin() {
        return TAB.getInstance();
    }

    public static TabPlayer getPlayer(Player player) {
        if (player == null) return null;
        return getPlugin().getPlayer(player.getUniqueId());
    }

    public static TabPlayer getPlayer(String player) {
        if (player == null) return null;
        return getPlugin().getPlayer(player);
    }

    public static void setPrefix(Player player, String string) {
        setPrefix(player, string, true);
    }

    public static void setPrefix(Player player, String string, boolean placeholder) {
        setTabPrefix(player, string);
        setTagPrefix(player, string, placeholder);
    }

    public static void setSuffix(Player player, String string) {
        setTabSuffix(player, string);
        setTagSuffix(player, string);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void setTabPrefix(Player player, String string) {
        Scheduler.Bukkit.doSync(() -> {
            TabPlayer tabPlayer = getPlayer(player);
            if (tabPlayer == null) return;
            Type type = Type.TABPREFIX;
            getPlugin().getTabListFormatManager().setPrefix(tabPlayer, string == null ? getTabGroup(LuckPermsHook.getUserGroup(player), type) : string);
            save(player, type, string);
        });
    }

    public static void setTagPrefix(Player player, String string) {
        setTagPrefix(player, string, true);
    }

    @SuppressWarnings("DataFlowIssue")
    public static void setTagPrefix(Player player, String string, boolean placeholder) {
        if (placeholder && Plugin.CMI.isEnabled() && string != null) string += CMIHook.GLOW_CODE_PLACEHOLDER;
        String finalString = string;
        Scheduler.Bukkit.doSync(() -> {
            TabPlayer tabPlayer = getPlayer(player);
            if (tabPlayer == null) return;
            Type type = Type.TAGPREFIX;
            getPlugin().getNameTagManager().setPrefix(tabPlayer, finalString == null ? getTabGroup(LuckPermsHook.getUserGroup(player), type) : finalString);
            save(player, type, finalString);
        });
    }

    @SuppressWarnings("DataFlowIssue")
    public static void setTabSuffix(Player player, String string) {
        Scheduler.Bukkit.doSync(() -> {
            TabPlayer tabPlayer = getPlayer(player);
            if (tabPlayer == null) return;
            Type type = Type.TABSUFFIX;
            getPlugin().getTabListFormatManager().setSuffix(tabPlayer, string == null ? getTabGroup(LuckPermsHook.getUserGroup(player), type) : string);
            save(player, type, string);
        });
    }

    @SuppressWarnings("DataFlowIssue")
    public static void setTagSuffix(Player player, String string) {
        Scheduler.Bukkit.doSync(() -> {
            TabPlayer tabPlayer = getPlayer(player);
            if (tabPlayer == null) return;
            Type type = Type.TAGSUFFIX;
            getPlugin().getNameTagManager().setSuffix(tabPlayer, string == null ? getTabGroup(LuckPermsHook.getUserGroup(player), type) : string);
            save(player, type, string);
        });
    }

    @SuppressWarnings({"DataFlowIssue", "deprecation"})
    public static boolean isAnimated(Player player) {
        TabPlayer tabPlayer = getPlayer(player);
        if (tabPlayer == null) return false;
        return Objects.toString(getPlugin().getNameTagManager().getCustomPrefix(tabPlayer), "").contains(ANIMATED) ||
               Objects.toString(getPlugin().getNameTagManager().getCustomSuffix(tabPlayer), "").contains(ANIMATED) ||

               Objects.toString(getPlugin().getNameTagManager().getOriginalPrefix(tabPlayer), "").contains(ANIMATED) ||
               Objects.toString(getPlugin().getNameTagManager().getOriginalSuffix(tabPlayer), "").contains(ANIMATED) ||

               Objects.toString(getPlugin().getTabListFormatManager().getCustomPrefix(tabPlayer), "").contains(ANIMATED) ||
               Objects.toString(getPlugin().getTabListFormatManager().getCustomSuffix(tabPlayer), "").contains(ANIMATED) ||

               Objects.toString(getPlugin().getTabListFormatManager().getOriginalPrefix(tabPlayer), "").contains(ANIMATED) ||
               Objects.toString(getPlugin().getTabListFormatManager().getOriginalSuffix(tabPlayer), "").contains(ANIMATED);
    }

    public static void save(Player player, Type type, String string) {
        if (player == null) return;
        getPlugin().getConfiguration().getUsers().setProperty(player.getName(), type.name().toLowerCase(), null, null, string);
    }

    public static String getTabGroup(String group, Type type) { // Мини костыль до перезахода игрока. Сохранять в конфигурацию это не следует.
        group = group.replace("default", DEFAULT);
        String[] property = getPlugin().getConfiguration().getGroups().getProperty(group, type.name().toLowerCase(), null, null);
        if (property.length == 0) property = getPlugin().getConfiguration().getGroups().getProperty(DEFAULT, type.name().toLowerCase(), null, null);
        if (property.length == 0) return null;
        return property[0];
    }

    public enum Type {
        TABPREFIX, TAGPREFIX,
        TABSUFFIX, TAGSUFFIX
    }
}