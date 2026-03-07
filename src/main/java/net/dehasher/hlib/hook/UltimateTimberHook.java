package net.dehasher.hlib.hook;

import com.craftaro.ultimatetimber.UltimateTimber;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;

public class UltimateTimberHook {
    public static UltimateTimber getPlugin() {
        return UltimateTimber.getPlugin(UltimateTimber.class);
    }

    public static boolean isBlockInAnimation(FallingBlock fallingBlock) {
        return getPlugin().getTreeAnimationManager().isBlockInAnimation(fallingBlock);
    }

    public static boolean isBlockInAnimation(Entity fallingBlock) {
        if (!(fallingBlock instanceof FallingBlock)) return false;
        return isBlockInAnimation((FallingBlock) fallingBlock);
    }
}