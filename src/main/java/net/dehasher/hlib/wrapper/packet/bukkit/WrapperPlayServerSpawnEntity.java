package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.EntityType;
import java.util.UUID;

public class WrapperPlayServerSpawnEntity extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.SPAWN_ENTITY;

	public WrapperPlayServerSpawnEntity() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
	}

	public WrapperPlayServerSpawnEntity(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getEntityID() {
		return getHandle().getIntegers().read(0);
	}

	public void setEntityID(int value) {
		getHandle().getIntegers().write(0, value);
	}

	public UUID getUniqueId() {
		return getHandle().getUUIDs().read(0);
	}

	public void setUniqueId(UUID value) {
		getHandle().getUUIDs().write(0, value);
	}

	public double getX() {
		return getHandle().getDoubles().read(0);
	}

	public void setX(double value) {
		getHandle().getDoubles().write(0, value);
	}

	public double getY() {
		return getHandle().getDoubles().read(1);
	}

	public void setY(double value) {
		getHandle().getDoubles().write(1, value);
	}

	public double getZ() {
		return getHandle().getDoubles().read(2);
	}

	public void setZ(double value) {
		getHandle().getDoubles().write(2, value);
	}

	public float getPitch() {
		return (getHandle().getBytes().read(0) * 360.F) / 256.0F;
	}

	public void setPitch(float value) {
		getHandle().getBytes().write(0, (byte) (value * 256.0F / 360.0F));
	}

	public float getYaw() {
		return (getHandle().getBytes().read(1) * 360.F) / 256.0F;
	}

	public void setYaw(float value) {
		getHandle().getBytes().write(1, (byte) (value * 256.0F / 360.0F));
	}

	public EntityType getType() {
		return getHandle().getEntityTypeModifier().read(0);
	}

	public void setType(EntityType value) {
		getHandle().getEntityTypeModifier().write(0, value);
	}
}