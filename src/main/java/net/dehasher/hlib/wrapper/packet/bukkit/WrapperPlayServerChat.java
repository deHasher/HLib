package net.dehasher.hlib.wrapper.packet.bukkit;

import java.util.Objects;
import java.util.stream.Stream;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.EnumWrappers.ChatType;
import com.comphenix.protocol.wrappers.WrappedChatComponent;

public class WrapperPlayServerChat extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.CHAT;

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

    public ChatType getChatType() {
        return getHandle().getChatTypes().read(0);
    }

    public void setChatType(ChatType type) {
        getHandle().getChatTypes().write(0, type);
    }

    public byte getPosition() {
        Byte position = getHandle().getBytes().readSafely(0);
        return Objects.requireNonNullElseGet(position, () -> getChatType().getId());
    }

    public void setPosition(byte value) {
        getHandle().getBytes().writeSafely(0, value);

        if (EnumWrappers.getChatTypeClass() != null) {
            Stream.of(ChatType.values()).filter(t -> t.getId() == value)
                    .findAny().ifPresent(t -> getHandle().getChatTypes().writeSafely(0, t));
        }
    }
}