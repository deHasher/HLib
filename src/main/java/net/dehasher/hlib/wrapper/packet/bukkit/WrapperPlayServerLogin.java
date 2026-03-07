package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.entity.Entity;

public class WrapperPlayServerLogin extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.LOGIN;

    public WrapperPlayServerLogin() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerLogin(PacketContainer packet) {
        super(packet, TYPE);
    }

    public int getEntityID() {
        return getHandle().getIntegers().read(0);
    }

    public void setEntityID(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public Entity getEntity(World world) {
        return getHandle().getEntityModifier(world).read(0);
    }

    public Entity getEntity(PacketEvent e) {
        return getEntity(e.getPlayer().getWorld());
    }

    public NativeGameMode getGamemode() {
        return getHandle().getGameModes().read(0);
    }

    public void setGamemode(NativeGameMode value) {
        getHandle().getGameModes().write(0, value);
    }

    public int getDimension() {
        return getHandle().getIntegers().read(0);
    }

    public void setDimension(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public int getMaxPlayers() {
        return getHandle().getIntegers().read(1);
    }

    public void setMaxPlayers(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public WorldType getLevelType() {
        return getHandle().getWorldTypeModifier().read(0);
    }

    public void setLevelType(WorldType value) {
        getHandle().getWorldTypeModifier().write(0, value);
    }

    public boolean getHardcore() {
        return getHandle().getBooleans().read(0);
    }

    public void setHardcore(boolean value) {
        getHandle().getBooleans().write(0, value);
    }
}