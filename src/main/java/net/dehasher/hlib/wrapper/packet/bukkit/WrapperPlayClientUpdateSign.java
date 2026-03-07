package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

public class WrapperPlayClientUpdateSign extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Client.UPDATE_SIGN;

    public WrapperPlayClientUpdateSign() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayClientUpdateSign(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getLocation() {
        return getHandle().getBlockPositionModifier().read(0);
    }

    public void setLocation(BlockPosition value) {
        getHandle().getBlockPositionModifier().write(0, value);
    }

    public String[] getLines() {
        return getHandle().getStringArrays().read(0);
    }

    public void setLines(String[] value) {
        if (value == null) value = new String[]{"", "", "", ""};
        if (value.length != 4) throw new IllegalArgumentException("value must have 4 elements!");

        getHandle().getStringArrays().write(0, value);
    }
}