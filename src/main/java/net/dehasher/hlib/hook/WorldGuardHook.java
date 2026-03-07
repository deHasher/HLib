package net.dehasher.hlib.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.BukkitWorldConfiguration;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class WorldGuardHook {
    public static final String GLOBAL = "__global__";
    private static RegionQuery REGION_QUERY;

    public static WorldGuard getPlugin() {
        return WorldGuard.getInstance();
    }

    public static RegionQuery getRegionQuery() {
        if (REGION_QUERY == null) REGION_QUERY = getPlugin().getPlatform().getRegionContainer().createQuery();
        return REGION_QUERY;
    }

    public static BukkitWorldConfiguration getWorldConfig(World world) {
        return (BukkitWorldConfiguration) getPlugin().getPlatform().getGlobalStateManager().get(BukkitAdapter.adapt(world));
    }

    public static BukkitWorldConfiguration getWorldConfig(Player player) {
        return getWorldConfig(player.getWorld());
    }

    public static RegionManager getRegionManager(World world) {
        return getPlugin().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
    }

    public static ProtectedRegion getRegion(Location location, String name) {
        return getRegionManager(location.getWorld()).getRegion(name);
    }

    public static ProtectedRegion getRegion(World world, String name) {
        return getRegionManager(world).getRegion(name);
    }

    public static Map<String, ProtectedRegion> getRegions(World world) {
        RegionManager regionManager = getRegionManager(world);
        if (regionManager == null) return Collections.emptyMap();
        return regionManager.getRegions();
    }

    public static LocalPlayer getPlayer(Player player) {
        return WorldGuardPlugin.inst().wrapPlayer(player);
    }

    public static LocalPlayer getOfflinePlayer(OfflinePlayer player) {
        return WorldGuardPlugin.inst().wrapOfflinePlayer(player);
    }

    public static Set<ProtectedRegion> getApplicableRegions(Location location) {
        return getRegionQuery().getApplicableRegions(BukkitAdapter.adapt(location)).getRegions();
    }

    public static boolean inRegion(Location location, String name) {
        if (name == null || name.isEmpty() || name.equalsIgnoreCase(GLOBAL)) return false;
        return getApplicableRegions(location).stream().anyMatch(region -> region.getId().equalsIgnoreCase(name));
    }

    public static boolean inGlobal(Location loc) {
        return WorldGuardHook.getApplicableRegions(loc).isEmpty();
    }

    public static boolean inRegion(Location loc) {
        return !WorldGuardHook.getApplicableRegions(loc).isEmpty();
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean canBuild(Player player, Location location) {
        return isFlagAllows(player, location, Flags.BUILD);
    }

    public static boolean isFlagAllows(Player player, Location location, StateFlag flag) {
        return getRegionQuery().testState(BukkitAdapter.adapt(location), player != null ? WorldGuardPlugin.inst().wrapPlayer(player) : null, flag);
    }

    public static void registerFlag(Flag<?> flag) throws FlagConflictException {
        getPlugin().getFlagRegistry().register(flag);
    }

    public static boolean isOwner(ProtectedRegion region, Player player) {
        return region.isOwner(getPlayer(player));
    }

    public static boolean isMember(ProtectedRegion region, Player player) {
        return region.isMember(getPlayer(player));
    }

    public static boolean compareLocationRegions(Location from, Location to) {
        return WorldGuardHook.getApplicableRegions(from).equals(WorldGuardHook.getApplicableRegions(to));
    }

    public static boolean canBypassProtection(Player player) {
        return getPlugin().getPlatform().getSessionManager().hasBypass(getPlayer(player), BukkitAdapter.adapt(player.getWorld()));
    }

    public static Set<ProtectedRegion> getPlayerRegions(Player player) {
        RegionManager regionManager = getRegionManager(player.getWorld());
        if (regionManager == null) return Collections.emptySet();
        LocalPlayer localPlayer = getPlayer(player);
        return regionManager.getRegions().values()
                .stream()
                .filter(Objects::nonNull)
                .filter(region -> !region.getId().equalsIgnoreCase(GLOBAL))
                .filter(region -> region.isOwner(localPlayer) || region.isMember(localPlayer))
                .collect(Collectors.toSet());
    }
}