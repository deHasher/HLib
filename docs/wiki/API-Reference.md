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
| `Tools` | Общие проверки, парсинг, права и служебные операции | Смешанный API |
| `Translator` | Загрузка vanilla locale JSON и регистрация Adventure-переводов | Bukkit-only; инициализация асинхронна и требует сети |
| `Updater` | Проверка и установка обновлений HLib | Внутренний |
| `Vector3D` | Неизменяемый трёхмерный вектор и операции с ним | Основной |

Практические примеры для этих классов находятся на странице [«Основной API»](Core-API).

## Команды

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `HCommandBukkit` | Базовый класс Bukkit-команды с cooldown и лимитом | Специализированный; объект нужно отдельно зарегистрировать в Bukkit, а permission должен существовать в `Permission` |
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
| `AttributeController` | Reflection-доступ к Bukkit attributes разных версий | Низкоуровневый |
| `ClassController` | Кэшированная проверка наличия класса | Основной как служебная проверка |
| `DebugController` | Состояние debug-режима и его получателей | Внутренний |
| `DiscordController` | Состояние Discord-интеграции | Внутренний; сам клиент не создаёт |
| `DNSController` | Чтение DNS TXT-записей | Низкоуровневый; синхронный DNS |
| `ItemsController` | Проверки предметов, inventory и контейнеров | Смешанный Bukkit API; отдельные методы экспериментальны |
| `StorageController` | Глобальные коллекции состояния и сообщения команд | Специализированный; тексты отказа/cooldown/лимита по умолчанию пустые |

## Data

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `BukkitVersion` | Определение поколения Bukkit/Paper API | Низкоуровневый вспомогательный enum |
| `CompiledPattern` | Набор заранее собранных регулярных выражений | Основной; IP-шаблон не является строгим валидатором |
| `HPlayer` | Кэш и SQL-модель игрока по числовому ID и имени | Специализированный; нужны глобальный MySQL, `initMySQL()` и заполнение кэша использующим плагином |
| `Logo` | Служебные действия для вывода логотипа | Внутренний |
| `Mod` | Каталог известных клиентских модов | Внутренний/специализированный |
| `NMS` | Reflection и индексы metadata для разных версий Minecraft | Низкоуровневый и версионно-зависимый |
| `Permission` | Фиксированный enum прав экосистемы | Внутренний; не конструктор произвольных permission nodes |
| `Platform` | Определение Bukkit или Velocity | Основной; ОС проверяется отдельно через `Tools.isWindows()` |
| `Plugin` | Каталог и проверка известных плагинов | Интеграционный; первый результат кэшируется, Bukkit-проверка означает наличие плагина |
| `Table` | DDL таблиц и индексов | Внутренний; ориентирован на MariaDB |

## Базы данных

| Класс | Назначение | Статус и важные условия |
| --- | --- | --- |
| `MySQL` | HikariCP-пул и builder подготовленных запросов | Основной; все JDBC-операции синхронные |
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
| `file.ConfigurationUtils` | Загрузка полей и полная генерация YAML | Низкоуровневая основа объектной модели |
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
| `GadgetsMenuHook` | GadgetsMenu | Получение `PlayerManager` |
| `ItemJoinHook` | ItemJoin | Проверка custom item |
| `ItemsAdderHook` | ItemsAdder | Custom items, модели, permission и pack metadata |
| `LibsDisguisesHook` | LibsDisguises | Проверка и снятие disguise |
| `LimboAuthHook` | LimboAuth | Создание SQL-подключения по его конфигу |
| `LiteBansHook` | LiteBans | Проверка наказаний и чтение записей |
| `LuckPermsHook` | LuckPerms | Prefix/suffix, права, группы и expiry |
| `NexoHook` | Nexo | Custom items, модели, permission и YAML item config |
| `PlaceholderAPIHook` | PlaceholderAPI, опционально | Подстановка placeholders или возврат исходной строки/списка |
| `PlasmoVoiceHook` | Plasmo Voice | Константа permission для речи |
| `ProtocolLibHook` | ProtocolLib | Listeners, fake armor stand, game-state events и клиентские изменения блоков |
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

Один release JAR содержит обе точки входа и соответствующие descriptors.

## ProtocolLib packet wrappers

Все классы ниже находятся в `net.dehasher.hlib.wrapper.packet.bukkit`. Это низкоуровневый API: ProtocolLib и структура пакетов меняются вместе с Minecraft, поэтому каждый используемый getter/setter следует проверять на фактической версии сервера.

| Класс | Пакет или роль |
| --- | --- |
| `AbstractPacket` | Базовая оболочка `PacketContainer`, чтение, запись и отправка |
| `WrapperPlayClientChat` | Клиентское chat-сообщение |
| `WrapperPlayClientCustomPayload` | Клиентский custom payload |
| `WrapperPlayClientSetCreativeSlot` | Изменение creative slot |
| `WrapperPlayClientTabComplete` | Клиентский запрос completion |
| `WrapperPlayClientUpdateSign` | Обновление строк таблички |
| `WrapperPlayClientUseEntity` | Взаимодействие с entity |
| `WrapperPlayServerBlockChange` | Изменение блока для клиента |
| `WrapperPlayServerChat` | Серверное chat-сообщение |
| `WrapperPlayServerDamageEvent` | Событие урона entity |
| `WrapperPlayServerEntityDestroy` | Удаление entities |
| `WrapperPlayServerEntityEquipment` | Экипировка entity |
| `WrapperPlayServerEntityMetadata` | Metadata entity |
| `WrapperPlayServerEntityStatus` | Статус entity |
| `WrapperPlayServerEntityTeleport` | Телепортация entity |
| `WrapperPlayServerExplosion` | Эффект взрыва |
| `WrapperPlayServerGameStateChange` | Изменение игрового состояния |
| `WrapperPlayServerHurtAnimation` | Анимация получения урона |
| `WrapperPlayServerLogin` | Данные входа; структура особенно чувствительна к версии |
| `WrapperPlayServerNamedSoundEffect` | Именованный звук |
| `WrapperPlayServerSetCooldown` | Cooldown предмета |
| `WrapperPlayServerSpawnEntity` | Создание entity для клиента |
| `WrapperPlayServerTabComplete` | Серверный ответ completion |
| `WrapperPlayServerWorldEvent` | Событие мира |
| `WrapperPlayServerWorldParticles` | Частицы мира |

Начните с проверенных сценариев на странице [«ProtocolLib и пакеты»](Packets). Не используйте wrapper только ради сокращения нескольких строк: прямой ProtocolLib API проще диагностировать при изменении протокола.

## Как выбрать API

Для обычного плагина разумный порядок такой:

1. Начните с `Colors`, `Informer`, `Scheduler`, `Cooldowner`, `ItemBuilder` и объектной конфигурации.
2. Подключайте `MySQL` или `Redis` только вместе с отдельным executor и явным закрытием ресурсов.
3. Проверяйте optional plugin перед загрузкой соответствующего hook-класса.
4. Используйте packet wrappers и NMS только после теста на каждой поддерживаемой версии Minecraft.