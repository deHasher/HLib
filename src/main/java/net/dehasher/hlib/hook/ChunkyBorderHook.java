package net.dehasher.hlib.hook;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.dehasher.hlib.data.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import net.dehasher.hlib.Tools;
import java.io.*;

public class ChunkyBorderHook {
    public static org.bukkit.plugin.Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(Plugin.CHUNKY_BORDER.getName());
    }

    public static JsonObject getData(String world) {
        String json = Tools.readFile(new File(getPlugin().getDataFolder(), "borders.json"));
        if (json == null || json.isEmpty() || json.equals("{}")) return null;
        JsonObject jsonObject = Tools.getGSON().fromJson(json, JsonObject.class);
        if (jsonObject == null || !jsonObject.has(world)) return null;
        JsonElement element = jsonObject.get(world);
        if (element == null || !element.isJsonObject()) return null;
        return element.getAsJsonObject();
    }

    public static JsonObject getData(World world) {
        if (world == null) return null;
        return getData(world.getName());
    }

    public static boolean exists(String world) {
        return getData(world) != null;
    }

    public static boolean exists(World world) {
        if (world == null) return false;
        return exists(world.getName());
    }

    public static Location getCenter(String world) {
        return getCenter(Bukkit.getWorld(world));
    }

    public static Location getCenter(World world) {
        if (world == null) return null;
        return new Location(world, getData(world).get("centerX").getAsDouble(), 0, getData(world).get("centerZ").getAsDouble());
    }

    @SuppressWarnings("DataFlowIssue")
    public static int getSize(String world) {
        if (getData(world).get("radiusX").getAsDouble() != getData(world).get("radiusZ").getAsDouble()) return -1;
        return (int) getData(world).get("radiusX").getAsDouble() * 2;
    }

    public static int getSize(World world) {
        if (world == null) return -1;
        return getSize(world.getName());
    }
}