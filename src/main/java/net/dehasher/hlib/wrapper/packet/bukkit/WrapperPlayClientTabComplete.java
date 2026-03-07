package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayClientTabComplete extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Client.TAB_COMPLETE;

    public WrapperPlayClientTabComplete() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayClientTabComplete(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTransactionId() {
        return getHandle().getIntegers().read(0);
    }

    public void setTransactionId(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public String getInput() {
        return getHandle().getStrings().read(0);
    }

    public void setInput(String value) {
        getHandle().getStrings().write(0, value);
    }
}