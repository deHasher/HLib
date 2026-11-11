package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedPositionMoveRotation;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

public class WrapperPlayServerEntityTeleport extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.ENTITY_POSITION_SYNC;

	public WrapperPlayServerEntityTeleport() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
		getHandle().getPositionMoveRotation().write(0, WrappedPositionMoveRotation.create(new Vector(), new Vector(), 0, 0));
	}

	public WrapperPlayServerEntityTeleport(PacketContainer packet) {
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

	public Entity getEntity(PacketEvent event) {
		return getEntity(event.getPlayer().getWorld());
	}

	public double getX() {
		return getHandle().getPositionMoveRotation().read(0).getPosition().getX();
	}

	public void setX(double value) {
		WrappedPositionMoveRotation state = getHandle().getPositionMoveRotation().read(0);
		state.setPosition(state.getPosition().setX(value));
		getHandle().getPositionMoveRotation().write(0, state);
	}

	public double getY() {
		return getHandle().getPositionMoveRotation().read(0).getPosition().getY();
	}

	public void setY(double value) {
		WrappedPositionMoveRotation state = getHandle().getPositionMoveRotation().read(0);
		state.setPosition(state.getPosition().setY(value));
		getHandle().getPositionMoveRotation().write(0, state);
	}

	public double getZ() {
		return getHandle().getPositionMoveRotation().read(0).getPosition().getZ();
	}

	public void setZ(double value) {
		WrappedPositionMoveRotation state = getHandle().getPositionMoveRotation().read(0);
		state.setPosition(state.getPosition().setZ(value));
		getHandle().getPositionMoveRotation().write(0, state);
	}

	public double getVelocityX() {
		return getHandle().getPositionMoveRotation().read(0).getDeltaMovement().getX();
	}

	public void setVelocityX(double value) {
		WrappedPositionMoveRotation state = getHandle().getPositionMoveRotation().read(0);
		state.setDeltaMovement(state.getDeltaMovement().setX(value));
		getHandle().getPositionMoveRotation().write(0, state);
	}

	public double getVelocityY() {
		return getHandle().getPositionMoveRotation().read(0).getDeltaMovement().getY();
	}

	public void setVelocityY(double value) {
		WrappedPositionMoveRotation state = getHandle().getPositionMoveRotation().read(0);
		state.setDeltaMovement(state.getDeltaMovement().setY(value));
		getHandle().getPositionMoveRotation().write(0, state);
	}

	public double getVelocityZ() {
		return getHandle().getPositionMoveRotation().read(0).getDeltaMovement().getZ();
	}

	public void setVelocityZ(double value) {
		WrappedPositionMoveRotation state = getHandle().getPositionMoveRotation().read(0);
		state.setDeltaMovement(state.getDeltaMovement().setZ(value));
		getHandle().getPositionMoveRotation().write(0, state);
	}

	public float getYaw() {
		return getHandle().getPositionMoveRotation().read(0).getYRot();
	}

	public void setYaw(float value) {
		WrappedPositionMoveRotation state = getHandle().getPositionMoveRotation().read(0);
		state.setYRot(value);
		getHandle().getPositionMoveRotation().write(0, state);
	}

	public float getPitch() {
		return getHandle().getPositionMoveRotation().read(0).getXRot();
	}

	public void setPitch(float value) {
		WrappedPositionMoveRotation state = getHandle().getPositionMoveRotation().read(0);
		state.setXRot(value);
		getHandle().getPositionMoveRotation().write(0, state);
	}

	public boolean getOnGround() {
		return getHandle().getBooleans().read(0);
	}

	public void setOnGround(boolean value) {
		getHandle().getBooleans().write(0, value);
	}
}