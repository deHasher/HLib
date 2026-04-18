package net.dehasher.hlib.controller;

import net.dehasher.hlib.Informer;
import net.dehasher.hlib.Scheduler;
import net.dehasher.hlib.data.NMS;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;
import net.dehasher.hlib.Tools;
import java.util.*;
import java.util.stream.Stream;

public class ItemsController {
    private static final ItemStack AIR = new ItemStack(Material.AIR);

    public static ItemStack getAir() {
        return AIR.clone();
    }

    public static boolean isBook(ItemStack item) {
        return item.hasItemMeta() && isBook(item.getItemMeta());
    }

    public static boolean isBook(ItemMeta meta) {
        return meta instanceof BookMeta;
    }

    public static boolean isDebugStick(ItemStack item) {
        return item.getType() == Material.DEBUG_STICK;
    }

    public static boolean isSkull(ItemStack item) {
        return item.hasItemMeta() && isSkull(item.getItemMeta());
    }

    public static boolean isSkull(ItemMeta meta) {
        return meta instanceof SkullMeta;
    }

    public static boolean isSpawnEgg(ItemStack item) {
        return item.hasItemMeta() && isSpawnEgg(item.getItemMeta());
    }

    public static boolean isSpawnEgg(ItemMeta meta) {
        return meta instanceof SpawnEggMeta;
    }

    public static boolean isPotion(ItemStack item) {
        return item.hasItemMeta() && isPotion(item.getItemMeta());
    }

    public static boolean isPotion(ItemMeta meta) {
        return meta instanceof PotionMeta;
    }

    public static boolean isCrossbow(ItemStack item) {
        return item.hasItemMeta() && isCrossbow(item.getItemMeta());
    }

    public static boolean isCrossbow(ItemMeta meta) {
        return meta instanceof CrossbowMeta;
    }

    public static boolean isFirework(ItemStack item) {
        return item.hasItemMeta() && isFirework(item.getItemMeta());
    }

    public static boolean isFirework(ItemMeta meta) {
        return meta instanceof FireworkMeta;
    }

    public static boolean isSuspiciousStew(ItemStack item) {
        return item.hasItemMeta() && isSuspiciousStew(item.getItemMeta());
    }

    public static boolean isSuspiciousStew(ItemMeta meta) {
        return meta instanceof SuspiciousStewMeta;
    }

    public static boolean isBanner(ItemStack item) {
        return item.hasItemMeta() && isBanner(item.getItemMeta());
    }

    public static boolean isBanner(ItemMeta meta) {
        return meta instanceof BannerMeta;
    }

    public static boolean isBanner(Block item) {
        return item.getType().name().toUpperCase().endsWith("BANNER");
    }

    public static boolean isItemFrame(ItemStack item) {
        return item.getType().name().toUpperCase().endsWith("ITEM_FRAME");
    }

    public static boolean isArmorStand(ItemStack item) {
        return item.getType().name().equalsIgnoreCase("ARMOR_STAND");
    }

    public static boolean isShulker(ItemStack item) {
        return item.getType().name().toUpperCase().endsWith("SHULKER_BOX");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isBundle(ItemStack item) {
        return item.getType().name().endsWith("BUNDLE");
    }

    public static boolean isPickaxe(ItemStack item) {
        return item.getType().name().toUpperCase().contains("PICKAXE");
    }

    public static boolean isSword(ItemStack item) {
        return item.getType().name().toUpperCase().endsWith("_SWORD");
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isShield(ItemStack item) {
        return item.getType().name().equalsIgnoreCase("SHIELD");
    }

    public static boolean isLightning(String input) {
        if (input == null || input.isEmpty()) return false;
        input = input.toUpperCase();
        return Stream.of("ENTITY_LIGHTNING_THUNDER", "ENTITY_LIGHTNING_IMPACT", "ENTITY_LIGHTNING_BOLT_THUNDER", "ENTITY_LIGHTNING_BOLT_IMPACT", "AMBIENT_WEATHER_THUNDER")
                .anyMatch(input::equalsIgnoreCase);
    }

    public static void toggleGlowing(ItemStack item, boolean state) {
        if (item == null || item.getType() == Material.AIR) return;
        ItemMeta meta = item.getItemMeta();
        if (state) {
            if (!meta.getEnchants().isEmpty()) return;
            meta.addEnchant(NMS.LUCK_OF_THE_SEA, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            meta.getEnchants().clear();
            meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        item.setItemMeta(meta);
    }

    // Предмет, который будет установлен в слоте хотабара: e.getCurrentItem()
    // Предмет, который будет перемещён: getSwappingItem(e)
    public static boolean isSwapAction(InventoryClickEvent e) {
        InventoryAction action = e.getAction();
        return action == InventoryAction.HOTBAR_SWAP || action == InventoryAction.HOTBAR_MOVE_AND_READD;
    }

    // Предмет, который будет установлен в слоте хотабара: e.getCurrentItem()
    // Предмет, который будет перемещён:
    public static ItemStack getSwappingItem(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return null;
        PlayerInventory inventory = player.getInventory();
        return e.getHotbarButton() == -1 ? inventory.getItemInOffHand() : inventory.getItem(e.getHotbarButton());
    }

    // Получение предмета, который был перемещён в инвентаре в зависимости от направления.
    // Узнать направление предмета можно с помощью метода itemDestination(e)
    // Важно! Контейнер может быть заполнен и предмет не переместится!
    public static ItemStack itemMove(InventoryClickEvent e, Boolean destination) {
        if (!(e.getWhoClicked() instanceof Player)) return null;
        Inventory inventory = e.getClickedInventory();

        if (inventory == null || destination == null) return null;
        if (isSwapAction(e)) throw new IllegalStateException("Invalid action type!"); // Обрабатывается отдельно нахуй!!!

        return switch (e.getAction()) {
            // Если игрок кладёт предмет в e.getView().
            // Помещаемый предмет: e.getCursor()
            case PLACE_ALL:
            case PLACE_SOME:
            case PLACE_ONE:
            case SWAP_WITH_CURSOR:
            case COLLECT_TO_CURSOR:
                if (destination && inventory.getType() == InventoryType.PLAYER) yield null;
                if (!destination && inventory.getType() != InventoryType.PLAYER) yield null;
                yield e.getCursor();

                // Если игрок кликает на предмет с зажатой клавишей Shift,
                // в своём инвентаре, чтобы переместить его в противоположный.
                // Помещаемый предмет: e.getCurrentItem()
            case MOVE_TO_OTHER_INVENTORY:
                if (destination && inventory.getType() != InventoryType.PLAYER) yield null;
                if (!destination && inventory.getType() == InventoryType.PLAYER) yield null;
                yield e.getCurrentItem();

                // CLONE_STACK - не нужен, так как игрок только берёт предмет, а не кладёт.
                // Обработка, когда игрок уже кладёт предмет выполняется через InventoryDragEvent.
            default: yield null;
        };
    }

    // Пример метода, для обработки предметов которые были перемещены с помощью InventoryDragEvent.
    // Направление предметов можно узнать с помощью: slot < e.getInventory().getSize()
    // return true if outside.
    // return false if inside.
    @SuppressWarnings({"UnreachableCode", "SameReturnValue"})
    public static ItemStack itemMove(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return null;

        // Предмет, который перемещается или дублируется.
        if (e.getOldCursor().getType() != Material.AIR) return e.getOldCursor();

        // Обработка предметов, которые уже были перемещены или дублированы.
        Scheduler.doAsync(() ->
                e.getNewItems().forEach((slot, item) ->
                        Informer.send(Tools.join(" ", NMS.Inventory.getItem(e.getView(), slot), "MOVE TO", slot < e.getInventory().getSize() ? "OUTSIDE" : "INSIDE"))));
        return null;
    }

    // Направление, куда помещается предмет.
    // return true if outside.
    // return false if inside.
    // return null if unknown.
    public static Boolean itemDestination(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return null;
        Inventory inventory = e.getClickedInventory();
        if (inventory == null) return null;
        return switch (e.getAction()) {
            case PLACE_ALL, PLACE_SOME, PLACE_ONE, SWAP_WITH_CURSOR, COLLECT_TO_CURSOR, HOTBAR_SWAP, HOTBAR_MOVE_AND_READD -> inventory.getType() != InventoryType.PLAYER;
            case MOVE_TO_OTHER_INVENTORY -> inventory.getType() == InventoryType.PLAYER;
            default -> null;
        };
    }

    // Направление, куда перемещается предмет.
    // ВАЖНО! Если хоть 1 предмет был вынесен из инвентаря игрока в любой контейнер, то return true.
    // return true if outside.
    // return false if inside.
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean itemDestination(InventoryDragEvent e) {
        Collection<Integer> rawSlots = e.getRawSlots();
        if (rawSlots.isEmpty()) return true;
        return Collections.min(rawSlots) < e.getInventory().getSize();
    }

    // Получить активный предмет, который держит игрок в руке.
    // Приоритет - активная рука.
    public static ItemStack getActiveItem(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main.getType() != Material.AIR) return main;
        ItemStack off = player.getInventory().getItemInOffHand();
        if (off.getType() != Material.AIR) return off;
        return null;
    }

    // Полный ли инвентарь у игрока?
    public static boolean isInventoryFull(Player player, ItemStack toAdd) {
        PlayerInventory inv = player.getInventory();
        if (inv.firstEmpty() != -1) return false;
        for (ItemStack c : inv.getStorageContents()) {
            if (c.isSimilar(toAdd) && c.getAmount() < c.getMaxStackSize()) return false;
        }
        return true;
    }
}