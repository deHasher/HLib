package net.dehasher.hlib.wrapper.packet.bukkit;

import java.util.List;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

public class WrapperPlayServerExplosion extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.EXPLOSION;

    public WrapperPlayServerExplosion() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerExplosion(PacketContainer packet) {
        super(packet, TYPE);
    }

    public double getX() {
        return getHandle().getDoubles().read(0);
    }

    public void setX(double value) {
        getHandle().getDoubles().write(0, value);
    }

    public double getY() {
        return getHandle().getDoubles().read(1);
    }

    public void setY(double value) {
        getHandle().getDoubles().write(1, value);
    }

    public double getZ() {
        return getHandle().getDoubles().read(2);
    }

    public void setZ(double value) {
        getHandle().getDoubles().write(2, value);
    }

    public float getRadius() {
        return getHandle().getFloat().read(0);
    }

    public void setRadius(float value) {
        getHandle().getFloat().write(0, value);
    }

    public List<BlockPosition> getRecords() {
        return getHandle().getBlockPositionCollectionModifier().read(0);
    }

    public void setRecords(List<BlockPosition> value) {
        getHandle().getBlockPositionCollectionModifier().write(0, value);
    }

    public float getPlayerVelocityX() {
        return getHandle().getFloat().read(1);
    }

    public void setPlayerVelocityX(float value) {
        getHandle().getFloat().write(1, value);
    }

    public float getPlayerVelocityY() {
        return getHandle().getFloat().read(2);
    }

    public void setPlayerVelocityY(float value) {
        getHandle().getFloat().write(2, value);
    }

    public float getPlayerVelocityZ() {
        return getHandle().getFloat().read(3);
    }

    public void setPlayerVelocityZ(float value) {
        getHandle().getFloat().write(3, value);
    }
}