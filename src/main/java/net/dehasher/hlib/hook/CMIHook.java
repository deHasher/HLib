package net.dehasher.hlib.hook;

import com.Zrips.CMI.CMI;
import com.Zrips.CMI.Containers.CMIUser;
import com.Zrips.CMI.Containers.CommandAlias;
import com.Zrips.CMI.Modules.Vanish.VanishManager;
import net.Zrips.CMILib.CMILib;
import net.Zrips.CMILib.Colors.CMICustomColors;
import net.Zrips.CMILib.Items.CMIMaterial;
import net.dehasher.hlib.Scheduler;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.data.Permission;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class CMIHook {
    public static final String COMMAND_PREFIX = "cmi ";
    public static final String GLOW_CODE_PLACEHOLDER = "%cmi_user_glow_code%";

    public static CMI getPlugin() {
        return CMI.getInstance();
    }

    public static CMILib getCMILib() {
        return CMILib.getInstance();
    }

    public static CMIUser getUser(Player player) {
        if (player == null || !player.isOnline()) return null;
        return getUser(player.getName());
    }

    public static CMIUser getUser(String player) {
        return getPlugin().getPlayerManager().getUser(player);
    }

    public static Player getPlayer(String player) {
        CMIUser user = getPlugin().getPlayerManager().getUser(player);
        return user != null ? user.getPlayer() : null;
    }

    public static String getCustomNickname(Player player) {
        return getCustomNickname(player.getName());
    }

    public static String getCustomNickname(String player) {
        CMIUser user = getUser(player);
        if (user == null) return player;
        String customName = user.getNickName();
        if (customName == null) return player;
        return customName;
    }

    public static void offGodMode(Player player) {
        CMIUser user = getUser(player);
        if (user == null || !CMI.getInstance().getNMS().getGodMode(user.getPlayer())) return;
        getPlugin().getNMS().changeGodMode(player, false);
    }

    public static void offVanish(Player player) {
        CMIUser user = getUser(player);
        if (user == null || !user.isVanished()) return;
        Scheduler.Bukkit.doSync(() -> {
            user.setVanished(false);
            getPlugin().getVanishManager().removePlayer(player);
        });
    }

    public static boolean isVanished(Player player) {
        CMIUser user = getUser(player);
        if (user == null) return false;
        return user.isVanished();
    }

    public static void offSpeed(Player player) {
        CMIUser user = getUser(player);
        if (user == null) return;
        if (user.getPlayer().getWalkSpeed() > 0.2) user.getPlayer().setWalkSpeed((float) 0.2);
        if (user.getPlayer().getFlySpeed()  > 0.1) user.getPlayer().setFlySpeed((float) 0.1);
    }

    public static float getWalkSpeed(Player player) {
        CMIUser user = getUser(player);
        if (user == null) return -1;
        return user.getPlayer().getWalkSpeed() / 2;
    }

    public static float getFlySpeed(Player player) {
        CMIUser user = getUser(player);
        if (user == null) return -1;
        return user.getPlayer().getFlySpeed();
    }

    public static boolean isOverdrawn(Player player, int limit) {
        int walk = (int) Tools.ceil(getWalkSpeed(player) * 10, 0);
        int fly  = (int) Tools.ceil(getFlySpeed(player)  * 10, 0);
        return checkOverdrawn(player, Permission.CMI_COMMAND_WALKSPEED, walk, limit) || checkOverdrawn(player, Permission.CMI_COMMAND_FLYSPEED, fly, limit);
    }

    private static boolean checkOverdrawn(Player player, Permission permission, int speed, int limit) {
        if (speed < limit) return false;
        for (int i = speed; i <= 10; i++) {
            if (Tools.isPerm(player, permission, i)) return false;
        }
        return true;
    }

    public static void clearRequests(Player player) {
        getPlugin().getTeleportManager().clearRequests(player);
    }

    public static int getPlayerMeta(Player player, String meta) {
        if (getUser(player) == null) return 0;
        String data = getUser(player).getMeta(meta);
        if (data == null || data.isEmpty()) return 0;
        return Tools.parseInt(data);
    }

    public static void setPlayerMeta(Player player, String meta, String value) {
        if (getUser(player) == null) return;
        getUser(player).getMeta().add(meta, value);
    }

    public static long getPlayerTime(Player player) {
        if (getUser(player) == null) return 0;
        return getUser(player).getTotalPlayTime();
    }

    public static boolean isJailed(Player player) {
        if (getUser(player) == null) return false;
        return getUser(player).isJailed();
    }

    public static boolean isCuffed(Player player) {
        if (getUser(player) == null) return false;
        return getUser(player).isCuffed();
    }

    public static void fixColorMessage(PlayerCommandPreprocessEvent e) {
        List.of(CMICustomColors.values()).forEach(color -> {
            String name = color.name().toLowerCase().replace("_", "");
            if (e.getMessage().toLowerCase().contains("{#" + name)) {
                e.setMessage(e.getMessage().replaceAll("(?i)\\b" + name + "\\b", color.getHex()));
            }
        });
    }

    public static CommandAlias getAlias(String command) {
        return CMIHook.getPlugin().getAliasManager().getAll().get(command);
    }

    public static String getTranslatedMaterial(String name) {
        Material material = Material.matchMaterial(name);
        if (material == null) return name;
        return getTranslatedMaterial(material);
    }

    public static String getTranslatedMaterial(ItemStack itemStack) {
        return getTranslatedMaterial(itemStack.getType());
    }

    public static String getTranslatedMaterial(Material material) {
        CMIMaterial cmiMaterial = CMIMaterial.get(material);
        String name = cmiMaterial.getTranslatedName();
        return name.equalsIgnoreCase("none") ? material.name() : name;
    }

    public static VanishManager getVanishManager() {
        return getPlugin().getVanishManager();
    }

    public static int getOnlineVanished() {
        return getVanishManager().getVanishedOnlineList().size();
    }
}