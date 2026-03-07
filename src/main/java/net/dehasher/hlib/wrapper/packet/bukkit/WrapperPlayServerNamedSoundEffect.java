package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers.SoundCategory;

public class WrapperPlayServerNamedSoundEffect extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Server.NAMED_SOUND_EFFECT;

    public WrapperPlayServerNamedSoundEffect() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayServerNamedSoundEffect(PacketContainer packet) {
        super(packet, TYPE);
    }

    public String getSoundEffect() {
        if (getHandle().getSoundEffects() == null || getHandle().getSoundEffects().size() == 0) return "";
        String data = getHandle().getSoundEffects().read(0) != null ? String.valueOf(getHandle().getSoundEffects().read(0)) : ""; // Тут нельзя .toString()
        return data.replace(".", "_").toUpperCase();
    }

    public SoundCategory getSoundCategory() {
        return getHandle().getSoundCategories().read(0);
    }

    public void setSoundCategory(SoundCategory value) {
        getHandle().getSoundCategories().write(0, value);
    }

    public int getEffectPositionX() {
        return getHandle().getIntegers().read(0);
    }

    public void setEffectPositionX(int value) {
        getHandle().getIntegers().write(0, value);
    }

    public int getEffectPositionY() {
        return getHandle().getIntegers().read(1);
    }

    public void setEffectPositionY(int value) {
        getHandle().getIntegers().write(1, value);
    }

    public int getEffectPositionZ() {
        return getHandle().getIntegers().read(2);
    }

    public void setEffectPositionZ(int value) {
        getHandle().getIntegers().write(2, value);
    }

    public float getVolume() {
        return getHandle().getFloat().read(0);
    }

    public void setVolume(float value) {
        getHandle().getFloat().write(0, value);
    }

    public float getPitch() {
        return getHandle().getFloat().read(1);
    }

    public void setPitch(float value) {
        getHandle().getFloat().write(1, value);
    }
}