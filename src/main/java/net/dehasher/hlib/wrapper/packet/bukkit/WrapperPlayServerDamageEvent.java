package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerDamageEvent extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.DAMAGE_EVENT;

    public WrapperPlayServerDamageEvent() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
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

    public int getSourceTypeID() {
        return getHandle().getIntegers().read(1);
    }

    public void setSourceTypeID(int value) {
        getHandle().getIntegers().write(1, value);
    }

    public int getSourceCauseID() {
        return getHandle().getIntegers().read(2);
    }

    public void setSourceCauseID(int value) {
        getHandle().getIntegers().write(2, value);
    }

    public int getSourceDirectID() {
        return getHandle().getIntegers().read(3);
    }

    public void setSourceDirectID(int value) {
        getHandle().getIntegers().write(3, value);
    }

    public boolean hasSourcePosition() {
        return getHandle().getBooleans().read(0);
    }

    public void setSourcePosition(boolean value) {
        getHandle().getBooleans().write(0, value);
    }
}