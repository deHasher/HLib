package net.dehasher.hlib.wrapper.packet.bukkit;

import java.util.List;
import java.util.Objects;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.google.common.collect.Lists;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

public class WrapperPlayServerEntityMetadata extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.ENTITY_METADATA;

	public WrapperPlayServerEntityMetadata() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
	}

	public WrapperPlayServerEntityMetadata(PacketContainer packet) {
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

	public List<WrappedWatchableObject> getMetadata() {
		return getHandle().getDataValueCollectionModifier().read(0).stream()
				.map(entry -> new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(entry.getIndex(), entry.getSerializer()), entry.getValue()))
				.toList();
	}

	public void setMetadata(List<WrappedWatchableObject> value) {
		List<WrappedDataValue> wrappedDataValueList = Lists.newArrayList();

		value.stream().filter(Objects::nonNull).forEach(entry -> {
			WrappedDataWatcher.WrappedDataWatcherObject watcherObject = entry.getWatcherObject();
			wrappedDataValueList.add(new WrappedDataValue(watcherObject.getIndex(), watcherObject.getSerializer(), entry.getRawValue()));
		});

		getHandle().getDataValueCollectionModifier().write(0, wrappedDataValueList);
	}
}