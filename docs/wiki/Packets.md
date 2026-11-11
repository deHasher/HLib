# ProtocolLib и пакеты

Packet API HLib работает только на Bukkit/Paper и требует совместимый ProtocolLib. Обёртки предоставляют удобные getters/setters поверх `PacketContainer`, но не скрывают различия протокола между версиями Minecraft.

## Подготовка

1. Установите ProtocolLib на сервер.
2. Добавьте API ProtocolLib и `HLib.jar` как `compileOnly` в проект.
3. Объявите ProtocolLib зависимостью своего плагина.
4. Регистрируйте listeners после загрузки ProtocolLib.

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

## Client wrappers

| Класс | Данные |
|---|---|
| `WrapperPlayClientChat` | строковый message клиентского CHAT |
| `WrapperPlayClientCustomPayload` | channel id без payload bytes |
| `WrapperPlayClientSetCreativeSlot` | short slot и `ItemStack` |
| `WrapperPlayClientTabComplete` | transaction id и input |
| `WrapperPlayClientUpdateSign` | block position и четыре строки |
| `WrapperPlayClientUseEntity` | target id, action и optional target vector |

## Server wrappers

| Класс | Данные |
|---|---|
| `WrapperPlayServerBlockChange` | position и block data |
| `WrapperPlayServerChat` | SYSTEM_CHAT: message и overlay |
| `WrapperPlayServerDamageEvent` | entity, DamageType, cause/direct ids и optional source position |
| `WrapperPlayServerEntityDestroy` | modern id list; overload int[] преобразуется в список |
| `WrapperPlayServerEntityEquipment` | entity и slot-item pairs |
| `WrapperPlayServerEntityMetadata` | entity metadata |
| `WrapperPlayServerEntityStatus` | entity и status byte |
| `WrapperPlayServerEntityTeleport` | position, velocity, rotation и on-ground |
| `WrapperPlayServerExplosion` | center, radius, block count и optional player velocity |
| `WrapperPlayServerGameStateChange` | game event reason/value |
| `WrapperPlayServerHurtAnimation` | entity id |
| `WrapperPlayServerLogin` | entity id, game mode, dimension key, max players и hardcore современного LOGIN |
| `WrapperPlayServerNamedSoundEffect` | sound, category, encoded position, volume, pitch |
| `WrapperPlayServerSetCooldown` | material и ticks |
| `WrapperPlayServerSpawnEntity` | id, UUID, type, position и rotation |
| `WrapperPlayServerTabComplete` | transaction id и преобразование native record в Brigadier suggestions |
| `WrapperPlayServerWorldEvent` | effect id, position и data |
| `WrapperPlayServerWorldParticles` | particle, position, offsets и count |

## Ограничения по версиям

- Wrapper’ы используют числовые индексы `StructureModifier`; изменение структуры пакета может сломать getter или setter без изменения Java-сигнатуры.
- Большинство классов не проверяет `PacketType.isSupported()`.
- Wrappers предоставляют выбранные поля пакетов: client CHAT не формирует подпись, LOGIN не создаёт полного состояния входа, UPDATE_SIGN сохраняет строки и сторону frontText.
- `WrapperPlayServerEntityTeleport` использует `ENTITY_POSITION_SYNC` и `WrappedPositionMoveRotation` ProtocolLib для позиции, скорости, yaw и pitch; legacy поля и reflection fallback удалены.
- `WrapperPlayServerEntityMetadata` читает и записывает modern data-value list; публичный список `WrappedWatchableObject` преобразуется в обе стороны.
- `WrapperPlayServerEntityDestroy.getCount()` и setters используют один modern int list.
- `WrapperPlayServerLogin` разделяет integer-индексы entity id и max players; game mode сохранён через nested `CommonPlayerSpawnInfo`, dimension читается и задаётся через `NamespacedKey`. Старые numeric dimension и `WorldType` accessors удалены: этих полей в современном пакете нет. Hardcore остаётся первым boolean.

После каждого обновления Minecraft или ProtocolLib запускайте интеграционные тесты именно на целевой версии сервера.

## Безопасность

По прямому указанию владельца `ProtocolLibHook.java` сохранён в исходном виде, включая `Methods.crash`, `CrashQueue` и `ChunkPacket`. Эти пути исключены из миграции: их реализация, version guards и deprecated calls не адаптировались. Этот факт не является подтверждением их работоспособности на целевой версии.

## Базовая версия 1.21.11

Ветки форматов пакетов старых серверов удалены из переносимых wrappers; исходный `ProtocolLibHook.java` оставлен отдельным исключением владельца. Cooldown передаёт resource key группы, spawn entity использует byte yaw/pitch, use-entity читает действие и координаты взаимодействия из `WrappedEnumEntityUseAction`. `setParticleTypeLegacy` удалён; частицы задаются через `WrappedParticle`. Компиляция проверяет доступность этих API в локальном ProtocolLib, но не заменяет проверку структуры пакетов на живом Paper 1.21.11.

`WrapperPlayServerExplosion` читает центр из `Vec3` и knockback из `Optional<Vec3>`; radius и blockCount используют отдельные поля. `getRecords`/`setRecords` удалены: список затронутых позиций отсутствует в пакете 1.21.11. Это изменение публичного packet API. `WrapperPlayClientCustomPayload` читает channel id через современный `CustomPacketPayloadWrapper`, без старого top-level key и reflection fallback. Для нового `WrapperPlayClientUpdateSign` сторона по умолчанию front; входящий пакет сохраняет свой frontText.

`WrapperPlayClientSetCreativeSlot` использует short slot; внешний int setter проверяет диапазон short. `WrapperPlayServerTabComplete` преобразует native поля id/start/length/entries через constructor и `toSuggestions()`; setter изменяет исходный PacketContainer. Полученный `Suggestions` — отдельный снимок: после фильтрации нужно вызвать `setSuggestions`, одной модификации списка недостаточно.

В `WrapperPlayServerDamageEvent` `DamageType` заменяет прежний numeric sourceType id, cause/direct имеют собственные integer-индексы, положение источника передаётся `Optional<Vector>`. Методы `getSourceTypeID`/`setSourceTypeID` и boolean `setSourcePosition` заменены актуальными типизированными методами. При создании пакета задаются GENERIC, неизвестные cause/direct (`-1`) и пустое положение.

Соответствие layouts проверено статически через `javap -private` на mappedServerJar Paper 1.21.11 и API локального ProtocolLib: Explosion, CustomPayload, SignUpdate, Particles, CreativeSlot, TabComplete, DamageEvent и остальные перенесённые wrappers. Это проверка полей и доступных конвертеров, без запуска Minecraft или отправки пакетов.

`WrapperPlayServerNamedSoundEffect.getSoundEffect()` получает имя из Registry key звука, сохраняя прежний формат `ENTITY_LIGHTNING_BOLT_THUNDER`. `Sound.toString()` больше не используется: в Paper 1.21.11 он описывает holder и не равен имени звука.

`WrapperPlayServerLogin.getGamemode()`/`setGamemode(NativeGameMode)` сохраняют прежний контракт и читают/изменяют `gameType` внутри `CommonPlayerSpawnInfo`, не затрагивая `previousGameType`. `getDimensionKey()`/`setDimensionKey(NamespacedKey)` работают с resource key измерения внутри той же структуры; изменение ключа не меняет отдельный dimension-type holder. Game mode и установка dimension key работают и у нового wrapper: ключ создаётся из `Registries.DIMENSION`, а не из прежнего nullable значения. Полная сериализация нового LOGIN требует также dimension-type holder и остальных параметров входа; wrapper не создаёт полное состояние нового входа. Удаление списка block positions из Explosion и numeric dimension/WorldType из LOGIN отражает изменения формата Minecraft.

Повторная проверка на реальных классах Paper 1.21.11 выявила незаполненные particle/sound в новом Explosion. Конструктор теперь задаёт `Particle.EXPLOSION` и `Sound.ENTITY_GENERIC_EXPLODE`; пустой `WeightedList` blockParticles корректно создаётся ProtocolLib. Новый и настроенный Explosion проходят native encode/decode. Настроенные Teleport, TabComplete, Destroy, Metadata, Spawn, Cooldown, Sign, UseEntity, NamedSound, CreativeSlot с пустым предметом, SystemChat и WorldParticles также прошли encode/decode в изолированном fixture. LOGIN проверен на независимость полей и установку ключа из null; полная сериализация LOGIN и DamageEvent в этом fixture не проверялась. У UseEntity действие требуется задать в underlying packet до работы с interaction vector; исходный default action=null не изменён.