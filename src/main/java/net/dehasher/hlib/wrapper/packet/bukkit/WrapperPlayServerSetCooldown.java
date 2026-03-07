package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.wrappers.MinecraftKey;
import net.dehasher.hlib.data.BukkitVersion;
import org.bukkit.Material;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.EquivalentConverter;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import net.dehasher.hlib.Tools;
import org.bukkit.NamespacedKey;

public class WrapperPlayServerSetCooldown extends AbstractPacket {
    private static final Class<?> ITEM_CLASS = MinecraftReflection.getMinecraftClass(Tools.requireBukkitVersion(BukkitVersion.V1_17) ? "world.item.Item" :"Item");
    public static final PacketType TYPE = PacketType.Play.Server.SET_COOLDOWN;

    public WrapperPlayServerSetCooldown() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerSetCooldown(PacketContainer packet) {
        super(packet, TYPE);
    }

    private boolean usedCooldownGroupKey() {
        StructureModifier<MinecraftKey> keys = getHandle().getMinecraftKeys();
        return keys != null && keys.size() > 0;
    }

    public Material getItem() {
        if (usedCooldownGroupKey()) {
            MinecraftKey key = getHandle().getMinecraftKeys().read(0);
            Material mat = Material.matchMaterial(key.getFullKey());
            return (mat != null) ? mat : Material.matchMaterial(key.getKey());
        } else return getHandle().getModifier().withType(ITEM_CLASS, new ItemConverter()).read(0);
    }

    public void setItem(Material value) {
        if (usedCooldownGroupKey()) {
            NamespacedKey bukkitKey = value.getKey();
            getHandle().getMinecraftKeys().write(0, new MinecraftKey(bukkitKey.getNamespace(), bukkitKey.getKey()));
        } else getHandle().getModifier()
                .withType(ITEM_CLASS, new ItemConverter())
                .write(0, value);
    }

    public int getTicks() {
        return getHandle().getIntegers().read(0);
    }

    public void setTicks(int value) {
        getHandle().getIntegers().write(0, value);
    }

    private static class ItemConverter implements EquivalentConverter<Material> {
        private static MethodAccessor getMaterial = null;
        private static MethodAccessor getItem = null;

        @Override
        public Material getSpecific(Object generic) {
            if (getMaterial == null) getMaterial = Accessors.getMethodAccessor(MinecraftReflection.getCraftBukkitClass("util.CraftMagicNumbers"), "getMaterial", ITEM_CLASS);
            return (Material) getMaterial.invoke(null, generic);
        }

        @Override
        public Object getGeneric(Material specific) {
            if (getItem == null) getItem = Accessors.getMethodAccessor(MinecraftReflection.getCraftBukkitClass("util.CraftMagicNumbers"), "getItem", Material.class);
            return getItem.invoke(null, specific);
        }

        @Override
        public Class<Material> getSpecificType() {
            return Material.class;
        }
    }
}