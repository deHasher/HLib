package net.dehasher.hlib.hook;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import net.dehasher.hlib.file.configuration.ConfigurationSection;
import net.dehasher.hlib.file.configuration.file.YamlConfiguration;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.Map;

public class NexoHook {
    public static boolean isCustomItem(ItemStack item) {
        return item != null && NexoItems.exists(item);
    }

    public static ItemBuilder getCustomItem(ItemStack item) {
        if (item == null) return null;
        return NexoItems.builderFromItem(item);
    }

    public static ItemBuilder getCustomItem(String item) {
        if (item == null) return null;
        return NexoItems.itemFromId(item);
    }

    public static boolean hasPermission(ItemBuilder customItem) {
        return getPermission(customItem) != null;
    }

    public static String getPermission(ItemBuilder customItem) {
        ConfigurationSection config = getItemConfig(customItem);
        return config.getString("permission");
    }

    public static boolean hasItemModel(ItemBuilder customItem) {
        return customItem != null && customItem.hasItemModel();
    }

    public static boolean hasCustomModelData(ItemBuilder customItem) {
        return customItem != null && customItem.getNexoMeta().getCustomModelData() != null;
    }

    public static @Nullable Integer getCustomModelData(ItemBuilder customItem) {
        if (customItem == null) return null;
        return customItem.getNexoMeta().getCustomModelData();
    }

    public static @Nullable String getItemModel(ItemBuilder customItem) {
        if (customItem == null || !customItem.hasItemModel()) return null;

        Key key = customItem.getItemModel();
        return key != null ? key.toString() : null;
    }

    public static File getItemConfigFile(ItemBuilder customItem) {
        if (customItem == null) return null;
        return getItemConfigFile(NexoItems.idFromItem(customItem));
    }

    public static File getItemConfigFile(String itemId) {
        if (itemId == null) return null;
        return NexoItems.itemMap()
                .entrySet()
                .stream()
                .filter(entry -> entry.getValue().containsKey(itemId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public static ConfigurationSection getItemConfig(ItemBuilder customItem) {
        if (customItem == null) return null;
        return getItemConfig(NexoItems.idFromItem(customItem));
    }

    public static ConfigurationSection getItemConfig(String itemId) {
        File configFile = getItemConfigFile(itemId);
        if (configFile == null) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        return config.getConfigurationSection(itemId);
    }

    public static String getId(ItemBuilder customItem) {
        if (customItem == null) return null;
        return NexoItems.idFromItem(customItem);
    }
}