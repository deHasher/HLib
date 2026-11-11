package net.dehasher.hlib.wrapper.packet.bukkit;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.events.InternalStructure;
import com.comphenix.protocol.reflect.accessors.Accessors;
import com.comphenix.protocol.reflect.accessors.MethodAccessor;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers.NativeGameMode;
import com.comphenix.protocol.wrappers.MinecraftKey;
import org.bukkit.NamespacedKey;
import java.util.Objects;
import org.bukkit.World;
import org.bukkit.entity.Entity;

public class WrapperPlayServerLogin extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.LOGIN;
	private static final Class<?> SPAWN_INFO = MinecraftReflection.getMinecraftClass("network.protocol.game.CommonPlayerSpawnInfo");
	private static final Class<?> RESOURCE_KEY = MinecraftReflection.getMinecraftClass("resources.ResourceKey");
	private static final MethodAccessor KEY_IDENTIFIER = Accessors.getMethodAccessor(RESOURCE_KEY, "identifier");
	private static final Object DIMENSION_REGISTRY = Objects.requireNonNull(Accessors.getFieldAccessorOrNull(MinecraftReflection.getMinecraftClass("core.registries.Registries"), "DIMENSION", RESOURCE_KEY)).get(null);
	private static final MethodAccessor KEY_CREATE = Accessors.getMethodAccessor(RESOURCE_KEY, "create", RESOURCE_KEY, MinecraftReflection.getMinecraftKeyClass());

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
		return getSpawnInfo().getGameModes().read(0);
	}

	public void setGamemode(NativeGameMode value) {
		getSpawnInfo().getGameModes().write(0, Objects.requireNonNull(value));
	}

	public NamespacedKey getDimensionKey() {
		Object key = getSpawnInfo().getModifier().withType(RESOURCE_KEY).read(0);
		MinecraftKey identifier = MinecraftKey.fromHandle(KEY_IDENTIFIER.invoke(key));
		return NamespacedKey.fromString(identifier.getFullKey());
	}

	public void setDimensionKey(NamespacedKey value) {
		Objects.requireNonNull(value);
		InternalStructure spawnInfo = getSpawnInfo();
		Object identifier = MinecraftKey.getConverter().getGeneric(new MinecraftKey(value.getNamespace(), value.getKey()));
		Object replacement = KEY_CREATE.invoke(null, DIMENSION_REGISTRY, identifier);
		spawnInfo.getModifier().withType(RESOURCE_KEY).write(0, replacement);
	}

	private InternalStructure getSpawnInfo() {
		return Objects.requireNonNull(getHandle().getModifier().withType(SPAWN_INFO, InternalStructure.getConverter()).read(0), "Login packet has no spawn information");
	}

	public int getMaxPlayers() {
		return getHandle().getIntegers().read(1);
	}

	public void setMaxPlayers(int value) {
		getHandle().getIntegers().write(1, value);
	}

	public boolean getHardcore() {
		return getHandle().getBooleans().read(0);
	}

	public void setHardcore(boolean value) {
		getHandle().getBooleans().write(0, value);
	}
}