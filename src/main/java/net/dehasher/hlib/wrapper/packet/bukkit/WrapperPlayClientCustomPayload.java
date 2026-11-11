package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;

public class WrapperPlayClientCustomPayload extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Client.CUSTOM_PAYLOAD;

	public WrapperPlayClientCustomPayload() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
	}

	public WrapperPlayClientCustomPayload(PacketContainer packet) {
		super(packet, TYPE);
	}

	public MinecraftKey getChannel() {
		return getHandle().getCustomPacketPayloads().read(0).getId();
	}
}