# ProtocolLib и пакеты

Packet API HLib работает только на Bukkit/Paper и требует совместимый ProtocolLib. Обёртки предоставляют удобные getters/setters поверх `PacketContainer`, но не скрывают различия протокола между версиями Minecraft.

## Подготовка

1. Установите ProtocolLib на сервер.
2. Добавьте API ProtocolLib и `HLib.jar` как `compileOnly` в проект.
3. Объявите ProtocolLib зависимостью своего плагина.
4. Регистрируйте listeners после загрузки ProtocolLib.

### Файл `plugin.yml`

```yaml
depend: ["HLib", "ProtocolLib"]
```

### Файл `paper-plugin.yml`

```yaml
dependencies: {server: {HLib: {load: "BEFORE", required: true, join-classpath: true}, ProtocolLib: {load: "BEFORE", required: true, join-classpath: true}}}
```

## Чтение клиентского пакета

### Файл `UseEntityPacketListener.java`

```java
package com.example.plugin.packet;

import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import net.dehasher.hlib.wrapper.packet.bukkit.WrapperPlayClientUseEntity;
import org.bukkit.plugin.Plugin;

public final class UseEntityPacketListener extends PacketAdapter {
	public UseEntityPacketListener(Plugin plugin) {
		super(plugin, WrapperPlayClientUseEntity.TYPE);
	}

	@Override
	public void onPacketReceiving(PacketEvent event) {
		WrapperPlayClientUseEntity packet = new WrapperPlayClientUseEntity(event.getPacket());
		int targetId = packet.getTargetID();
		String action = packet.getAction();

		if (targetId < 0 || action == null) {
			event.setCancelled(true);
		}
	}
}
```

### Файл `ExamplePlugin.java`

```java
package com.example.plugin;

import com.example.plugin.packet.UseEntityPacketListener;
import net.dehasher.hlib.hook.ProtocolLibHook;
import org.bukkit.plugin.java.JavaPlugin;

public final class ExamplePlugin extends JavaPlugin {
	private UseEntityPacketListener packetListener;

	@Override
	public void onEnable() {
		packetListener = new UseEntityPacketListener(this);
		ProtocolLibHook.register(packetListener);
	}

	@Override
	public void onDisable() {
		ProtocolLibHook.unregisterAll(this);
	}
}
```

## Отправка client-side изменения блока

Изменение увидит только указанный игрок; настоящий блок мира не меняется.

```java
WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange();
packet.setLocation(new BlockPosition(
	location.getBlockX(),
	location.getBlockY(),
	location.getBlockZ()
));
packet.setBlockData(WrappedBlockData.createData(Material.GOLD_BLOCK));
packet.sendPacket(player);
```

`sendPacket` логирует и подавляет ошибку отправки. Если вызывающему коду нужен гарантированный результат, обрабатывайте пакет через ProtocolLib напрямую.

## Fake armor stand

```java
WrapperPlayServerSpawnEntity spawn = ProtocolLibHook.ArmorStand.create(location);
WrapperPlayServerEntityMetadata metadata = ProtocolLibHook.ArmorStand.setup(
	spawn,
	true,
	true,
	Colors.set("&eТекст")
);

spawn.sendPacket(player);
metadata.sendPacket(player);
```

HLib генерирует случайный entity id в диапазоне `short`; коллизии с реальными или другими fake-сущностями теоретически возможны. Храните wrapper и отправляйте destroy packet при удалении визуального объекта.

```java
ProtocolLibHook.ArmorStand.remove(player, spawn);
```

`ArmorStand.equip(...)` создаёт packet экипировки; его тоже нужно отправить игроку явно. `remove(null, ...)` рассылает удаление всем игрокам.

## Другие helpers

- `ProtocolLibHook.Methods.getPlayer(event)` исключает временных и отключённых игроков и может вернуть `null`.
- `ProtocolLibHook.GameEvent` отправляет клиенту отдельные game-state события. Изменение клиентского режима не меняет настоящий game mode сервера.
- `ProtocolLibHook.ChunkPacket.sendVisible(player, blockData)` заменяет для клиента все блоки во всех загруженных чанках в квадрате его view distance. Реальный мир не меняется, автоматического восстановления нет. Метод проходит все вертикальные секции, может отправить большой объём данных и требует отдельной проверки на целевом протоколе; для одной позиции используйте `WrapperPlayServerBlockChange`.

## Client wrappers

| Класс | Данные |
|---|---|
| `WrapperPlayClientChat` | legacy chat message |
| `WrapperPlayClientCustomPayload` | channel id без payload bytes |
| `WrapperPlayClientSetCreativeSlot` | slot и `ItemStack` |
| `WrapperPlayClientTabComplete` | transaction id и input |
| `WrapperPlayClientUpdateSign` | block position и четыре строки |
| `WrapperPlayClientUseEntity` | target id, action и optional target vector |

## Server wrappers

| Класс | Данные |
|---|---|
| `WrapperPlayServerBlockChange` | position и block data |
| `WrapperPlayServerChat` | legacy chat component/type/position |
| `WrapperPlayServerDamageEvent` | entity и damage source ids |
| `WrapperPlayServerEntityDestroy` | legacy array или modern id list |
| `WrapperPlayServerEntityEquipment` | entity и slot-item pairs |
| `WrapperPlayServerEntityMetadata` | entity metadata |
| `WrapperPlayServerEntityStatus` | entity и status byte |
| `WrapperPlayServerEntityTeleport` | position, velocity, rotation и on-ground |
| `WrapperPlayServerExplosion` | center, radius, blocks и player velocity |
| `WrapperPlayServerGameStateChange` | game event reason/value |
| `WrapperPlayServerHurtAnimation` | entity id |
| `WrapperPlayServerLogin` | legacy login fields |
| `WrapperPlayServerNamedSoundEffect` | sound, category, encoded position, volume, pitch |
| `WrapperPlayServerSetCooldown` | material и ticks |
| `WrapperPlayServerSpawnEntity` | id, UUID, type, position и rotation |
| `WrapperPlayServerTabComplete` | transaction id и Brigadier suggestions |
| `WrapperPlayServerWorldEvent` | effect id, position и data |
| `WrapperPlayServerWorldParticles` | particle, position, offsets и count |

## Ограничения по версиям

- Wrapper’ы используют числовые индексы `StructureModifier`; изменение структуры пакета может сломать getter или setter без изменения Java-сигнатуры.
- Большинство классов не проверяет `PacketType.isSupported()`.
- Legacy chat, login и sign packet не покрывают все современные поля подписанного чата и новых протоколов.
- `WrapperPlayServerEntityTeleport` использует reflection для modern `PositionMoveRotation`; при несовпадении NMS-структуры getter может вернуть `0`, а setter стать no-op.
- `WrapperPlayServerEntityMetadata` имеет разные modern/legacy пути записи и чтения.
- `WrapperPlayServerEntityDestroy.getCount()` читает legacy array и не подходит modern list.
- `WrapperPlayServerLogin` имеет конфликтующие integer-индексы: dimension и max players могут перезаписать entity id. Не используйте его setters до исправления и отдельного теста.

После каждого обновления Minecraft или ProtocolLib запускайте интеграционные тесты именно на целевой версии сервера.

## Безопасность

В `ProtocolLibHook` присутствуют внутренние административные методы, создающие длительные packet-flood задачи. Они не относятся к публично рекомендуемому API, могут нарушить работу клиента или сервера и здесь намеренно не документируются.