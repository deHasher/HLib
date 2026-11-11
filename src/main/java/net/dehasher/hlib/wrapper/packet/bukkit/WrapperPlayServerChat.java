package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class WrapperPlayServerChat extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.SYSTEM_CHAT;

	public WrapperPlayServerChat() {
		super(new PacketContainer(TYPE), TYPE);
		getHandle().getModifier().writeDefaults();
	}

	public WrapperPlayServerChat(PacketContainer packet) {
		super(packet, TYPE);
	}

	public WrappedChatComponent getMessage() {
		return getHandle().getChatComponents().read(0);
	}

	public void setMessage(WrappedChatComponent value) {
		getHandle().getChatComponents().write(0, value);
	}

	public boolean isOverlay() {
		return getHandle().getBooleans().read(0);
	}

	public void setOverlay(boolean value) {
		getHandle().getBooleans().write(0, value);
	}
}