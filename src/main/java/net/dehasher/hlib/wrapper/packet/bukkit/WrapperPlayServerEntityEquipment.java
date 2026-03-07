package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.ItemSlot;
import java.util.List;

public class WrapperPlayServerEntityEquipment extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_EQUIPMENT;

    public WrapperPlayServerEntityEquipment() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerEntityEquipment(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityID() {
        return getHandle().getIntegers().read(0);
    }

    public void setEntityID(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public Entity getEntity(World world) {
        return getHandle().getEntityModifier(world).read(0);
    }

    public Entity getEntity(PacketEvent e) {
        return getEntity(e.getPlayer().getWorld());
    }

    public List<com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack>> getSlotStackPairs() {
        return getHandle().getSlotStackPairLists().read(0);
    }

    public void setSlotStackPair(ItemSlot slot, ItemStack item) {
        List<com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack>> slotStackPairs = getHandle().getSlotStackPairLists().read(0);
        slotStackPairs.removeIf(pair -> pair.getFirst().equals(slot));
        slotStackPairs.add(new com.comphenix.protocol.wrappers.Pair<>(slot, item));
        getHandle().getSlotStackPairLists().write(0, slotStackPairs);
    }

    public boolean removeSlotStackPair(ItemSlot slot) {
        List<com.comphenix.protocol.wrappers.Pair<ItemSlot, ItemStack>> slotStackPairs = getHandle().getSlotStackPairLists().read(0);
        boolean removed = slotStackPairs.removeIf(pair -> pair.getFirst().equals(slot));
        getHandle().getSlotStackPairLists().write(0, slotStackPairs);
        return removed;
    }

    public boolean isSlotSet(ItemSlot slot) {
        return getHandle().getSlotStackPairLists().read(0).stream()
                .anyMatch(pair -> pair.getFirst().equals(slot));
    }

    public ItemStack getItem(ItemSlot slot) {
        return getHandle().getSlotStackPairLists().read(0)
                .stream()
                .filter(pair -> pair.getFirst().equals(slot))
                .map(Pair::getSecond)
                .findFirst()
                .orElse(null);
    }
}