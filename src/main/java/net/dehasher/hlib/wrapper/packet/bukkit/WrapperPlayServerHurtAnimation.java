package net.dehasher.hlib.wrapper.packet.bukkit;

import org.bukkit.World;
import org.bukkit.entity.Entity;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class WrapperPlayServerHurtAnimation extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.HURT_ANIMATION;

    public WrapperPlayServerHurtAnimation() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerHurtAnimation(PacketContainer packet) {
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
}