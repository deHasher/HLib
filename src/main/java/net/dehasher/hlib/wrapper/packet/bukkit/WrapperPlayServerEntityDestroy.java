package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import java.util.stream.IntStream;
import java.util.List;

public class WrapperPlayServerEntityDestroy extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.ENTITY_DESTROY;

	public WrapperPlayServerEntityDestroy() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
	}

	public WrapperPlayServerEntityDestroy(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getCount() {
		return getHandle().getIntLists().read(0).size();
	}

	// Массив идентификаторов.
	public void setEntityIds(int[] value) {
		setEntityIds(IntStream.of(value).boxed().toList());
	}

	// Список идентификаторов.
	public void setEntityIds(List<Integer> value) {
		getHandle().getIntLists().write(0, value);
	}

	// Для всех версий.
	public void setEntityId(int value) {
		setEntityIds(List.of(value));
	}
}