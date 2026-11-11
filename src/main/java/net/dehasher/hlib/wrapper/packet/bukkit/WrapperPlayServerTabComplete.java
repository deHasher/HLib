package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.ConstructorAccessor;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestions;
import java.util.List;
import java.util.Objects;

public class WrapperPlayServerTabComplete extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.TAB_COMPLETE;
	private static final ConstructorAccessor PACKET_CONSTRUCTOR = Accessors.getConstructorAccessor(TYPE.getPacketClass(), int.class, Suggestions.class);
	private static final MethodAccessor TO_SUGGESTIONS = Accessors.getMethodAccessor(TYPE.getPacketClass(), "toSuggestions");

	public WrapperPlayServerTabComplete() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
		setSuggestions(new Suggestions(StringRange.at(0), List.of()));
	}

	public WrapperPlayServerTabComplete(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getTransactionId() {
		return getHandle().getIntegers().read(0);
	}

	public void setTransactionId(int value) {
		getHandle().getIntegers().write(0, value);
	}

	public Suggestions getSuggestions() {
		return (Suggestions) TO_SUGGESTIONS.invoke(getHandle().getHandle());
	}

	public void setSuggestions(Suggestions value) {
		PacketContainer packet = new PacketContainer(TYPE, PACKET_CONSTRUCTOR.invoke(getTransactionId(), Objects.requireNonNull(value)));
		getHandle().getIntegers().write(1, packet.getIntegers().read(1));
		getHandle().getIntegers().write(2, packet.getIntegers().read(2));
		getHandle().getModifier().withType(List.class).write(0, packet.getModifier().withType(List.class).read(0));
	}

}