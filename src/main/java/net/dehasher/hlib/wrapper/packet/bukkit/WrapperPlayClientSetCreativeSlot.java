package net.dehasher.hlib.wrapper.packet.bukkit;

import org.bukkit.inventory.ItemStack;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayClientSetCreativeSlot extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Client.SET_CREATIVE_SLOT;

	public WrapperPlayClientSetCreativeSlot() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
	}

	public WrapperPlayClientSetCreativeSlot(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getSlot() {
		return getHandle().getShorts().read(0);
	}

	public void setSlot(int value) {
		if (value < Short.MIN_VALUE || value > Short.MAX_VALUE) throw new IllegalArgumentException("Slot must fit a short");
		getHandle().getShorts().write(0, (short) value);
	}

	public ItemStack getClickedItem() {
		return getHandle().getItemModifier().readSafely(0);
	}

	public void setClickedItem(ItemStack value) {
		getHandle().getItemModifier().write(0, value);
	}

}