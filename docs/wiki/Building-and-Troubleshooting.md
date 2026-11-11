# Сборка и решение проблем

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

Paper, Velocity, ProtocolLib и API hook-плагинов являются `compileOnly` и внутрь HLib не входят. Используйте официальные дистрибутивы и соблюдайте лицензии сторонних компонентов; файлы из `libs/` не являются runtime-набором для копирования на сервер.

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
| Packet getter/setter падает после обновления | Изменилась структура Minecraft/ProtocolLib | Сверьте PacketType, ProtocolLib и wrapper, затем протестируйте на целевой версии |
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
- Packet wrappers — version-sensitive low-level API, а не стабильная замена ProtocolLib.

## Автоматическое обновление README и Wiki

Исходники документации находятся в [docs/README.md](https://github.com/deHasher/HLib/blob/master/docs/README.md) и [docs/wiki](https://github.com/deHasher/HLib/tree/master/docs/wiki). Параметры подставляет `scripts/render-docs.py` из `gradle.properties`; Java, версия HLib, API, адреса и перечисленные версии зависимостей обновляются вместе с их свойствами.

```bash
python scripts/render-docs.py
python scripts/render-docs.py --check
```

Первая команда обновляет корневой README и создаёт страницы в `build/wiki/`. Вторая проверяет актуальность README и корректность токенов всех шаблонов без записи файлов. Python использует только стандартную библиотеку. Несовпадение версии в README со значением `lib_version_plugin` также останавливает `--check`, даже если Java-компиляция и рендеринг шаблонов успешны.

Workflow `docs` публикует результат в GitHub Wiki и при необходимости обновляет README после изменения документации или параметров в `master`. Он также доступен для ручного запуска в Actions. Для Wiki используется штатный `GITHUB_TOKEN` с `contents: write`; отдельный токен не нужен. Изменяйте страницы в `docs/wiki`: ручная правка управляемой страницы непосредственно в Wiki будет заменена следующей синхронизацией. [Инструкция для участников](https://github.com/deHasher/HLib/blob/master/CONTRIBUTING.md).

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

## Миграция на Paper 1.21.11

HLib 6.32 устанавливает одинаковый `api-version: 1.21.11` для обоих серверных дескрипторов. После отдельного перехода на Java 25 собственные классы собираются с `--release {{java_version_target}}`; компиляция и анализаторы используют JDK {{java_version_build}}. Paperweight не нужен: библиотека использует Paper API и обёртки ProtocolLib, а собственных прямых импортов NMS у неё нет.

Убраны старые server-version ветки, reflection-обходы смены Bukkit enum/interface и InventoryView, устаревший `Class.newInstance()` конфигураций. `Translator` использует современный translation API, custom model data — компонент с float, головы — PlayerProfile. В `ItemsController.isInventoryFull` учтены nullable элементы storage contents, отмеченные современным Paper API.

Оставшиеся deprecation-предупреждения относятся к строковым сообщениям/title и обязательным методам `ConsoleCommandSender`/`Conversable`: этот интерфейс по-прежнему требует deprecated conversation API. Они сохранены по контракту и не заменены компонентами.

Предыдущая проверка до перехода target на Java 25, 10 ноября 2026 года: `compileJava`, `checkstyleMain`, `spotbugsMain` и `shadowJar` с отключённым копированием артефакта завершилась успешно. Сборка использовала JDK 25 и `--release 21`; `result/HLib.jar` содержит 210 собственных классов major 65 и оба дескриптора 1.21.11/6.32. Checkstyle и SpotBugs не сообщили ошибок. Перед итоговой сборкой дополнительно скомпилированы generated copies без suppressions `deprecation`/`removal`; найденные non-string вызовы TAB заменены актуальными API; исходный ProtocolLibHook сохранён по исключению владельца. После прямого уточнения владельца `ProtocolLibHook.java` восстановлен исходным вместе с его suppression `removal` и version guards; этот файл исключён из переноса. Остались 24 предупреждения только в `Console` и строковом `Informer.sendTitle`, включая повторения javac для conversation-сигнатур.

В отдельной JVM Java 21 выполнены 14 assertions: граница протокола 1.21.11, исключение старых и неизвестных протоколов, диапазон V26, создание конфигурации через public constructor и выбранный provider, сохранение и повторная загрузка YAML. YAML проверен с библиотеками итогового JAR, без Paper startup. Исходники проверок находятся в `build/verification/paper-1.21.11`, логи компиляции и проверок — `build/migration-packet-layouts.log` и `build/migration-unmasked.log`; это локальные результаты, не production. Генерация документации и `--check`, UTF-8/EOF/whitespace и `git diff --check` пройдены. JAR не устанавливался на сервер; Minecraft, proxy, БД, updater и сетевые сценарии не запускались. Структура пакетов и сторонние runtime-интеграции требуют отдельной проверки на целевой платформе.

После проверки реальных NMS layouts исправлены wrappers Explosion, CreativeSlot, TabComplete, DamageEvent, CustomPayload и получение имени звука; набор перенесённых полей и изменения публичного API перечислены в [пакетах](Packets). При исправлении `Tools.headModify` null/пустое имя больше не заменяется строкой UUID из 36 символов: Paper-профиль создаётся без имени, поскольку его имя ограничено 16 символами. После этих изменений повторно успешно выполнены compile, Checkstyle, SpotBugs и shadowJar, а также 14 assertions на Java 21. Итоговый JAR содержит восстановленные исходные `ProtocolLibHook$CrashQueue` и `$ChunkPacket`; байты `ProtocolLibHook.java` сверены с исходным HEAD. В этом исключённом владельцем файле сохранены четыре suppressed предупреждения ProtocolLib Registry; они были обнаружены аудитом и сознательно не исправлялись после уточнения границ задачи.

В завершающей проверке LOGIN сохранены `getGamemode`/`setGamemode` через вложенный `CommonPlayerSpawnInfo`, добавлены современные dimension-key accessors. Повторная сборка и анализаторы прошли (`build/migration-login.log`). Два isolated assertions на Java 21 с настоящим ProtocolLib подтвердили изменение primitive и reference полей record через `Accessors` без изменения соседних полей; они проверяют механизм доступа, а не сериализацию Minecraft-пакета.

### Повторная проверка миграции

11 ноября 2026 года повторно просмотрен весь migration diff HLib. Исправлены пять контрактов: generic getter перевода в Translator, результаты ошибок AttributeController, обязательные defaults Explosion, установка dimension key у нового LOGIN и сохранение буквальных UUID/имени в headModify через createProfileExact. Публичные сигнатуры при повторной проверке не менялись.

При этой проверке локальный fixture `build/verification/paper-1.21.11` использовал настоящие mapped классы Paper 1.21.11 и ProtocolLib, vanilla registry bootstrap и подставной Bukkit Server; MinecraftServer, plugin lifecycle, БД и сетевые сценарии не запускаются. Выполнены 44 packet assertions и 15 native encode/decode roundtrips для 13 типов пакетов. CreativeSlot проверяет mutation обычного предмета, а encode/decode — пустого предмета; сериализация обычного ItemStack требует server-dependent Paper sanitizer и отдельно не подтверждалась. Полный LOGIN и DamageEvent не сериализовались. Дополнительно прошли 24 assertions на настоящий AttributeController, Translator, Tools и CraftMeta: ошибки и значения атрибутов, перевод пользовательского объекта, границы версии, UUID/имя/texture головы и соответствие integer CMD первому float. Для Tools DNSController заменён локальной заглушкой; setter профиля проверен с настоящим CraftMetaSkull без применения предмета к миру, profile factory имитирует выбор онлайн-профиля и точное создание. Поведение createProfileExact отдельно сверено с bytecode CraftServer. Логи — `actual-packet-results.txt` и `actual-core-results.txt`; проверки не являются запуском сервера или клиента.

После исправлений успешно выполнены compileJava, Checkstyle, SpotBugs и shadowJar без копирования артефакта. Исходный ProtocolLibHook не изменён. Предыдущие 16 isolated assertions и проверки документации/формата также сохраняются.

### Java 25

Сборка, source/target, `--release`, Java toolchain и Checkstyle переведены на Java 25. CI уже читает `java_version_build` из gradle.properties и использует Temurin 25. Для загрузки HLib теперь нужна Java 25 как на Paper 1.21.11+, так и на Velocity; прежние результаты Java 21 выше относятся к предыдущему артефакту. `Tools.getDamager` и `Translator.resolveTranslationKey` используют pattern switch с прежним порядком выбора типа и обработкой null; неиспользуемые catch bindings в Translator и AttributeController заменены на `_`. Preview features не включены; публичные сигнатуры и строковые API не менялись.

Первичная проверка Java 25: compileJava, Checkstyle, SpotBugs и shadowJar завершились успешно; лог `build/java25-final.log`. В итоговом JAR 210 собственных классов имеют major 69 и minor 0, без preview bytecode. На `jbr-25.0.3/bin/java` повторены все 84 прежних assertions и добавлены 10 проверок ветвей pattern switch (null/Material в Translator, player/projectile/firework/TNT/cloud/null/unknown source в getDamager): всего 94 assertions. Packet fixture сохранил 15 успешных native encode/decode roundtrips. Логи этого прогона находились в `build/verification/paper-1.21.11/java25-packet-results.txt` и `java25-core-results.txt`; временный каталог позднее очищен. Эти 94 assertions относятся к предыдущему артефакту, а не к следующей завершающей проверке. ProtocolLib выдаёт предупреждение JDK 25 об использовании sun.misc.Unsafe; проверки завершились успешно, реализация внешней библиотеки не менялась. Прежние ограничения fixture для полного LOGIN, DamageEvent и server-dependent сериализации ItemStack остаются в силе. Сервер, plugin startup, сеть и БД не запускались, JAR в plugins не копировался.

### Завершающая проверка Java 25 и Paper metadata

Проверены все 126 разрешённых Java-исходников HLib; исключённый владельцем `ProtocolLibHook.java` не изменялся. В 26 файлах 78 неиспользуемых catch bindings и 6 lambda parameters заменены на `_`. JDK compiler AST больше не находит именованных неиспользуемых привязок. Кандидатов для `List.getFirst/removeFirst/getLast/removeLast` нет: `propertiesArray.get(0)` относится к Gson `JsonArray`, его API сохранён. Снимок и отчёты — `build/verification/java25-syntax`. При сверке отдельно сохранены параллельные изменения `EmoteCraftHook.textToString` и разделителя импортов `DateTimeFormatter`; преобразование EmoteCraft в pattern switch совпало с прежним результатом и типом исключения на 13 входах, включая null, строку, JSON и неизвестный тип.

Исправлены две ошибки загрузки общего API на Velocity без Bukkit. В `Translator.resolveTranslationKey` проверки null и String выполняются до pattern switch с Bukkit-типами: его bootstrap иначе разрешал `Material` ещё до выбора строковой ветки. В `Tools.headModify` сохранена локальная ссылка типа `ItemMeta`, а `SkullMeta` используется только для профиля; передача этой базовой ссылки в `setItemMeta` предотвращает разрешение иерархии Bukkit при верификации общего класса Tools. UUID, имя и texture головы сохраняются без изменения. Проверки выполняли методы настоящего итогового JAR на classpath Velocity без Bukkit в обычном режиме JVM и с `-Xverify:all`; оба класса прошли оба режима. Proxy API подставлен без конструктора плагина, DNSController заменён локальной заглушкой.

В `org.gradle.jvmargs` удалён `-Djava.security.manager=allow`: Java 25 завершалась с кодом 1 ещё до запуска Gradle. Проверка `java -version` со всеми исходными параметрами воспроизвела отказ, с исправленными параметрами завершилась с кодом 0; отдельный пустой Gradle-проект с теми же параметрами также запустился успешно.

Сохранены семь точечных исключений для устаревших API: шесть обязательных методов `ConsoleCommandSender`/`Conversable` в `Console.java` и строковый `Player.sendTitle` в `Informer.java`. Предупреждения подавлены непосредственно на этих методах; сигнатуры и поведение не удалены и не заменены компонентами. Интерфейс консольного отправителя по-прежнему наследует Conversable, хотя conversation API отмечен для удаления. [ConsoleCommandSender](https://jd.papermc.io/paper/1.21.11/org/bukkit/command/ConsoleCommandSender.html), [Conversable](https://jd.papermc.io/paper/1.21.11/org/bukkit/conversations/Conversable.html).

Серверный `plugin.yml` удалён, используется только `paper-plugin.yml`; Velocity metadata сохранена. В Paper descriptor явно задан `join-classpath: true` для всех 21 необязательных зависимостей с `load: BEFORE`, включая прежде пропущенный Nexo. Проверка настоящим `PaperPluginMeta` сравнила сведения плагина и зависимости со старым дескриптором: 20 assertions прошли. В итоговом JAR есть `paper-plugin.yml` и `velocity-plugin.json`, `plugin.yml` отсутствует. Формат зависимостей описан в [Paper plugins](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/).

Завершающая offline-сборка с компиляцией, Checkstyle, SpotBugs и упаковкой Shadow, без копирования артефакта на JDK 25 успешна, предупреждений javac нет; лог `build/java25-paper-final.log`. Отдельные 14 assertions проверили настоящий CraftMetaSkull и Paper profile с точными UUID/именем/textures, пустые значения, пропуск предмета без skull meta, передачу сообщений Console и её прежние no-op методы. Использованы vanilla registry bootstrap, подставной Server и ItemStack; MinecraftServer, plugin lifecycle, сеть и БД не запускались. JDK предупреждает о native access и Unsafe во внешних JLine/JOML, но проверки успешны. Полные прежние 94 assertions повторно после очистки стенда не запускались; ограничения сериализации LOGIN, DamageEvent и обычного ItemStack остаются в силе. Артефакт не копировался в plugins.