package net.dehasher.hlib.wrapper.packet.bukkit;

import net.dehasher.hlib.data.BukkitVersion;
import org.bukkit.util.Vector;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dehasher.hlib.Tools;

public class WrapperPlayClientUseEntity extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Client.USE_ENTITY;

    public WrapperPlayClientUseEntity() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayClientUseEntity(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getTargetID() {
        return getHandle().getIntegers().read(0);
    }

    public void setTargetID(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public String getAction() {
        if (Tools.requireBukkitVersion(BukkitVersion.V1_17)) {
            return getHandle().getEnumEntityUseActions().read(0).getAction().name();
        } else {
            return getHandle().getEntityUseActions().read(0).name();
        }
    }

    public Vector getTargetVector() {
        return getHandle().getVectors().read(0);
    }

    public void setTargetVector(Vector value) {
        getHandle().getVectors().write(0, value);
    }
}