package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedParticle;

public class WrapperPlayServerWorldParticles extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.WORLD_PARTICLES;

    public WrapperPlayServerWorldParticles() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerWorldParticles(PacketContainer packet) {
        super(packet, TYPE);
    }

    public void setParticleTypeLegacy(EnumWrappers.Particle value) {
        getHandle().getParticles().write(0, value);
    }

    @SuppressWarnings("rawtypes")
    public void setParticleType(WrappedParticle value) {
        getHandle().getNewParticles().write(0, value);
    }

    public void setX(double value) {
        getHandle().getDoubles().write(0, value);
    }

    public void setY(double value) {
        getHandle().getDoubles().write(1, value);
    }

    public void setZ(double value) {
        getHandle().getDoubles().write(2, value);
    }

    public void setOffsetX(float value) {
        getHandle().getFloat().write(0, value);
    }

    public void setOffsetY(float value) {
        getHandle().getFloat().write(1, value);
    }

    public void setOffsetZ(float value) {
        getHandle().getFloat().write(2, value);
    }

    public void setNumberOfParticles(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public void setLongDistance(boolean value) {
        getHandle().getBooleans().write(0, value);
    }
}