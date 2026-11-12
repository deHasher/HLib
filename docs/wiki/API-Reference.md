# Каталог API

Ниже перечислены Java-файлы HLib `{{lib_version_plugin}}`. Каталог помогает быстро найти нужный класс, но не заменяет тематические страницы с примерами.

Обозначения:

- **Основной** — пригоден для обычного кода плагина.
- **Интеграция** — требует указанный сторонний плагин или API.
- **Низкоуровневый** — требует проверки на целевой версии сервера.
- **Внутренний** — создан для инфраструктуры HLib; использовать как стабильный API не рекомендуется.

Исходники: [`src/main/java/net/dehasher/hlib`](https://github.com/deHasher/HLib/tree/master/src/main/java/net/dehasher/hlib).

## Основной пакет

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `Colors` | Legacy-коды, HEX, градиенты HLib и Adventure-компоненты | Основной; MiniMessage-синтаксис не поддерживается |
| `Console` | Bukkit-обёртка `ConsoleCommandSender` над другим sender | Специализированный; права всегда разрешены, часть методов заглушена |
| `Cooldowner` | Cooldown по строковому ключу | Основной; обычные типы хранятся в памяти |
| `DateTimeFormatter` | Склонение единиц времени и форматирование длительности | Основной |
| `Experience` | Расчёт и изменение опыта Bukkit-игрока | Основной; Bukkit API |
| `HLibCfg` | Каталог собственных файлов конфигурации HLib | Внутренний |
| `Informer` | Сообщения, broadcast, title, action bar и HTTP helpers с заголовками | Основной; поведение зависит от платформы и типа сообщения; HTTP синхронный |
| `ItemBuilder` | Создание и изменение `ItemStack` | Основной для Paper; PlaceholderAPI используется при наличии |
| `Locationer` | Центр и сравнение блоков, направления, повороты и 2D-дистанция | Основной; Bukkit API |
| `Performance` | Локальные метрики и обмен через Redis, CPU/TPS/MSPT через Spark | Специализированный; `init()` и периодический `update()` вызываются внешним плагином |
| `Reflections` | Создание versioned NMS bridge и вызов его static-методов | Низкоуровневый; классы ищутся в пакете текущей NMS-версии |
| `Requestor` | Временный запрос между игроками с accept/cancel и callback | Специализированный; используйте `TimeUnit.SECONDS`, callback работает асинхронно |
| `Scheduler` | Планирование задач Bukkit и Velocity | Основной; используйте ветку своей платформы |
| `ShortNumbersFormatter` | Компактные числа с суффиксами | Основной |
| `StringRepairer` | Удаление bidi controls и RTL-диапазонов из строки | Специализированный; удаляет и обычный текст соответствующих письменностей |
| `Tools` | Общие проверки, парсинг, права, `getGeo(String/InetAddress)` и служебные операции | Смешанный API; GeoIP lookup локальный, результат nullable |
| `TelegramClient` | JSON Bot API transport с HTTP/SOCKS proxy | Синхронный `execute`, до четырёх запросов одновременно, владелец закрывает клиент |
| `Translator` | Загрузка vanilla locale JSON и регистрация Adventure-переводов | Bukkit-only; инициализация асинхронна и требует сети |
| `Updater` | Проверка версии и установка обновления указанного JAR | `check(name, version)` проверяет версию; `run(file)` читает корневой ресурс `hash` из указанного JAR до временного файла и скачивания |
| `Vector3D` | Неизменяемый трёхмерный вектор и операции с ним | Основной |

Практические примеры для этих классов находятся на странице [«Основной API»](Core-API).

## Команды

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `HCommandBukkit` | Базовый класс Bukkit-команды с cooldown, лимитом и `canExecute` | Специализированный; объект нужно отдельно зарегистрировать в Bukkit, permission должен существовать в `Permission`; `canExecute` по умолчанию разрешает действие и переопределяется для предварительной доменной проверки |
| `HCommandPreflight` | Предварительная проверка настроенных Bukkit-команд перед внешним действием | `command.HCommandPreflight.prepare(CommandSender, List<String>)` и `Prepared.check()` на main thread; результаты `ALLOWED`/`DENIED`/`UNAVAILABLE`, снимок команд и вызов их `canExecute`; команды не выполняются |
| `HCommandVelocity` | Аналогичная основа для `SimpleCommand` | Специализированный; нужны регистрация в Velocity и существующий элемент `Permission` |
| `HCommandAnimatedBukkit` | Учёт задач и игроков для анимированной команды | Специализированный; подкласс сам запускает и регистрирует задачу |

## Конфигурация HLib

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `config.Info` | Загруженные значения собственного `info.yml` HLib | Внутренний глобальный state |

## Controllers

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `APINotificationController` | Отправка служебного HTTP-уведомления | Внутренний; синхронная сеть с большими timeout |
| `AttributeController` | Типизированный доступ к Bukkit attributes через Registry | Низкоуровневый |
| `ClassController` | Кэшированная проверка наличия класса | Основной как служебная проверка |
| `DebugController` | Состояние debug-режима и его получателей | Внутренний |
| `DiscordController` | Состояние Discord-интеграции | Внутренний; сам клиент не создаёт |
| `DNSController` | Чтение DNS TXT-записей | Низкоуровневый; синхронный DNS |
| `GeoIPController` | Общий reader GeoLite2 City, обновление на старте и закрытие | Внутренний lifecycle; потребители вызывают `Tools.getGeo` |
| `ItemsController` | Проверки предметов, inventory и контейнеров | Смешанный Bukkit API; отдельные методы экспериментальны |
| `StorageController` | Глобальные коллекции состояния и сообщения команд | Специализированный; тексты отказа/cooldown/лимита по умолчанию пустые |

## Data

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `BukkitVersion` | Определение поколения Bukkit/Paper API | Низкоуровневый вспомогательный enum |
| `CompiledPattern` | Общий каталог заранее собранных regex: placeholders, аргументы, HTML, идентификаторы, HEX и namespaced keys | Основной; контракты описаны в [Core API](Core-API.md#общие-регулярные-выражения); IP-шаблон не является строгим валидатором |
| `Geo` | Record: код страны, локализованные страна/регион/город, координаты | Результат `Tools.getGeo`; координаты nullable |
| `HPlayer` | Кэш и SQL-модель игрока по числовому ID и каноническому имени; транзакционная фабрика учитывает внешние вставки и публикует кеш после commit | Специализированный; нужны глобальный MySQL, `initMySQL()` и заполнение кэша использующим плагином |
| `Logo` | Служебные действия для вывода логотипа | Внутренний |
| `Mod` | Каталог известных клиентских модов | Внутренний/специализированный |
| `NMS` | Выбор NMS-пакета, разрешение имён звуков и константы пакетов | Низкоуровневый и версионно-зависимый; эффекты, частицы, зачарования, типы сущностей и InventoryView используются через Paper API напрямую |
| `Permission` | Фиксированный enum прав | Внутренний; не конструктор произвольных permission nodes; ограничения описаны в [основном API](Core-API#базовые-классы-команд) |
| `Platform` | Определение Bukkit или Velocity | Основной; ОС проверяется отдельно через `Tools.isWindows()` |
| `Plugin` | Каталог и проверка известных плагинов, включая `HCOSMETIC`, `PACKET_EVENTS` и `PROTOCOL_LIB` | Интеграционный; первый результат кэшируется, Bukkit-проверка означает наличие плагина; запись ProtocolLib не включает его API или hook |
| `Table` | DDL таблиц и индексов | Внутренний; ориентирован на MariaDB |

Потребители удалённых enum-констант требуют обновления исходников и повторной сборки. Удалённые права также больше не распознаются `Permission.getEnum(...)`: метод выбрасывает `IllegalArgumentException` для отсутствующего значения.

## Базы данных

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `MySQL` | HikariCP-пул, builder подготовленных запросов и callback-транзакции | Основной; все JDBC-операции синхронные; `Transaction` предоставляет affected rows, 64-bit generated key и typed query |
| `MySQLTable` | Интерфейс SQL/DDL-константы с методом `getValue()` | Основной; SQL формирует реализующий enum или класс |
| `Redis` | Jedis-пул, publish и базовый Pub/Sub | Основной; подписка блокирует выделенный поток |

Подключение, закрытие ресурсов и рабочие примеры: [«Базы данных»](Databases).

## Высокоуровневые конфигурации

В этом разделе `file.ConfigurationSection` — пустой маркер Java-объекта. Не путайте его с полным YAML-интерфейсом `file.configuration.ConfigurationSection` ниже.

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `file.Annotations` | Контейнер `@Key`, `@Comment`, `@Final`, `@Ignore` | Основной |
| `file.Configuration` | Reflection-модель и builder объектной конфигурации | Основной; сначала нужен `load()`, затем `save()` |
| `file.ConfigurationProvider` | SPI источника конфигурации | Расширение API |
| `file.ConfigurationSection` | Маркер вложенной секции модели | Основной |
| `file.ConfigurationSettingsSerializer` | SPI сериализатора настроек | Расширение API |
| `file.ConfigurationUtils` | Загрузка полей с сохранением вложенности `Map` и полная генерация YAML | Низкоуровневая основа объектной модели |
| `file.PluginCfg` | Контракт enum-каталога конфигов для `Tools.reloadFiles(...)` | Специализированный; миграцию схемы реализует `reload(...)` |
| `file.provider.StandaloneConfigurationProvider` | Общий файловый YAML-provider для обеих платформ | Основной; отдельный provider-плагин не требуется |
| `file.util.Files` | Создание родительских директорий | Вспомогательный |
| `file.util.NumberConversions` | Безопасные числовые преобразования | Вспомогательный |
| `file.util.Validate` | Проверки аргументов и состояния | Вспомогательный |

Ограничения полей, массивов и повторного сохранения разобраны на странице [«Конфигурации»](Configuration).

## Низкоуровневый YAML API

Это автономная Bukkit-подобная реализация YAML; она не требует наследования от высокоуровневого `file.Configuration`.

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `file.configuration.Configuration` | Корневая конфигурация с defaults и options | Основной низкоуровневый API |
| `file.configuration.ConfigurationOptions` | Разделитель пути и копирование defaults | Основной |
| `file.configuration.ConfigurationSection` | Typed API YAML-секции | Основной |
| `file.configuration.InvalidConfigurationException` | Checked-ошибка разбора | Основной |
| `file.configuration.MemoryConfiguration` | Корневая in-memory конфигурация | Основной |
| `file.configuration.MemoryConfigurationOptions` | Options in-memory конфигурации | Основной |
| `file.configuration.MemorySection` | Реализация путей, секций и typed getters | Используется через корневую конфигурацию или `createSection()` |
| `file.configuration.file.FileConfiguration` | UTF-8 загрузка и сохранение файла | Основной |
| `file.configuration.file.FileConfigurationOptions` | Header и copyHeader | Основной |
| `file.configuration.file.YamlConfiguration` | SnakeYAML load/save API | Основной; для полной перезагрузки лучше новый объект |
| `file.configuration.file.YamlConfigurationOptions` | Настройки YAML | Часть настроек имеет ограничения реализации |
| `file.configuration.file.YamlConstructor` | Безопасный конструктор YAML-объектов | Низкоуровневая реализация |
| `file.configuration.file.YamlRepresenter` | Представление секций и serializable-объектов | Низкоуровневая реализация |
| `file.configuration.serialization.ConfigurationSerializable` | Контракт сериализации в `Map` | Основной |
| `file.configuration.serialization.ConfigurationSerialization` | Реестр aliases и reflection-десериализация | Основной; регистрируйте классы до загрузки |
| `file.configuration.serialization.DelegateDeserialization` | Делегирование десериализации другому классу | Расширение API |
| `file.configuration.serialization.SerializableAs` | Короткий alias serializable-класса | Основной |

## Интеграции

Все hook-классы находятся в `net.dehasher.hlib.hook`. Большинство требует соответствующий API в runtime; `PlaceholderAPIHook` отдельно проверяет наличие плагина и при его отсутствии возвращает вход без изменений.

| Класс | Сторонний компонент | Назначение |
| --- | --- | --- |
| `BotSentryHook` | BotSentry | Добавление IP в whitelist |
| `ChunkyBorderHook` | ChunkyBorder | Чтение центра и размера границы из `borders.json` |
| `CMIHook` | CMI и CMILib | Пользователи, vanish, god mode, speed, metadata |
| `EmoteCraftHook` | Emotecraft | Запуск и остановка анимаций |
| `ForceResourcepacksHook` | ForceResourcepacks | Получение и обновление resource pack |
| `HCosmeticHook` | HCosmetic service provider, опционально | `getProvider`, `isAvailable`, `unequipGadget(Player)`, `unequipSuit(Player)`; отсутствующий/выключенный provider — no-op |
| `ItemJoinHook` | ItemJoin | Проверка custom item |
| `ItemsAdderHook` | ItemsAdder | Custom items, модели, permission и pack metadata |
| `LibsDisguisesHook` | LibsDisguises | Проверка и снятие disguise |
| `LiteBansHook` | LiteBans | Проверка наказаний и чтение записей |
| `LuckPermsHook` | LuckPerms | Prefix/suffix, права, группы и expiry |
| `NexoHook` | Nexo | Custom items, модели, permission и YAML item config |
| `PlaceholderAPIHook` | PlaceholderAPI, опционально | Подстановка placeholders или возврат исходной строки/списка |
| `PlasmoVoiceHook` | Plasmo Voice | Константа permission для речи |
| `PacketEventsHook` | PacketEvents | Listeners с владельцем, отправка нативных wrappers, fake armor stand, game-state events, клиентские изменения блоков и тестовая нагрузка |
| `SparkHook` | spark | CPU, TPS и MSPT |
| `TABHook` | TAB, местами LuckPerms/CMI | Prefix/suffix и группы TAB |
| `UltimateTimberHook` | UltimateTimber | Проверка анимированного падающего блока |
| `VaultHook` | Vault и economy provider | Получение economy API |
| `WorldEditHook` | WorldEdit | Выделение и вертикальное расширение |
| `WorldGuardHook` | WorldGuard | Regions, flags, membership и build checks |

Таблица совместимости и безопасные примеры: [«Интеграции»](Integrations).

## Точки входа платформ

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `platform.bukkit.HLib` | Bukkit/Paper entrypoint, инициализация и обновление | Внутренний entrypoint библиотеки |
| `platform.velocity.HLib` | Velocity entrypoint, инициализация и обновление | Внутренний entrypoint библиотеки |

Один release JAR содержит обе точки входа и соответствующие descriptors. Общий ресурс [`src/main/resources/hash`](https://github.com/deHasher/HLib/blob/master/src/main/resources/hash) упаковывается как корневой entry `hash`; обе платформы используют его через `Updater.run(file)`. Значение больше не хранится в Java-полях или Lombok-accessors.

## PacketEvents

Собственный пакет `net.dehasher.hlib.wrapper.packet.bukkit` удалён целиком (26 Java-файлов). Вместо него используются `com.github.retrooper.packetevents.wrapper.play.client` и `.server` из отдельного PacketEvents {{dependency_version_packetevents}}. API HLib не содержит адаптеров совместимости со старыми wrappers.

`PacketEventsHook` — высокоуровневый helper, а не собственный codec: `ArmorStand` возвращает нативные spawn/metadata/equipment wrappers, `GameEvent` отправляет game-state пакет, регистрация listeners хранит владельца для корректного снятия при отключении.

Примеры и lifecycle: [PacketEvents и пакеты](Packets).

## Как выбрать API

Для обычного плагина разумный порядок такой:

1. Начните с `Colors`, `Informer`, `Scheduler`, `Cooldowner`, `ItemBuilder` и объектной конфигурации.
2. Подключайте `MySQL` или `Redis` только вместе с отдельным executor и явным закрытием ресурсов.
3. Проверяйте optional plugin перед загрузкой соответствующего hook-класса.
4. Используйте packet wrappers и NMS только после теста на каждой поддерживаемой версии Minecraft.