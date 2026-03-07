package net.dehasher.hlib.hook;

import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldEditHook {
    public static WorldEditPlugin getPlugin() {
        return JavaPlugin.getPlugin(WorldEditPlugin.class);
    }

    public static Region getSelection(Player player) throws IncompleteRegionException {
        return getPlugin().getSession(player).getSelection(BukkitAdapter.adapt(player.getWorld()));
    }

    public static void expandVert(Player player) {
        LocalSession session = getPlugin().getSession(player);
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(player.getWorld());
        try {
            Region region = session.getSelection(weWorld);
            region.expand(BlockVector3.at(0, (weWorld.getMaxY() + 1), 0), BlockVector3.at(0, -(weWorld.getMaxY() + 1), 0));
            session.getRegionSelector(weWorld).learnChanges();
        } catch (Throwable ignored) {}
    }
}