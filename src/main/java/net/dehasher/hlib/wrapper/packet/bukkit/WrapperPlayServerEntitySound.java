package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers.SoundCategory;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class WrapperPlayServerEntitySound extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.ENTITY_SOUND;

	public WrapperPlayServerEntitySound() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
	}

	public WrapperPlayServerEntitySound(PacketContainer packet) {
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
		return getHandle().getEntityModifier(e).read(0);
	}

	public SoundCategory getSoundCategory() {
		return getHandle().getSoundCategories().read(0);
	}

	public void setSoundCategory(SoundCategory value) {
		getHandle().getSoundCategories().write(0, value);
	}

	public float getVolume() {
		return getHandle().getFloat().read(0);
	}

	public void setVolume(float value) {
		getHandle().getFloat().write(0, value);
	}

	public float getPitch() {
		return getHandle().getFloat().read(1);
	}

	public void setPitch(float value) {
		getHandle().getFloat().write(1, value);
	}

	public long getSeed() {
		return getHandle().getLongs().read(0);
	}

	public void setSeed(long value) {
		getHandle().getLongs().write(0, value);
	}

	public WrapperPlayServerNamedSoundEffect toNamedSoundEffect(Location location) {
		WrapperPlayServerNamedSoundEffect packet = new WrapperPlayServerNamedSoundEffect();
		packet.getHandle().getModifier().withType(MinecraftReflection.getHolderClass()).write(0, getHandle().getModifier().withType(MinecraftReflection.getHolderClass()).read(0));
		packet.setSoundCategory(getSoundCategory());
		packet.setEffectPosition(location);
		packet.setVolume(getVolume());
		packet.setPitch(getPitch());
		packet.setSeed(getSeed());
		return packet;
	}
}