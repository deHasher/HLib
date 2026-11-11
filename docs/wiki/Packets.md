# PacketEvents и пакеты

Packet API HLib работает на Paper 1.21.11+ с отдельным плагином PacketEvents {{dependency_version_packetevents}}. Собственные 26 packet wrappers и ProtocolLibHook удалены. Используются нативные wrappers PacketEvents; совместимые aliases старого API не предоставляются.

## Подключение

1. Установите официальный `packetevents-spigot-{{dependency_version_packetevents}}.jar` рядом с HLib и своим плагином.
2. Добавьте PacketEvents как `compileOnly` в сборку потребителя.
3. Объявите `packetevents` в `paper-plugin.yml`, сохраняя точный регистр имени.
4. Регистрируйте listeners в `onEnable`, снимайте их при отключении своего плагина.

### Файл `build.gradle.kts`

```kotlin
repositories {
	maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
	compileOnly(files("libs/HLib.jar"))
	compileOnly("com.github.retrooper:packetevents-spigot:{{dependency_version_packetevents}}")
}
```

### Файл `paper-plugin.yml`

```yaml
dependencies: {server: {HLib: {load: BEFORE, required: true, join-classpath: true}, packetevents: {load: BEFORE, required: true, join-classpath: true}}}
```

Сам HLib объявляет PacketEvents необязательной зависимостью, чтобы общие API оставались доступны без пакетных функций и на Velocity. В HCore обязательное объявление PacketEvents сохраняется только в ранее содержавших packet dependency manifests: HCore loader, HCalendar, HAntiRelog и HImages. В остальные manifests новые записи не добавляются. Вызовы пакетного API выполняются без дополнительных проверок наличия PacketEvents. PacketEvents не включается в Shadow JAR HLib и не инициализируется повторно: `load/init/terminate` выполняет установленный плагин PacketEvents.

В каталоге `Plugin` сохранены обе записи: `PACKET_EVENTS("packetevents")` и `PROTOCOL_LIB("ProtocolLib")`. Запись ProtocolLib служит только для идентификации стороннего плагина; его hook, собственные packet wrappers и зависимость сборки не возвращаются.

## Чтение и изменение пакетов

### Файл `InteractionListener.java`

```java
package com.example.plugin;

import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

public final class InteractionListener extends PacketListenerAbstract {
	@Override
	public void onPacketReceive(PacketReceiveEvent event) {
		if (event.getPacketType() != PacketType.Play.Client.INTERACT_ENTITY) return;
		WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
		if (packet.getEntityId() < 0) event.setCancelled(true);
	}
}
```

Регистрация: `PacketEventsHook.register(plugin, new InteractionListener())`. Снятие одного listener: `PacketEventsHook.unregister(listener)`; снятие всех listeners владельца: `PacketEventsHook.unregisterAll(plugin)`. HLib хранит владельца явно, потому что PacketEvents EventManager не связывает listener с Bukkit plugin автоматически. Дополнительно `PluginDisableEvent` снимает listeners отключаемого владельца. Обработчики других плагинов не удаляются.

Входящие и исходящие callbacks PacketEvents работают в сетевых потоках. Не обращайтесь там к миру, инвентарям и изменяемому Bukkit state; подготовьте снимок необходимых данных на главном потоке. Для Bukkit-действия запланируйте sync callback и повторно проверьте подключение игрока. `PacketEventsHook.Methods.getPlayer(event)` возвращает только настоящий online `Player` либо `null`, однако сам по себе не переключает поток.

После изменения wrapper входящего/исходящего события вызывайте `event.markForReEncode(true)`, когда меняется его содержимое. Не сохраняйте событие или его ByteBuf после завершения callback.

## Отправка

`PacketEventsHook.sendPacket(player, packet)` делегирует native `PacketWrapper<?>` в PlayerManager PacketEvents. Исключение не подавляется. Отправка ставит данные в сетевой канал и не является подтверждением получения клиентом.

Для client-side изменения блока:

```java
WrappedBlockState block = SpigotConversionUtil.fromBukkitBlockData(Material.GOLD_BLOCK.createBlockData());
WrapperPlayServerBlockChange packet = new WrapperPlayServerBlockChange(
	new Vector3i(location.getBlockX(), location.getBlockY(), location.getBlockZ()),
	block.getGlobalId()
);
PacketEventsHook.sendPacket(player, packet);
```

Классы wrappers импортируются из `com.github.retrooper.packetevents.wrapper.play.server`, `Vector3i` — из `com.github.retrooper.packetevents.util`, `WrappedBlockState` — из `com.github.retrooper.packetevents.protocol.world.states`, `SpigotConversionUtil` — из `io.github.retrooper.packetevents.util`. Настоящий мир при отправке такого пакета не меняется.

## Fake armor stand

```java
WrapperPlayServerSpawnEntity spawn = PacketEventsHook.ArmorStand.create(location);
PacketEventsHook.sendPacket(player, spawn);
PacketEventsHook.sendPacket(player, PacketEventsHook.ArmorStand.setup(spawn, true, true, Colors.set("&eТекст")));
PacketEventsHook.sendPacket(player, PacketEventsHook.ArmorStand.equip(spawn, EquipmentSlot.HELMET, item));
```

`EquipmentSlot` принадлежит `com.github.retrooper.packetevents.protocol.player`. `equip` принимает Bukkit `ItemStack` и преобразует его через официальный SpigotConversionUtil. Metadata использует типизированные `EntityData<?>`, включая `OPTIONAL_ADV_COMPONENT` для имени. `create` и `setup` возвращают native wrappers и ничего не отправляют самостоятельно.

Удаление: `PacketEventsHook.ArmorStand.remove(player, spawn)`. Вызов с `null` вместо игрока рассылает destroy всем online players. Entity id сохраняет прежний диапазон `short`; коллизии с реальными или другими fake-сущностями возможны. Владелец визуального объекта хранит id и выполняет удаление при завершении анимации.

## Game-state события и блоки

`PacketEventsHook.GameEvent` сохраняет `guardian`, `demo`, `survival`, `creative`, `adventure`, `spectator` и overloads `run(Player)`/`run(String)`. Клиентское изменение game mode не меняет настоящее состояние сервера.

`PacketEventsHook.ChunkPacket.sendVisible(player, WrappedBlockState)` отправляет `MULTI_BLOCK_CHANGE` для загруженных чанков в пределах минимального client/world view distance. Каждая секция содержит 4096 замен; отрицательная высота мира учитывается через `floorDiv(minHeight, 16)`. Вызов требует главный поток для чтения мира. Метод не подгружает отсутствующие чанки, не меняет server-side блоки и не создаёт block entities.

## Тестовая нагрузка клиента

`PacketEventsHook.Methods.crash(owner, player)` сохраняет сценарий владельца: client-side замена видимых загруженных секций на end gateway и серия из 666666 случайных spawn-entity пакетов; после завершения серии следующая планируется через 200 тиков. Количество пакетов и набор сущностей сохранены. Это нагрузка также на серверную сериализацию и сетевой канал, а не измеритель одной только мощности клиентской машины; время сбоя или результат не гарантированы.

Проверки NPC и истории последних трёх имён сохранены. Снимок положения, высот и загруженных секций создаётся на главном потоке; отправка выполняется асинхронно. Активная сессия отменяется при выходе игрока, отключении владельца, HLib или PacketEvents, а также при ошибке отправки. `CrashQueue.remove(name)` удаляет имя и из множества, и из очереди; `clear()` очищает обе структуры. Права и выбор разрешённого тестового клиента проверяет вызывающая команда, HLib самостоятельно не предоставляет команду.

## Проверка и ограничения

Компиляция не подтверждает работу сетевого pipeline на настоящем сервере. После обновления Minecraft или PacketEvents проверяйте используемые пакеты на целевой версии; metadata indices HLib остаются версионными данными. Нагрузочный сценарий, plugin startup, настоящая отправка клиенту, БД и внешний updater при миграции не запускались. Результат isolated-проверок и сборки находится в [руководстве](Building-and-Troubleshooting#переход-на-packetevents).

Официальные источники: [PacketEvents 2.14.0](https://github.com/retrooper/packetevents/releases/tag/v2.14.0), [исходники wrappers](https://github.com/retrooper/packetevents/tree/v2.14.0/api/src/main/java/com/github/retrooper/packetevents/wrapper), [документация](https://docs.packetevents.com/), [Javadoc](https://javadocs.packetevents.com/).