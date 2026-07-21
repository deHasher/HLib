package net.dehasher.hlib.hook;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import net.dehasher.hlib.Informer;
import net.dehasher.hlib.data.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

public class ItemsAdderHook {
    public static boolean isCustomItem(ItemStack item) {
        return getCustomItem(item) != null;
    }

    @SuppressWarnings("UnreachableCode")
    public static CustomStack getCustomItem(ItemStack item) {
        CustomStack customStack = CustomStack.byItemStack(item);
        if (customStack == null) return null;
        return CustomStack.getInstance(customStack.getNamespacedID());
    }

    @SuppressWarnings({"UnreachableCode", "SimplifiableConditionalExpression"})
    public static CustomStack getCustomItem(String item) {
        CustomStack customStack = CustomStack.getInstance(item);
        return customStack != null ? customStack : null;
    }

    public static boolean hasPermission(CustomStack customStack) {
        if (customStack == null) return false;
        try {
            return customStack.hasPermission();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hasItemModel(CustomStack customStack) {
        if (customStack == null) return false;
        return !customStack.getItemStack().getItemMeta().hasCustomModelData();
    }

    public static boolean hasCustomModelData(CustomStack customStack) {
        if (customStack == null) return false;
        return customStack.getItemStack().getItemMeta().hasCustomModelData();
    }

    public static @Nullable Integer getCustomModelData(CustomStack customStack) {
        if (!hasCustomModelData(customStack)) return null;
        return customStack.getItemStack().getItemMeta().getCustomModelData();
    }

    public static @Nullable String getItemModel(CustomStack customStack) {
        if (!hasItemModel(customStack)) return null;
        return customStack.getNamespace() + ":ia_auto/" + customStack.getId();
    }

    @SuppressWarnings("UnreachableCode")
    public static String getPackUrl(boolean appendHash) {
        String url = ItemsAdder.getPackUrl(appendHash);
        if (url == null) return "";
        return url;
    }

    public static String getPackSha1() {
        org.bukkit.plugin.Plugin itemsAdder = Bukkit.getPluginManager().getPlugin(Plugin.ITEMS_ADDER.getName());
        if (itemsAdder == null) {
            Informer.send("ItemsAdder plugin not found!!!");
            return null;
        }

        Path path = itemsAdder.getDataFolder().toPath()
                .resolve("output")
                .resolve("generated.zip");

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            try (InputStream inputStream = Files.newInputStream(path)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = inputStream.read(buffer)) != -1) digest.update(buffer, 0, read);
            }

            return HexFormat.of().formatHex(digest.digest());
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}