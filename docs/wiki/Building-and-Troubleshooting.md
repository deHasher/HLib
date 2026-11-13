# Сборка и решение проблем

## HLib 7.4: общий каталог регулярных выражений

В `data.CompiledPattern` добавлены девять констант для общих placeholders, аргументов Cart, HTML, свойства `segment_amount`, идентификаторов, HEX-строк и namespaced keys. Старые константы, их порядок и API сохранены; новые значения добавлены в конец enum. Контракты и примеры приведены в [Core API](Core-API.md#общие-регулярные-выражения).

12 ноября 2026 года на JDK 25 и Gradle 9.8.2 выполнены offline `compileJava`, `checkstyleMain`, `spotbugsMain` и `shadowJar` с `-x copyShadowJarToHCorePlugins`: успешно, 7 задач, из них 6 выполнены и одна актуальна. Артефакт — `result/HLib.jar`, SHA256 `9b611f02bf4f8cdaaa7b5233366bf9c28ea2c1b867ce4492c3ab85a2ff6f6139`. В рабочие `plugins` он не копировался. Сохранились два прежних предупреждения deprecated UUID-overloads в `Console` и предупреждение Gradle о deprecated features.

Отдельный Java 25 runner с `-Xverify:all` загрузил настоящий `CompiledPattern` из итогового JAR и прошёл 105 assertions: длина и алфавит идентификаторов, регистр и длина HEX, namespace/path, Unicode и запрещённые символы аргументов Cart, группы placeholders и HTML, HTML entities, чтение и `replaceFirst` свойства `segment_amount`, имя игрока и порядок enum. Runner и результат находятся в `%TEMP%/hlib-compiled-patterns-20261112`, журнал Gradle — `%TEMP%/hlib-compiled-patterns-build.log`. Проверка не запускала HLib entrypoints, сервер, прокси, сеть или БД.

`python scripts/render-docs.py` и `python scripts/render-docs.py --check` обновляют и проверяют README/Wiki для версии 7.4. Результаты компиляции и изолированного runner не заменяют проверку на установленной серверной платформе.

## Потребители удалённых NMS-обёрток

После упрощения владельцем `data/NMS.java` класс `ItemsController` использует `Enchantment.LUCK_OF_THE_SEA` и `InventoryView.getItem(rawSlot)` напрямую. `NMS.java`, поиск версии, разрешение звуков и пакетные константы этой правкой не изменены. Публичные методы `ItemsController`, условия подсветки и параметры чтения слота сохранены.

На Java 25 успешно выполнены offline `compileJava`, `checkstyleMain`, `spotbugsMain` и `shadowJar` с отключённым копированием артефакта: 7 задач, 6 выполнены и одна актуальна; копирования в рабочие `plugins` нет. Два предупреждения UUID-overrides в `Console` не относятся к NMS и в этой задаче не исправлялись. Итоговый HLib JAR имеет SHA256 `a88a4005abfa69529d5e8c28c2b0f46a0d91d0eec5d49bf2d7ad2693808e3a6f`. Проверка конечных JAR не обнаружила старой `NMS$Inventory` и обращений к удалённым NMS-членам. Сервер, игровые события, БД и клиент не запускались.

## Сборка из исходников

### Требования

- Git.
- JDK {{java_version_build}} для Gradle Java toolchain и сборки, как в CI.
- JDK {{java_version_checkstyle}}, доступный Gradle toolchains, для Checkstyle при полном `build`.
- Доступ к Maven Central, PaperMC и репозиториям optional API.
- Локальные compile-only JAR из каталога `libs/`, уже присутствующие в репозитории.

Версии Java берутся из `gradle.properties`: `java_version_target={{java_version_target}}`, `java_version_build={{java_version_build}}` и `java_version_checkstyle={{java_version_checkstyle}}`. Собственные классы HLib компилируются с `--release {{java_version_target}}` и bytecode Java {{java_version_target}}; это не гарантия, что все включённые библиотеки и сторонние плагины имеют такой же bytecode.

Paper compile dependency: `{{dependency_version_bukkit}}`. Единственный серверный дескриптор `paper-plugin.yml` объявляет Paper API `{{api_version_paper}}`. Velocity API для компиляции берётся из подготовленной копии актуального `libs/Velocity.jar`, версия annotation processor — `{{dependency_version_velocity}}`.

### Совместимость Adventure

Задача `velocityCompileJar` создаёт `build/dependencies/Velocity-compile.jar` без встроенных `net/kyori/**`, включая multi-release варианты. Этот файл подключается как `compileOnly`; исходный `libs/Velocity.jar` не изменяется и не откатывается. BOM Adventure `{{dependency_version_adventure}}` задаёт отдельные Maven-зависимости компилятора. Эта версия нужна общему Bukkit/Velocity classpath: современный `Velocity Player` наследует `PlayerHeadObjectContents.SkinSource`, которого нет в старой Adventure 4.8.1. Adventure и подготовленный Velocity не включаются в HLib; при запуске их предоставляет платформа.

Это предотвращает скрытую компиляцию против Adventure 5 из полного JAR прокси. Минимальный Bukkit/Paper — 1.21.11; проверки Adventure от старых серверов не используются для выбора поведения. `Component.decorationIfAbsent(...)` доступен на всей поддерживаемой серверной платформе и вызывается без проверки версии. Внешний полный `Velocity.jar` по-прежнему нельзя использовать вместо подготовленной compile-копии.

Транзитивные `org.bukkit:bukkit` и `org.spigotmc:spigot-api` исключены из конфигураций: старый Bukkit от WorldGuard/Vault конфликтует с capability современного Paper API. Версии самих интеграций не обновлены.

### Windows

```powershell
git clone https://github.com/deHasher/HLib.git
Set-Location HLib
.\gradlew.bat clean build
```

### Linux и macOS

```bash
git clone https://github.com/deHasher/HLib.git
cd HLib
chmod +x gradlew
./gradlew clean build
```

Результат: `result/HLib.jar`.

`build` зависит от `shadowJar`. Перед компиляцией Gradle копирует Java-файлы в generated sources и заменяет токены `${...}` значениями из `gradle.properties`. Тем же способом заполняются `paper-plugin.yml` и Velocity metadata.

### Проверка исходников

Для компиляции и статического анализа без упаковки JAR:

```powershell
.\gradlew.bat compileJava checkstyleMain spotbugsMain --offline
```

Команда запускается из каталога HLib и обновляет результаты проверки в `build/`. Режим `--offline` требует уже закэшированных зависимостей. Задачи не запускают плагины и не устанавливают JAR на сервер.

## Что входит в JAR

Shadow JAR включает Jedis, Commons Pool, HikariCP, Gson, SnakeYAML, MySQL Connector/J, MariaDB driver, GeoIP/MaxMind, NBT API, dnsjava и Vorbis core. NBT API и Telegram transport Apache HttpClient {{dependency_version_httpclient}} с Commons Codec/Logging relocates в namespace HLib; остальные библиотеки потенциально могут пересечься по версиям с окружением. Jackson core/databind/annotations включены для фактической работы GeoIP reader.

Paper, Velocity, PacketEvents и API hook-плагинов являются `compileOnly` и внутрь HLib не входят. Используйте официальные дистрибутивы и соблюдайте лицензии сторонних компонентов; файлы из `libs/` не являются runtime-набором для копирования на сервер.

Корневой entry `hash` копируется из `src/main/resources/hash` и содержит UTF-8 строку для запроса скачивания обновления. Оба platform entrypoints HLib используют одно значение; оно удалено из Java-исходников. Ресурс не извлекается в data directory.

## Правила Java при изменениях

Рабочие инструкции закреплены в [`CONTRIBUTING.md`](https://github.com/deHasher/HLib/blob/master/CONTRIBUTING.md). Для новых и текущих изменённых Java-файлов целиком используются следующие правила; остальной legacy-код не переписывается попутно:

- Изменяемые списки создаются через `Lists.newArrayList(...)`, неизменяемые — через `List.of(...)`, `List.copyOf(...)` или `Stream.toList()` по контракту.
- Вместо `for` для коллекций используется `.forEach(...)`. Ранний отказ или поиск выражается через `anyMatch`, `allMatch`, `findFirst` и другие подходящие операции stream, сохраняя порядок и остановку без `AtomicBoolean` и исключений для управления обходом.
- Вместо `String.join(...)` используется `Tools.join(CharSequence delimiter, Object... elements)`. Он поддерживает объекты и раскрывает `Collection<?>` на один уровень, но не произвольный `Iterable`: такой источник сначала преобразуется в коллекцию. Массивы примитивов также требуют отдельного преобразования элементов.
- Для массивов объектов вместо `Arrays.stream(array)` используется `Stream.of(array)`. Для `int[]`, `long[]`, `double[]` выбираются `IntStream.of`, `LongStream.of`, `DoubleStream.of`; другие примитивные массивы обходятся через подходящий доступ по индексу, например `IntStream.range`. `Stream.of(primitiveArray)` не является обходом его элементов.
- Для простых DTO предпочитается `record`, если сохраняются логика, идентичность, ограничения API и приемлемая производительность. Неизменяемость ссылок компонентов не заменяет защитную копию вложенных коллекций; объекты с изменяемым состоянием, наследованием или обязательным закрытым конструктором публичного API остаются обычными классами.
- Тривиальные геттеры и сеттеры создаются через Lombok `@Getter`/`@Setter`, включая явные аннотации статических полей: аннотации класса на них не действуют. Доступность сохраняется; внутреннее состояние использует `AccessLevel.PRIVATE`, `final`-поля имеют только геттер. Обращение к состоянию идёт через эти методы, кроме инициализации. Публичный API и видимость mutable-объектов не расширяются. Методы с валидацией, вычислениями, защитным копированием или побочными эффектами остаются явными, у record используются нативные методы компонентов.
- Для изменения регистра строк обязательны `.toUpperCase()` и `.toLowerCase()` без аргументов. Передача `Locale.ROOT` в `toUpperCase` или `toLowerCase` запрещена.
- Отладка выводится только через `Tools.debug(player, message)`; для консоли используется `Tools.debug(null, message)`. Logger любого уровня, `System.out` и `System.err` для отладочных сообщений запрещены.
- Обычные сообщения игроку или в консоль отправляются только через `Informer.send(player, message)` или `Informer.send(message)` соответственно. `Informer.component(...)` создаёт компонент, отправка выполняется через `Informer.send(...)`. Для title, action bar и boss bar используются API соответствующих специальных каналов.

### Историческая проверка зависимостей до перехода на Paper 1.21.11

В HLib 6.30 изменены только зависимости сборки; Java-код сообщений и переводов сохранён. Отдельная копия в `build/verification/adventure-dependencies/isolated/HLib` прошла `compileJava`, экспорт compile classpath и `shadowJar` с отключённым копированием артефакта: 127 Java-исходников, 211 собственных классов. Компилятор выдал 14 предупреждений о deprecated API; ошибок компиляции не было. Checkstyle и SpotBugs в этой проверке не запускались.

В compile classpath подтверждён единственный `Component.class` из Adventure {{dependency_version_adventure}}. Производный Velocity не содержит Kyori; исходный `libs/Velocity.jar` сохранён без изменения. В новом Shadow JAR нет классов Kyori или Velocity, Bukkit/Paper/Velocity metadata содержат версию 6.30. Снимок 149 исходных файлов, scripts и локальных зависимостей сверён по SHA-256.

В изолированных JVM настоящие `Colors` и `CompiledPattern` проверены на Adventure 4.8.1, 4.13.1, 4.26.1 и 5.2.0: legacy-цвет, HEX, извлечение plain text, `Component.empty().toBuilder()`, кнопка с click/hover и `build()` прошли на всех четырёх версиях. Для этих проверок загружались только два класса HLib и нужные библиотеки, без Tools и plugin entrypoints. Статический разбор 20 уникальных Kyori references новых классов повторно подтвердил: относительно 4.8.1 остаются только описанные выше два обращения с платформенным/версионным ограничением; новых незащищённых ссылок нет.

Артефакт проверки находится в `build/verification/adventure-dependencies/isolated/HLib/result/HLib.jar`; отчёты — `results.json`, `abi-results.json`, `source-hashes.json` и `compile.log` в каталоге проверки. Обычный `result/HLib.jar` и серверные JAR не заменялись. Сеть, БД, updater, startup плагинов и игровые серверы не запускались; совместимость остальных API HLib этими проверками не заявляется.

## Проверки проекта

Основные задачи сборки:

```text
generateTokenizedJava + velocityCompileJar → compileJava
processResources → classes → jar / shadowJar
checkstyleMain + spotbugsMain + test → check
assemble + check + shadowJar → build
```

Тестовых исходников в `src/test` нет, поэтому стандартная задача `test` не выполняет тесты. Полный `build` включает Checkstyle и SpotBugs. Успех этих проверок не подтверждает совместимость каждой интеграции с реальным сервером.

Workflow `build` запускает `clean shadowJar` на Temurin {{java_version_build}}, создаёт или обновляет release и принудительно перемещает тег `v<version>` на текущий commit. Один и тот же version tag не следует считать неизменяемым артефактом. Этот workflow не запускает `checkstyleMain`, `spotbugsMain` и тесты.

## Сетевое поведение при загрузке

При загрузке класса `Tools` выполняется синхронная DNS-проверка режима разработки. На системах, отличных от Windows, entrypoint дополнительно проверяет `{{url_site_api}}/plugins`.

Если проверка обновления не вернула данные, HLib вызывает shutdown всей платформы. При наличии новой версии HLib скачивает JAR, заменяет текущий файл и также завершает платформу для перезапуска. В `info.yml` нет флага отключения updater.

Разрешите исходящие DNS/HTTPS-запросы и заранее проверьте доступность endpoint в закрытой сети.

`Updater.run(String file)` сначала читает корневой entry `hash` из указанного JAR. Применяется UTF-8 и `String.strip()` к внешним пробельным символам; пустой результат считается ошибкой. Отсутствующий entry, directory вместо ресурса и ошибка чтения дают сообщение через `Informer.send` и `false` до создания/удаления `.tmp` и HTTP-загрузки. Поток ресурса и `JarFile` закрываются до замены JAR. Старого overload с аргументом hash больше нет. Отдельный `Updater.check` по-прежнему выполняет запрос версии; описанный порядок относится к `run`.

После загрузки `info.yml` HLib запускает асинхронное обновление GeoLite2 City. Источник `geoip.download-url` должен отдавать файл MMDB напрямую по HTTPS. Сбой GeoIP не останавливает платформу: прежняя рабочая база остаётся в памяти и на диске, если существовала. При первом запуске без базы `Tools.getGeo` возвращает `null` до успешной загрузки. Внешний источник может быть недоступен или содержать старую копию; это не подтверждает успешное обновление данных MaxMind.

## Частые проблемы

| Симптом | Причина | Решение |
|---|---|---|
| `UnknownDependencyException: HLib` | HLib не установлен или имя зависимости неверно | Положите `HLib.jar` в корень `plugins/`, объявите `HLib` в descriptor и полностью перезапустите сервер |
| Класс HLib компилируется, но недоступен на Paper | Paper plugin classpath изолирован | Добавьте `dependencies.server.HLib` с `join-classpath: true` |
| Плейсхолдеры PAPI остались в тексте | PlaceholderAPI отсутствует или ещё не загружен | Установите его для нужных подстановок; без PAPI hook возвращает исходный текст |
| `NoClassDefFoundError` из hook-класса | API optional-плагина отсутствует | Установите плагин, добавьте его API как `compileOnly` и проверяйте `Plugin.X.isEnabled()` после загрузки |
| `NoSuchMethodError` у Adventure | Байткод скомпилирован против другой JVM-сигнатуры API; её могла скрыто предоставить встроенная Adventure полного JAR прокси | Сверьте фактический compile classpath с Adventure целевого сервера, исключите встроенный Kyori из вспомогательного Velocity JAR и пересоберите затронутый плагин; простой перезапуск или замена build script прежний байткод не исправляют |
| Строка не приходит игроку Velocity | String path использует незарегистрированный `hlib:main` | Используйте `Informer.send(player, Informer.component(...))` |
| Сервер останавливается при старте HLib | Сервис обновлений недоступен на non-Windows | Проверьте DNS, HTTPS, proxy/firewall и `{{url_site_api}}/plugins` |
| Новый `HCommand*` падает на `Permission.getEnum` | Автосгенерированного permission нет в enum HLib | Используйте native command API или добавьте permission в исходники HLib |
| Команда создана, но не появилась | Constructor хранит объект, но не регистрирует его | Зарегистрируйте команду в `CommandMap`/`CommandManager` самостоятельно |
| SQL тормозит сервер | Query API синхронный | Выполняйте запрос через async scheduler и возвращайте Bukkit-операции в sync thread |
| Packet getter/setter падает после обновления | Изменилась структура Minecraft/PacketEvents | Сверьте PacketType, PacketEvents и его native wrapper, затем протестируйте на целевой версии |
| Первый YAML save падает | Provider ещё не инициализирован | Сначала вызовите `load()`; создавайте файл через `save()` только если он отсутствует, не затирайте существующий файл с ошибками |

## Ограничения, которые важно учитывать

- Folia region scheduler не используется.
- Общий Scheduler facade предоставляет только async-методы; Bukkit sync API находится в `Scheduler.Bukkit`.
- `Tools.isPerm` учитывает `Info.admins`: проверьте список `admins` в `info.yml`, так как он даёт обход части проверок прав.
- Ошибка Redis-подписки в `Performance.init()` вызывает остановку платформы; сбор статистики включайте осознанно.
- HLib содержит много глобального static state. Избегайте повторной инициализации и очищайте созданные вами задачи/подписки.
- `Requestor` корректно планирует срок практически только с `TimeUnit.SECONDS`.
- Config-массивы загружаются некорректно; используйте `List`.
- HTTP helpers, часть hook-методов и все DB query синхронны.
- `Plugin.isEnabled()` кэширует первый результат, поэтому ранняя проверка может навсегда сохранить `false`.
- Нативные wrappers PacketEvents зависят от версии протокола; HLib не содержит собственных packet wrappers.

## Генерация README и автоматическая публикация Wiki

Исходники документации находятся в [docs/README.md](https://github.com/deHasher/HLib/blob/master/docs/README.md) и [docs/wiki](https://github.com/deHasher/HLib/tree/master/docs/wiki). Параметры подставляет `scripts/render-docs.py` из `gradle.properties`; Java, версия HLib, API, адреса и перечисленные версии зависимостей обновляются вместе с их свойствами.

```bash
python scripts/render-docs.py
python scripts/render-docs.py --check
```

Первая команда обновляет корневой README и создаёт страницы в `build/wiki/`. Вторая проверяет актуальность README и корректность токенов всех шаблонов без записи файлов. Python использует только стандартную библиотеку. Несовпадение версии в README со значением `lib_version_plugin` также останавливает `--check`, даже если Java-компиляция и рендеринг шаблонов успешны.

Workflow `docs` сначала проверяет README и шаблоны без записи при push, pull request и ручном запуске. После успешной проверки push или ручного запуска из `master` отдельная задача публикует Wiki из того же commit. Основной репозиторий не коммитится и не пушится; сгенерированный README включается в локальный commit автора. Это устраняет прежние bot-коммиты `docs: render README from project properties`, из-за которых локальная ветка отставала и следующий push получал `fetch first`. Коммиты других участников по-прежнему требуют обычного согласования истории. Проверке достаточно `contents: read`, а `contents: write` получает только задача Wiki. Git credentials сохраняются лишь у checkout Wiki; отдельный токен не нужен. Изменяйте страницы в `docs/wiki`: ручная правка управляемой страницы непосредственно в Wiki будет заменена следующей синхронизацией. [Инструкция для участников](https://github.com/deHasher/HLib/blob/master/CONTRIBUTING.md).

Локальная проверка нового workflow: 25 assertions с настоящим SnakeYAML проверили jobs, permissions, credentials и область Git-команд; 68 assertions проверили девять сочетаний события/ветки, запрет публикации после failed check и неизменность bytes/mtime файлов при `--check`. Устаревший README и неизвестный token отклоняются без частичной записи, корректная публикация сохраняет unmanaged Wiki-страницы. Живой GitHub Actions запуск не выполнялся; workflow изменится на сервере после обычного push этой правки.

## Диагностика перед issue

Соберите следующую информацию:

1. Версию HLib и ссылку на release.
2. Java runtime из `java -version`.
3. Точную версию Paper/Velocity и Minecraft.
4. Версии optional-плагинов, которых касается ошибка.
5. Полный stack trace без обрезки.
6. Минимальный код вызова HLib.
7. Результат `./gradlew clean build --stacktrace` для ошибки сборки.

Репозиторий: [github.com/deHasher/HLib](https://github.com/deHasher/HLib)

## Лицензия

Исходный код HLib распространяется по [GNU GPL-3.0](https://github.com/deHasher/HLib/blob/master/LICENSE).

### Канонические профили, созданные сайтом

Предшествующая проверка `HPlayer.createHPlayer` подтвердила чтение из SQL ID и сохранённого имени с профилем, добавленным после заполнения начального кэша, и внешней вставкой между SELECT и прежним INSERT IGNORE. Проверены ошибочный регистр первой попытки, последующий корректный вход, совпадение объекта кэша при параллельных запросах и создание одной строки профиля. Предыдущая реализация воспроизводимо не проходит проверку сохранения регистра имени сайта; исправленная проходит.

Изолированный SQL harness `build/verification` проверил 178 assertions с реальными HLib `HPlayer`, `MySQL` и Hikari. База — H2 в памяти в MariaDB mode, без внешнего сервера; адаптер заменяет `<=>` на `IS NOT DISTINCT FROM` и убирает column `CHARACTER SET ascii COLLATE ascii_bin`, таблица имён использует `VARCHAR_IGNORECASE`. Для проверки обработки гонки с сайтом duplicate при вставке аккаунта отображается из H2 `23505` в MariaDB `SQLState 23000 / error 1062`; это имитация метаданных ошибки драйвера. Это проверка поведения запросов, откатов и конкуренции, а не подтверждение MariaDB-совместимости. Лог — `results.txt`, воспроизведение дефекта до исправления — `regression-before.txt`, прежний отказ при регистрации одновременно с сайтом — `website-race-before.txt`, версии проверенных исходников — `source-hashes.json`.

24 октября 2026 года фабрика переведена на INSERT с no-op при дубликате и SELECT FOR UPDATE в одной транзакции; кеш публикуется после commit, одинаковая повторная загрузка идемпотентна. Изолированный стенд проверил 80 assertions на настоящих HPlayer и MySQL.transaction: внешние конкурентные вставки и rollback, canonical case, один объект при параллельном создании, ошибки SQL/commit без ложного кеша, конфликт ID/имени и post-commit публикацию identity без повторного SQL. База — H2 в памяти с адаптером dialect/advisory locks; сетевые подключения и plugin startup не выполняются. После успешной проверки тестовые исходники, fixtures и runner удалены по прямому указанию владельца; сохраняемой команды повторного запуска этого стенда больше нет. Полная offline-сборка с отключённым копированием артефакта прошла compilation, Shadow, Checkstyle и SpotBugs. Получен result/HLib.jar версии {{lib_version_plugin}}; копирование JAR и установка на сервер в этой проверке не выполнялись.

## Paper 1.21.11 и пакеты

Минимальный Paper — 1.21.11, единственный серверный descriptor — `paper-plugin.yml`. Собственные классы собираются с `--release {{java_version_target}}`; компиляция и анализаторы используют JDK {{java_version_build}}. Paperweight для HLib не требуется: прямых NMS-импортов нет. Пакеты предоставляет внешний PacketEvents {{dependency_version_packetevents}}, подключённый как `compileOnly` через CodeMC. HLib не вызывает его `load/init/terminate`: lifecycle принадлежит отдельному плагину.

При миграции удалены ProtocolLibHook и все 26 собственных packet wrappers. Публичное API намеренно изменено: используется PacketEventsHook и native PacketEvents types, потребители должны быть пересобраны. Предыдущие проверки ProtocolLib layouts и serializers больше не описывают текущую реализацию.

Убраны старые server-version ветки, reflection-обходы смены Bukkit enum/interface и InventoryView, устаревший `Class.newInstance()` конфигураций. `Translator` использует современный translation API, custom model data — компонент с float, головы — PlayerProfile. В `ItemsController.isInventoryFull` учтены nullable элементы storage contents, отмеченные современным Paper API.

### Java 25

Сборка, source/target, `--release`, Java toolchain и Checkstyle переведены на Java 25. CI уже читает `java_version_build` из gradle.properties и использует Temurin 25. Для загрузки HLib теперь нужна Java 25 как на Paper 1.21.11+, так и на Velocity; прежние результаты Java 21 выше относятся к предыдущему артефакту. `Tools.getDamager` и `Translator.resolveTranslationKey` используют pattern switch с прежним порядком выбора типа и обработкой null; неиспользуемые catch bindings в Translator и AttributeController заменены на `_`. Preview features не включены; публичные сигнатуры и строковые API не менялись.

Первичная проверка Java 25: compileJava, Checkstyle, SpotBugs и shadowJar завершились успешно; лог `build/java25-final.log`. В итоговом JAR 210 собственных классов имеют major 69 и minor 0, без preview bytecode. На `jbr-25.0.3/bin/java` повторены все 84 прежних assertions и добавлены 10 проверок ветвей pattern switch (null/Material в Translator, player/projectile/firework/TNT/cloud/null/unknown source в getDamager): всего 94 assertions. Packet fixture сохранил 15 успешных native encode/decode roundtrips. Логи этого прогона находились в `build/verification/paper-1.21.11/java25-packet-results.txt` и `java25-core-results.txt`; временный каталог позднее очищен. Эти 94 assertions относятся к предыдущему артефакту, а не к следующей завершающей проверке. Проверки старых packet wrappers относились к удалённой реализации и не переносятся на PacketEvents автоматически. Сервер, plugin startup, сеть и БД не запускались, JAR в plugins не копировался.

### Завершающая проверка Java 25 и Paper metadata

При предыдущей миграции синтаксиса проверены 126 разрешённых тогда Java-исходников HLib; прежний packet hook в эту проверку не входил и впоследствии заменён. В 26 файлах 78 неиспользуемых catch bindings и 6 lambda parameters заменены на `_`. JDK compiler AST больше не находит именованных неиспользуемых привязок. Кандидатов для `List.getFirst/removeFirst/getLast/removeLast` нет: `propertiesArray.get(0)` относится к Gson `JsonArray`, его API сохранён. Снимок и отчёты — `build/verification/java25-syntax`. При сверке отдельно сохранены параллельные изменения `EmoteCraftHook.textToString` и разделителя импортов `DateTimeFormatter`; преобразование EmoteCraft в pattern switch совпало с прежним результатом и типом исключения на 13 входах, включая null, строку, JSON и неизвестный тип.

Исправлены две ошибки загрузки общего API на Velocity без Bukkit. В `Translator.resolveTranslationKey` проверки null и String выполняются до pattern switch с Bukkit-типами: его bootstrap иначе разрешал `Material` ещё до выбора строковой ветки. В `Tools.headModify` сохранена локальная ссылка типа `ItemMeta`, а `SkullMeta` используется только для профиля; передача этой базовой ссылки в `setItemMeta` предотвращает разрешение иерархии Bukkit при верификации общего класса Tools. UUID, имя и texture головы сохраняются без изменения. Проверки выполняли методы настоящего итогового JAR на classpath Velocity без Bukkit в обычном режиме JVM и с `-Xverify:all`; оба класса прошли оба режима. Proxy API подставлен без конструктора плагина, DNSController заменён локальной заглушкой.

В `org.gradle.jvmargs` удалён `-Djava.security.manager=allow`: Java 25 завершалась с кодом 1 ещё до запуска Gradle. Проверка `java -version` со всеми исходными параметрами воспроизвела отказ, с исправленными параметрами завершилась с кодом 0; отдельный пустой Gradle-проект с теми же параметрами также запустился успешно.

Сохранены семь точечных исключений для устаревших API: шесть обязательных методов `ConsoleCommandSender`/`Conversable` в `Console.java` и строковый `Player.sendTitle` в `Informer.java`. Предупреждения подавлены непосредственно на этих методах; сигнатуры и поведение не удалены и не заменены компонентами. Интерфейс консольного отправителя по-прежнему наследует Conversable, хотя conversation API отмечен для удаления. [ConsoleCommandSender](https://jd.papermc.io/paper/1.21.11/org/bukkit/command/ConsoleCommandSender.html), [Conversable](https://jd.papermc.io/paper/1.21.11/org/bukkit/conversations/Conversable.html).

Серверный `plugin.yml` удалён, используется только `paper-plugin.yml`; Velocity metadata сохранена. В Paper descriptor явно задан `join-classpath: true` для всех 21 необязательных зависимостей с `load: BEFORE`, включая прежде пропущенный Nexo. Проверка настоящим `PaperPluginMeta` сравнила сведения плагина и зависимости со старым дескриптором: 20 assertions прошли. В итоговом JAR есть `paper-plugin.yml` и `velocity-plugin.json`, `plugin.yml` отсутствует. Формат зависимостей описан в [Paper plugins](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/).

Завершающая offline-сборка с компиляцией, Checkstyle, SpotBugs и упаковкой Shadow, без копирования артефакта на JDK 25 успешна, предупреждений javac нет; лог `build/java25-paper-final.log`. Отдельные 14 assertions проверили настоящий CraftMetaSkull и Paper profile с точными UUID/именем/textures, пустые значения, пропуск предмета без skull meta, передачу сообщений Console и её прежние no-op методы. Использованы vanilla registry bootstrap, подставной Server и ItemStack; MinecraftServer, plugin lifecycle, сеть и БД не запускались. JDK предупреждает о native access и Unsafe во внешних JLine/JOML, но проверки успешны. Исторические проверки удалённых packet wrappers не подтверждают поведение нового PacketEvents API. Артефакт не копировался в plugins.

## Переход на PacketEvents

11 ноября 2026 года packet API перенесён на отдельный PacketEvents {{dependency_version_packetevents}}. Удалены `ProtocolLibHook.java`, 26 файлов `wrapper/packet/bukkit`, локальный compile JAR `libs/ProtocolLib.jar` и старые build/descriptor dependencies. Добавлены `PacketEventsHook`, `Plugin.PACKET_EVENTS`, Maven compileOnly dependency и optional Paper dependency `packetevents` с `join-classpath: true`. Совместимые aliases старого API отсутствуют; потребителям нужна повторная сборка. Первичная миграция PacketEvents выполнена без изменения версии HLib 7.0.

На JDK 25 успешно выполнены `compileJava checkstyleMain spotbugsMain shadowJar -x copyShadowJarToHCorePlugins`. Checkstyle и SpotBugs завершились без замечаний; сохраняются два существовавших предупреждения deprecated UUID-overrides в `Console`. `result/HLib.jar` содержит 185 собственных классов Java 25 (major 69, minor 0), Paper/Velocity metadata, без `plugin.yml`, удалённых wrappers, ссылок на ProtocolLib и встроенной копии PacketEvents. Артефакт в `plugins` не копировался.

В отдельной JVM с `-Xverify:all` выполнены 4150 assertions на настоящем итоговом HLib и PacketEvents 2.14.0 при выбранном протоколе 1.21.11. Проверены body encode/decode spawn (id, UUID, position, pitch/yaw, type), metadata armor stand (невидимость, имя, gravity/flags), native equipment с PLAYER_HEAD, шесть GameEvent, все 4096 координат multi-block-change для отрицательной высоты, очередь remove/readd/limit/clear и снятие listeners только выбранного владельца. Реальный ProtocolManager 16 раз сериализовал общий spawn wrapper из двух потоков; каждый buffer корректно прочитан и освобождён, поле buffer wrapper сброшено. Это проверка сериализаторов, не настоящий канал Minecraft. Преобразование Bukkit ItemStack через SpigotReflectionUtil и настоящий CraftServer отдельно не проверялось.

Fixture использовал подставной Bukkit Server/Player, DNSController без сети, native Netty buffers/EmbeddedChannel и API PacketEvents без load/init/inject. Для classpath из производного Velocity удалены только service descriptors Kyori, требующие запущенный proxy. Отдельная JVM без Bukkit, PacketEvents и ProtocolLib успешно загрузила реальный HLib на Velocity и выполнила Tools.join, Colors и Plugin enum с `-Xverify:all`; ProxyServer подставной, constructor и lifecycle HLib не вызывались.

Нагрузочный сценарий, настоящий сервер/клиент, plugin startup, рабочие БД/Redis и updater не запускались. Временный стенд `build/verification/packetevents` удалён после общей проверки потребителей по указанию владельца; сохраняемого runner для этих assertions нет. Сборочные задачи выше воспроизводят компиляцию и анализаторы. `python scripts/render-docs.py --check` проверяет шаблоны и актуальность README.

### Каталог плагинов и обязательность PacketEvents

После уточнения владельца `Plugin` сохраняет обе константы `PACKET_EVENTS("packetevents")` и `PROTOCOL_LIB("ProtocolLib")`. ProtocolLib остаётся только записью каталога: удалённые hook, packet wrappers и compile dependency не восстановлены. HLib сохраняет `packetevents.required: false`; в HCore обязательное объявление PacketEvents остаётся только в manifests HCore loader, HCalendar, HAntiRelog и HImages, где ранее уже была packet dependency. В остальные manifests новые зависимости не добавляются; вызовы API выполняются без проверок наличия. В HLib таких проверок нет; сравнение имени в `PluginDisableEvent` отвечает за очистку listener/session state и не является проверкой доступности зависимости.

После восстановления enum-константы повторно успешно выполнены offline `compileJava checkstyleMain spotbugsMain shadowJar -x copyShadowJarToHCorePlugins` на JDK 25: семь задач, пять выполнены и две актуальны. Checkstyle и SpotBugs прошли; два прежних предупреждения `Console` сохранены. В новом `result/HLib.jar` проверены обе константы и optional Paper dependency, отсутствие ProtocolLib API references, hook и собственных packet wrappers. Строка `ProtocolLib` теперь ожидаемо присутствует в `Plugin.class` как имя элемента каталога. Исторические packet fixtures выше повторно не запускались. Готовые JAR в `HCore/build/libs/packetevents` не обновлялись, копирование в `plugins` отключено; новый временный стенд не создавался.

## Ресурс hash для обновлений

В обеих точках входа HLib удалены Java-поле hash и его getter; прежние одинаковые значения перенесены без изменения в `src/main/resources/hash`. Новый публичный вызов — `Updater.run(String file)`. Проверка версии, адрес скачивания, валидация скачанного JAR, замена файла и остановка платформы вызывающим плагином сохраняют прежнее поведение.

На JDK 25 успешно выполнены offline `compileJava checkstyleMain spotbugsMain shadowJar -x copyShadowJarToHCorePlugins`: семь задач, шесть выполнены и одна актуальна. Checkstyle и SpotBugs прошли, два прежних deprecated UUID-overrides предупреждения `Console` сохранены. Итоговый `result/HLib.jar` содержит ровно один корневой `hash`, его bytes совпадают с исходными значениями обеих платформ, а в собственных class-файлах значение отсутствует. Оба platform entrypoints скомпилированы с новым API.

Reflection-only fixture с настоящим итоговым HLib.jar и `-Xverify:all` проверил 17 assertions: единственный `run(String)`, различение двух JAR, UTF-8 и внешние пробельные символы, отсутствующий/nested/directory/empty/blank ресурс, повреждённый ZIP, отсутствующий файл и каталог вместо JAR. Переименование и удаление JAR после чтения прошли на Windows, подтверждая закрытие файловых handles. Статически подтверждён порядок readHash → операции `.tmp` → URL скачивания; classloader lookup и извлечение файла отсутствуют.

Сам `Updater.run`, сетевые запросы, загрузка плагина и его updater lifecycle не запускались. Временный source fixture и синтетические JAR удалены; существующий `build/verification` не изменялся. Готовые артефакты HCore в `build/libs/packetevents` не обновлялись, копирование в рабочий `plugins` отключено.

## Версия после миграций

После перехода на PacketEvents и переноса updater hash в ресурс версия HLib повышена с 7.0 до {{lib_version_plugin}}. Зависимости, Java/API и версия схемы `info.yml` не изменены. README и Wiki перегенерированы из шаблонов; `render-docs.py --check` пройден. Для самого инкремента сборка не запускалась: существующий `result/HLib.jar` и готовые артефакты HCore сохранены без изменения. Приведённые выше результаты компиляции и isolated-проверок относятся к артефакту до этого повышения версии.

## Регистрация HCosmetic

В каталоги добавлены `Plugin.HCOSMETIC`, семь прав `HCOSMETIC_*`, локальный `Cooldowner.Type.COSMETIC_GADGET` без MySQL и отдельный `CompiledPattern.MINECRAFT_TEXTURE_HASH` для HEX-идентификаторов текстур длиной 32–64 символа. Контракты строгих HEX-шаблонов не расширены. Пять прав категорий требуют явного разрешения и не используют fallback `Info.admins`; командное право и reload сохраняют стандартный fallback. Подробности и permission nodes перечислены в [основном API](Core-API#права-hcosmetic).

Для этих изменений на JDK 25 успешно выполнены offline `compileJava checkstyleMain spotbugsMain shadowJar -x copyShadowJarToHCorePlugins`: семь задач, пять выполнены и две актуальны. Checkstyle и SpotBugs прошли без замечаний, два прежних deprecated UUID-overrides предупреждения `Console` сохранены. Обновлён `result/HLib.jar`; артефакт в `HCore/plugins` не копировался. Зависимости и версия HLib не изменялись. Plugin startup, сервер Minecraft, БД и внешние запросы не запускались. Проверка совместимости самого HCosmetic с игровым клиентом остаётся ответственностью потребителя.

## Вложенные Map конфигураций

12 ноября 2026 года исправлена загрузка полей `Map` объектной модели в `ConfigurationUtils`: каждый уровень читает `getValues(false)` и рекурсивно преобразует дочерние sections в карты. Старый `getValues(true)` добавлял одновременно вложенные объекты и плоские пути их полей. Явный низкоуровневый API `getValues(true)`, обработка списков, `null`, точек в YAML-путях и публичные сигнатуры не менялись. Локальная нормализация `HCosmeticCfg` больше не требуется.

На настоящих `Configuration`, `StandaloneConfigurationProvider` и YAML выполнена одна и та же изолированная регрессионная проверка до и после исправления: 128 утверждений, 49 нарушений формы вложенных карт на прежней версии и ни одного на исправленном `result/HLib.jar`. Проверены три уровня вложенности, статические поля, вложенные Java-секции, `@Key`/`@Final`/`@Ignore`, скалярные и пустые карты, списки карт, порядок, прежнее поведение `null`/точек и три последовательных цикла загрузки/сохранения новым экземпляром модели. Подмен загрузчика и YAML не было. На окончательном JAR дополнительно пройдены 11 проверок неизменяемых leaf-карт и пользовательского serializer: `Map.of`, пустая карта, read-only вложенные map/list, загрузка и повторное сохранение. Скаляры не перезаписываются в исходной карте при преобразовании. Всего на финальном JAR — 139 проверок без ошибок. Стенд и отчёты находятся во временном каталоге `hlib-map-regression`.

Штатный `--offline build -x copyShadowJarToHCorePlugins` прошёл компиляцию Java 25, Checkstyle, SpotBugs и Shadow. Сохраняются два прежних deprecation-предупреждения `Console`; `test` сообщает `NO-SOURCE`, поэтому регрессионные утверждения выполнялись отдельной JVM. Проверка не устанавливает JAR в `HCore/plugins`, не запускает платформу, startup/update, БД или сетевые сценарии. HCore проверяется без его корневой Gradle-конфигурации.

В HCore просмотрены 49 файлов конфигурационных моделей: девять прямых `Map` и два `List<Map>`. Ни один потребитель не требует синтетических плоских ключей. На пяти настоящих моделях HCalendar, HParkour, HAuth и Cart выполнены 457 одинаковых проверок до и после исправления, включая три цикла save/load и настоящий `HParkour.Settings.rewardCommands`; в обоих случаях они успешны. Вложенные карты, порядок правил/команд и существующие миграции сохранены. У Cart заменена только версия из HCoreCfg, сами модели и loader настоящие. HCosmetic без локального `normalize` прошёл 6061 проверку семи YAML, 98 defaults, прямой загрузки пяти map-моделей, миграций и отката неверных данных/ошибки записи. Границы этих стендов и сборки потребителя описаны в рабочей вики HCore, `docs/build-and-verification.md`.