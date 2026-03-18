package net.dehasher.hlib.hook;

import lombok.Getter;
import me.RockinChaos.itemjoin.api.ItemJoinAPI;
import org.bukkit.inventory.ItemStack;

public class ItemJoinHook {
    @Getter
    private static final ItemJoinAPI plugin = new ItemJoinAPI();

    public static boolean isCustomItem(ItemStack item) {
        return getPlugin().isCustom(item);
    }
}