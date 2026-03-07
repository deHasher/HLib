package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayClientChat extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Client.CHAT;

    public WrapperPlayClientChat() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayClientChat(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getMessage() {
        return getHandle().getStrings().read(0);
    }

    public void setMessage(String value) {
        getHandle().getStrings().write(0, value);
    }
}