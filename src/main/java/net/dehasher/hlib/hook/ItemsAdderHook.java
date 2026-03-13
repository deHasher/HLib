package net.dehasher.hlib.hook;

import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.ItemsAdder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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

    public static int getCustomModelData(String namespace) {
        CustomStack customStack = getCustomItem(namespace);
        if (customStack == null) return 0;
        return customStack.getItemStack().getItemMeta().getCustomModelData();
    }

    public static boolean hasPermission(CustomStack customStack) {
        if (customStack == null) return false;
        try {
            return customStack.hasPermission();
        } catch (Throwable t) {
            return false;
        }
    }

    @SuppressWarnings({"UnreachableCode", "SimplifiableConditionalExpression"})
    public static CustomStack getCustomItem(String item) {
        CustomStack customStack = CustomStack.getInstance(item);
        return customStack != null ? customStack : null;
    }

    public static Set<String> getAllCustomItems() {
        return CustomStack.getNamespacedIdsInRegistry();
    }

    public static Set<String> getAllCustomItems(Material material) {
        if (material == null) return getAllCustomItems();
        return CustomStack.getNamespacedIdsInRegistry()
                .stream()
                .map(ItemsAdderHook::getCustomItem)
                .filter(Objects::nonNull)
                .filter(custom -> custom.getItemStack().getType() == material)
                .map(CustomStack::getNamespacedID)
                .collect(Collectors.toSet());
    }

    public static Set<String> getAllCustomItemsMaterials() {
        return CustomStack.getNamespacedIdsInRegistry()
                .stream()
                .map(ItemsAdderHook::getCustomItem)
                .filter(Objects::nonNull)
                .map(custom -> custom.getItemStack().getType().name())
                .collect(Collectors.toSet());
    }

    @SuppressWarnings("UnreachableCode")
    public static String getPackUrl(boolean appendHash) {
        String url = ItemsAdder.getPackUrl(appendHash);
        if (url == null) return "";
        return url;
    }
}