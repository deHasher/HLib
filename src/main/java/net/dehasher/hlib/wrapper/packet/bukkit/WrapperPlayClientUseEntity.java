package net.dehasher.hlib.wrapper.packet.bukkit;

import org.bukkit.util.Vector;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayClientUseEntity extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Client.USE_ENTITY;

	public WrapperPlayClientUseEntity() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
	}

	public WrapperPlayClientUseEntity(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getTargetID() {
		return getHandle().getIntegers().read(0);
	}

	public void setTargetID(int value) {
		getHandle().getIntegers().write(0, value);
	}

	public String getAction() {
		return getHandle().getEnumEntityUseActions().read(0).getAction().name();
	}

	public Vector getTargetVector() {
		return getHandle().getEnumEntityUseActions().read(0).getPosition();
	}

	public void setTargetVector(Vector value) {
		getHandle().getEnumEntityUseActions().read(0).setPosition(value);
	}
}