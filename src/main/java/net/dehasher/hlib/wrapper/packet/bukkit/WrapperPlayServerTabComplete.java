package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.mojang.brigadier.suggestion.Suggestions;

public class WrapperPlayServerTabComplete extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.TAB_COMPLETE;

    public WrapperPlayServerTabComplete() {
        super(new PacketContainer(TYPE), TYPE);
        handle.getModifier().writeDefaults();
    }

    public WrapperPlayServerTabComplete(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTransactionId() {
        return handle.getIntegers().read(0);
    }

    public void setTransactionId(int value) {
        handle.getIntegers().write(0, value);
    }

    public Suggestions getSuggestions() {
        return handle.getSpecificModifier(Suggestions.class).readSafely(0);
    }

    public void setSuggestions(Suggestions value) {
        handle.getSpecificModifier(Suggestions.class).write(0, value);
    }

}