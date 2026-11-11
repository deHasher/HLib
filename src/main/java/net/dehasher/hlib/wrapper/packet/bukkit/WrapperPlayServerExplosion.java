package net.dehasher.hlib.wrapper.packet.bukkit;

import java.util.Optional;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BukkitConverters;
import com.comphenix.protocol.wrappers.WrappedParticle;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.util.Vector;

public class WrapperPlayServerExplosion extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.EXPLOSION;

	public WrapperPlayServerExplosion() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
		getHandle().getNewParticles().write(0, WrappedParticle.create(Particle.EXPLOSION, null));
		getHandle().getSoundEffects().write(0, Sound.ENTITY_GENERIC_EXPLODE);
		getHandle().getVectors().write(0, new Vector());
		getHandle().getOptionals(BukkitConverters.getVectorConverter()).write(0, Optional.empty());
	}

	public WrapperPlayServerExplosion(PacketContainer packet) {
		super(packet, TYPE);
	}

	public double getX() {
		return getHandle().getVectors().read(0).getX();
	}

	public void setX(double value) {
		Vector center = getHandle().getVectors().read(0);
		getHandle().getVectors().write(0, center.setX(value));
	}

	public double getY() {
		return getHandle().getVectors().read(0).getY();
	}

	public void setY(double value) {
		Vector center = getHandle().getVectors().read(0);
		getHandle().getVectors().write(0, center.setY(value));
	}

	public double getZ() {
		return getHandle().getVectors().read(0).getZ();
	}

	public void setZ(double value) {
		Vector center = getHandle().getVectors().read(0);
		getHandle().getVectors().write(0, center.setZ(value));
	}

	public float getRadius() {
		return getHandle().getFloat().read(0);
	}

	public void setRadius(float value) {
		getHandle().getFloat().write(0, value);
	}

	public int getBlockCount() {
		return getHandle().getIntegers().read(0);
	}

	public void setBlockCount(int value) {
		getHandle().getIntegers().write(0, value);
	}

	public float getPlayerVelocityX() {
		return (float) getHandle().getOptionals(BukkitConverters.getVectorConverter()).read(0).orElseGet(Vector::new).getX();
	}

	public void setPlayerVelocityX(float value) {
		Vector velocity = getHandle().getOptionals(BukkitConverters.getVectorConverter()).read(0).orElseGet(Vector::new);
		getHandle().getOptionals(BukkitConverters.getVectorConverter()).write(0, Optional.of(velocity.setX(value)));
	}

	public float getPlayerVelocityY() {
		return (float) getHandle().getOptionals(BukkitConverters.getVectorConverter()).read(0).orElseGet(Vector::new).getY();
	}

	public void setPlayerVelocityY(float value) {
		Vector velocity = getHandle().getOptionals(BukkitConverters.getVectorConverter()).read(0).orElseGet(Vector::new);
		getHandle().getOptionals(BukkitConverters.getVectorConverter()).write(0, Optional.of(velocity.setY(value)));
	}

	public float getPlayerVelocityZ() {
		return (float) getHandle().getOptionals(BukkitConverters.getVectorConverter()).read(0).orElseGet(Vector::new).getZ();
	}

	public void setPlayerVelocityZ(float value) {
		Vector velocity = getHandle().getOptionals(BukkitConverters.getVectorConverter()).read(0).orElseGet(Vector::new);
		getHandle().getOptionals(BukkitConverters.getVectorConverter()).write(0, Optional.of(velocity.setZ(value)));
	}
}