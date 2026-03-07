package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import net.dehasher.hlib.data.BukkitVersion;
import net.dehasher.hlib.Tools;
import java.util.Collections;
import java.util.List;

public class WrapperPlayServerEntityDestroy extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.ENTITY_DESTROY;

    public WrapperPlayServerEntityDestroy() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerEntityDestroy(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getCount() {
        return getHandle().getIntegerArrays().read(0).length;
    }

    // Для версий 1.16.5 и ниже.
    public void setEntityIds(int[] value) {
        getHandle().getIntegerArrays().write(0, value);
    }

    // Для версий 1.17 и выше.
    public void setEntityIds(List<Integer> value) {
        getHandle().getIntLists().write(0, value);
    }

    // Для всех версий.
    public void setEntityId(int value) {
        if (!Tools.requireBukkitVersion(BukkitVersion.V1_17)) {
            setEntityIds(new int[]{value});
        } else {
            setEntityIds(Collections.singletonList(value));
        }
    }
}