package net.dehasher.hlib.data;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;
import net.dehasher.hlib.Tools;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

// Attribute незя тут обработать из-за несоответствия классов...
@SuppressWarnings({"deprecation"})
public class NMS {
    public static PotionEffectType JUMP_BOOST;
    public static PotionEffectType SLOWNESS;
    public static PotionEffectType NAUSEA;
    public static PotionEffectType MINING_FATIGUE;
    public static PotionEffectType WEAVING;

    public static Particle LARGE_SMOKE;
    public static Particle DUST;
    public static Particle ENTITY_EFFECT;
    public static Particle ELDER_GUARDIAN;
    public static Particle EXPLOSION_EMITTER;
    public static Particle FALLING_WATER;
    public static Particle ITEM_SLIME;

    public static EntityType FIREWORK_ROCKET;

    public static Enchantment SHARPNESS;
    public static Enchantment LUCK_OF_THE_SEA;
    public static Enchantment UNBREAKING;

    public static final String VERSION;

    static {
        JUMP_BOOST = PotionEffectType.getByName("JUMP");
        if (JUMP_BOOST == null) JUMP_BOOST = PotionEffectType.getByName("JUMP_BOOST");

        SLOWNESS = PotionEffectType.getByName("SLOW");
        if (SLOWNESS == null) SLOWNESS = PotionEffectType.getByName("SLOWNESS");

        NAUSEA = PotionEffectType.getByName("CONFUSION");
        if (NAUSEA == null) NAUSEA = PotionEffectType.getByName("NAUSEA");

        MINING_FATIGUE = PotionEffectType.getByName("SLOW_DIGGING");
        if (MINING_FATIGUE == null) MINING_FATIGUE = PotionEffectType.getByName("MINING_FATIGUE");

        WEAVING = PotionEffectType.getByName("WEAVING");

        try {
            LARGE_SMOKE = Particle.valueOf("SMOKE_LARGE");
        } catch (Throwable t) {
            LARGE_SMOKE = Particle.valueOf("LARGE_SMOKE");
        }

        try {
            DUST = Particle.valueOf("REDSTONE");
        } catch (Throwable t) {
            DUST = Particle.valueOf("DUST");
        }

        try {
            ENTITY_EFFECT = Particle.valueOf("SPELL_MOB");
        } catch (Throwable t) {
            ENTITY_EFFECT = Particle.valueOf("ENTITY_EFFECT");
        }

        try {
            ELDER_GUARDIAN = Particle.valueOf("MOB_APPEARANCE");
        } catch (Throwable t) {
            ELDER_GUARDIAN = Particle.valueOf("ELDER_GUARDIAN");
        }

        try {
            EXPLOSION_EMITTER = Particle.valueOf("EXPLOSION_LARGE");
        } catch (Throwable t) {
            EXPLOSION_EMITTER = Particle.valueOf("EXPLOSION_EMITTER");
        }

        try {
            FALLING_WATER = Particle.valueOf("WATER_SPLASH");
        } catch (Throwable t) {
            FALLING_WATER = Particle.valueOf("FALLING_WATER");
        }

        try {
            ITEM_SLIME = Particle.valueOf("SLIME");
        } catch (Throwable t) {
            ITEM_SLIME = Particle.valueOf("ITEM_SLIME");
        }

        try {
            FIREWORK_ROCKET = EntityType.valueOf("FIREWORK");
        } catch (Throwable t) {
            FIREWORK_ROCKET = EntityType.valueOf("FIREWORK_ROCKET");
        }

        try {
            SHARPNESS = Enchantment.getByName("DAMAGE_ALL");
            if (SHARPNESS == null) throw new Throwable();
        } catch (Throwable t) {
            SHARPNESS = Enchantment.getByName("SHARPNESS");
        }

        try {
            LUCK_OF_THE_SEA = Enchantment.getByName("LUCK");
            if (LUCK_OF_THE_SEA == null) throw new Throwable();
        } catch (Throwable t) {
            LUCK_OF_THE_SEA = Enchantment.getByName("LUCK_OF_THE_SEA");
        }

        UNBREAKING = Enchantment.getByKey(NamespacedKey.minecraft("unbreaking"));

        VERSION = detectNMSVersion();
    }

    public static class Packets {
        public static int END_PORTAL_CREATION_EVENT_ID;
        public static int ARMOR_STAND_META_INDEX;
        public static EntityType LIGHTNING_BOLT;

        static {
            END_PORTAL_CREATION_EVENT_ID = 1038;

            if (Tools.requireBukkitVersion(BukkitVersion.V1_17)) {
                ARMOR_STAND_META_INDEX = 15;
            } else {
                ARMOR_STAND_META_INDEX = 14;
            }

            try {
                LIGHTNING_BOLT = EntityType.valueOf("LIGHTNING");
            } catch (Throwable t) {
                LIGHTNING_BOLT = EntityType.valueOf("LIGHTNING_BOLT");
            }
        }
    }

    public static Sound getSound(String name) {
        Sound sound = Registry.SOUNDS.get(NamespacedKey.minecraft(name.toLowerCase().replace("_", ".")));
        if (sound == null) {
            try {
                Field f = Sound.class.getField(name);
                return (Sound) f.get(null);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return null;
    }

    public static class Inventory {
        public static InventoryType getInventoryType(InventoryView view) {
            try {
                Method method = view.getClass().getMethod("getType");
                method.setAccessible(true);
                return (InventoryType) method.invoke(view);
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }

        public static org.bukkit.inventory.Inventory getTopInventory(InventoryView view) {
            try {
                Method method = view.getClass().getMethod("getTopInventory");
                method.setAccessible(true);
                return (org.bukkit.inventory.Inventory) method.invoke(view);
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }

        public static HumanEntity getPlayer(InventoryView view) {
            try {
                Method method = view.getClass().getMethod("getPlayer");
                method.setAccessible(true);
                return (HumanEntity) method.invoke(view);
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }

        public static ItemStack getItem(InventoryView view, int slot) {
            try {
                Method method = view.getClass().getMethod("getItem", int.class);
                method.setAccessible(true);
                return (ItemStack) method.invoke(view, slot);
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }

        public static Component title(InventoryView view) {
            try {
                Method method = view.getClass().getMethod("title");
                method.setAccessible(true);
                return (Component) method.invoke(view);
            } catch (Throwable t) {
                t.printStackTrace();
                return null;
            }
        }
    }

    private static String detectNMSVersion() {
        String version = Tools.getBukkitVersionAsMinor().replace(".", "_") + "_R";
        for (int r = 1; r <= 20; r++) {
            String candidate = "org.bukkit.craftbukkit.v" + version + r + ".CraftWorld";
            try {
                Class.forName(candidate);
                return version + r;
            } catch (ClassNotFoundException ignore) {}
        }
        return "UNKNOWN";
    }
}