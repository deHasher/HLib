package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.BukkitConverters;
import org.bukkit.damage.DamageType;
import org.bukkit.util.Vector;
import java.util.Objects;
import java.util.Optional;

public class WrapperPlayServerDamageEvent extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.DAMAGE_EVENT;
	private static final MethodAccessor TO_BUKKIT = Accessors.getMethodAccessor(MinecraftReflection.getCraftBukkitClass("damage.CraftDamageType"), "minecraftHolderToBukkit", MinecraftReflection.getHolderClass());
	private static final MethodAccessor TO_MINECRAFT = Accessors.getMethodAccessor(MinecraftReflection.getCraftBukkitClass("damage.CraftDamageType"), "bukkitToMinecraftHolder", DamageType.class);

	public WrapperPlayServerDamageEvent() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
		setSourceType(DamageType.GENERIC);
		setSourceCauseID(-1);
		setSourceDirectID(-1);
		setSourcePosition(Optional.empty());
	}

	public WrapperPlayServerDamageEvent(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getEntityID() {
		return getHandle().getIntegers().read(0);
	}

	public void setEntityID(int value) {
		getHandle().getIntegers().write(0, value);
	}

	public DamageType getSourceType() {
		return (DamageType) TO_BUKKIT.invoke(null, getHandle().getModifier().withType(MinecraftReflection.getHolderClass()).read(0));
	}

	public void setSourceType(DamageType value) {
		getHandle().getModifier().withType(MinecraftReflection.getHolderClass()).write(0, TO_MINECRAFT.invoke(null, Objects.requireNonNull(value)));
	}

	public int getSourceCauseID() {
		return getHandle().getIntegers().read(1);
	}

	public void setSourceCauseID(int value) {
		getHandle().getIntegers().write(1, value);
	}

	public int getSourceDirectID() {
		return getHandle().getIntegers().read(2);
	}

	public void setSourceDirectID(int value) {
		getHandle().getIntegers().write(2, value);
	}

	public boolean hasSourcePosition() {
		return getSourcePosition().isPresent();
	}

	public Optional<Vector> getSourcePosition() {
		return getHandle().getOptionals(BukkitConverters.getVectorConverter()).read(0);
	}

	public void setSourcePosition(Optional<Vector> value) {
		getHandle().getOptionals(BukkitConverters.getVectorConverter()).write(0, Objects.requireNonNull(value));
	}
}