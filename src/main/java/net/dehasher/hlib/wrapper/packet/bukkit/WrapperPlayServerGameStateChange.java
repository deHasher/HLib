package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerGameStateChange extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.GAME_STATE_CHANGE;

    public WrapperPlayServerGameStateChange() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerGameStateChange(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getReason() {
        return getHandle().getGameStateIDs().read(0);
    }

    public void setReason(int value) {
        getHandle().getGameStateIDs().write(0, value);
    }

    public float getValue() {
        return getHandle().getFloat().read(0);
    }

    public void setValue(float value) {
        getHandle().getFloat().write(0, value);
    }
}