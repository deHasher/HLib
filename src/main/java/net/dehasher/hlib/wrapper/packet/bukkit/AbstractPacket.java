package net.dehasher.hlib.wrapper.packet.bukkit;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.google.common.base.Objects;
import net.dehasher.hlib.Informer;

@Getter
@Setter
public abstract class AbstractPacket {

    protected PacketContainer handle;

    protected AbstractPacket(PacketContainer handle, PacketType type) {
        if (handle == null) throw new IllegalArgumentException("Packet handle cannot be NULL.");
        if (!Objects.equal(handle.getType(), type)) throw new IllegalArgumentException(handle.getHandle() + " is not a packet of type " + type);
        setHandle(handle);
    }

    public void sendPacket(Player receiver) {
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(receiver, getHandle());
        } catch (Throwable t) {
            Informer.send("Cannot send packet: " + getHandle().getType().name());
            t.printStackTrace();
        }
    }

    public void broadcastPacket() {
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(getHandle());
    }

    public void receivePacket(Player sender) {
        try {
            ProtocolLibrary.getProtocolManager().receiveClientPacket(sender, getHandle());
        } catch (Throwable t) {
            throw new RuntimeException("Cannot receive packet.", t);
        }
    }
}