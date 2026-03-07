package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

public class WrapperPlayServerWorldEvent extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.WORLD_EVENT;

    public WrapperPlayServerWorldEvent() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerWorldEvent(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEffectId() {
        return getHandle().getIntegers().read(0);
    }

    public void setEffectId(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public BlockPosition getLocation() {
        return getHandle().getBlockPositionModifier().read(0);
    }

    public void setLocation(BlockPosition value) {
        getHandle().getBlockPositionModifier().write(0, value);
    }

    public int getData() {
        return getHandle().getIntegers().read(1);
    }

    public void setData(int value) {
        getHandle().getIntegers().write(1, value);
    }

    public boolean getDisableRelativeVolume() {
        return getHandle().getBooleans().read(0);
    }

    public void setDisableRelativeVolume(boolean value) {
        getHandle().getBooleans().write(0, value);
    }
}