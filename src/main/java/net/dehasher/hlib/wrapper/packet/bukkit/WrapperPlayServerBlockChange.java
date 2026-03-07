package net.dehasher.hlib.wrapper.packet.bukkit;

import org.bukkit.Location;
import org.bukkit.World;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;

public class WrapperPlayServerBlockChange extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.BLOCK_CHANGE;

    public WrapperPlayServerBlockChange() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerBlockChange(PacketContainer packet) {
        super(packet, TYPE);
    }

    public BlockPosition getLocation() {
        return getHandle().getBlockPositionModifier().read(0);
    }

    public void setLocation(BlockPosition value) {
        getHandle().getBlockPositionModifier().write(0, value);
    }

    public Location getBukkitLocation(World world) {
        return getLocation().toVector().toLocation(world);
    }

    public WrappedBlockData getBlockData() {
        return getHandle().getBlockData().read(0);
    }

    public void setBlockData(WrappedBlockData value) {
        getHandle().getBlockData().write(0, value);
    }
}