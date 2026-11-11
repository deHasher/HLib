package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class WrapperPlayServerSetCooldown extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.SET_COOLDOWN;

	public WrapperPlayServerSetCooldown() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
	}

	public WrapperPlayServerSetCooldown(PacketContainer packet) {
		super(packet, TYPE);
	}

	public Material getItem() {
		return Material.matchMaterial(getHandle().getMinecraftKeys().read(0).getFullKey());
	}

	public void setItem(Material value) {
		NamespacedKey key = value.getKey();
		getHandle().getMinecraftKeys().write(0, new MinecraftKey(key.getNamespace(), key.getKey()));
	}

	public int getTicks() {
		return getHandle().getIntegers().read(0);
	}

	public void setTicks(int value) {
		getHandle().getIntegers().write(0, value);
	}
}