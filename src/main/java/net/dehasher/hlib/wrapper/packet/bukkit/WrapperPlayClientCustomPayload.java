package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.MinecraftKey;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class WrapperPlayClientCustomPayload extends AbstractPacket {
    public static final PacketType TYPE = PacketType.Play.Client.CUSTOM_PAYLOAD;

    public WrapperPlayClientCustomPayload() {
        super(new PacketContainer(TYPE), TYPE);
        getHandle().getModifier().writeDefaults();
    }

    public WrapperPlayClientCustomPayload(PacketContainer packet) {
        super(packet, TYPE);
    }

    public MinecraftKey getChannel() {
        String string = readChannelString();
        if (string == null) return null;
        int i = string.indexOf(":");
        if (i <= 0) return new MinecraftKey("minecraft", string);
        return new MinecraftKey(string.substring(0, i), string.substring(i + 1));
    }

    private String readChannelString() {
        MinecraftKey minecraftKey = getHandle().getMinecraftKeys().readSafely(0);
        if (minecraftKey != null) return minecraftKey.getFullKey();

        try {
            Object nms = getHandle().getHandle();
            Field fPayload = nms.getClass().getDeclaredField("payload");
            fPayload.setAccessible(true);
            Object payload = fPayload.get(nms);

            try {
                Method mId = payload.getClass().getMethod("id");
                Object rl = mId.invoke(payload);
                return String.valueOf(rl);
            } catch (NoSuchMethodException e) {
                Method mType = payload.getClass().getMethod("type");
                Object type = mType.invoke(payload);
                Method mId2 = type.getClass().getMethod("id");
                Object rl = mId2.invoke(type);
                return String.valueOf(rl);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }
}