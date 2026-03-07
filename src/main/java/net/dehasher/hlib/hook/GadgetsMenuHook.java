package net.dehasher.hlib.hook;

import com.yapzhenyie.GadgetsMenu.api.GadgetsMenuAPI;
import com.yapzhenyie.GadgetsMenu.player.PlayerManager;
import org.bukkit.entity.Player;

public class GadgetsMenuHook {
    public static PlayerManager getPlugin(Player player) {
        return GadgetsMenuAPI.getPlayerManager(player);
    }
}