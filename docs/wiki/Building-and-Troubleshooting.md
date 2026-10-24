# Сборка и решение проблем

## Сборка из исходников

### Требования

- Git.
- JDK {{java_version_build}} для сборки, как в CI. Локальные compile-only JAR Velocity и Nexo содержат классы с bytecode новее Java {{java_version_target}}.
- JDK {{java_version_checkstyle}}, доступный Gradle toolchains, для Checkstyle при полном `build`.
- Доступ к Maven Central, PaperMC и репозиториям optional API.
- Локальные compile-only JAR из каталога `libs/`, уже присутствующие в репозитории.

Версии Java берутся из `gradle.properties`: `java_version_target={{java_version_target}}`, `java_version_build={{java_version_build}}` и `java_version_checkstyle={{java_version_checkstyle}}`. Собственные классы HLib компилируются с `sourceCompatibility` и `targetCompatibility` Java {{java_version_target}}; это не гарантия, что все включённые библиотеки и сторонние плагины имеют такой же bytecode.

Paper compile dependency: `{{dependency_version_bukkit}}`. Дескрипторы объявляют Paper API `{{api_version_paper}}` и Bukkit API `{{api_version_bukkit}}`. Velocity API для компиляции берётся из `libs/Velocity.jar`, версия annotation processor — `{{dependency_version_velocity}}`.

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

`build` зависит от `shadowJar`. Перед компиляцией Gradle копирует Java-файлы в generated sources и заменяет токены `${...}` значениями из `gradle.properties`. Тем же способом заполняются `plugin.yml`, `paper-plugin.yml` и Velocity metadata.

### Локальная проверка без копирования в HCore

`shadowJar` завершает работу задачей `copyShadowJarToHCorePlugins`, которая копирует JAR в локальный каталог `HCore/plugins`, если он существует. Для проверки исходников достаточно `compileJava`; для упаковки без этого копирования исключите завершающую задачу:

```powershell
.\gradlew.bat compileJava --offline
.\gradlew.bat shadowJar -x copyShadowJarToHCorePlugins --offline
.\gradlew.bat clean compileJava checkstyleMain spotbugsMain shadowJar -x copyShadowJarToHCorePlugins --offline
```

Команды запускаются из HLib. Его `settings.gradle.kts` не подключает HCore; эти задачи не конфигурируют соседний проект и не запускают плагины. Режим `--offline` требует уже закэшированных зависимостей. Упаковка обновляет локальный `result/HLib.jar` и результаты задач в `build/`, а завершающая задача копирования не запускается. Третья команда очищает результаты предыдущей сборки и дополнительно выполняет Checkstyle и SpotBugs; первые две их не проверяют. При проверке `HCommandPreflight` в HLib `6.24` третья команда успешно выполнилась, Checkstyle и SpotBugs завершились без ошибок. Копия `HCore/plugins/HLib.jar` и серверные JAR не обновлялись, серверы не запускались.

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

## Проверки проекта

Основные задачи сборки:

```text
generateTokenizedJava → compileJava
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

24 октября 2026 года фабрика переведена на INSERT с no-op при дубликате и SELECT FOR UPDATE в одной транзакции; кеш публикуется после commit, одинаковая повторная загрузка идемпотентна. Временный объединённый стенд с соседним HCore проверил 80 assertions на настоящих HPlayer и MySQL.transaction: внешние конкурентные вставки и rollback, canonical case, один объект при параллельном создании, ошибки SQL/commit без ложного кеша, конфликт ID/имени и post-commit публикацию identity без повторного SQL. База — H2 в памяти с адаптером dialect/advisory locks; сетевые подключения и plugin startup не выполняются. После успешной проверки тестовые исходники, fixtures и runner удалены по прямому указанию владельца; сохраняемой команды повторного запуска этого стенда больше нет. Полный `gradlew --offline --no-daemon -x copyShadowJarToHCorePlugins build` прошёл compilation, Shadow, Checkstyle и SpotBugs. Получен result/HLib.jar версии {{lib_version_plugin}}; копирование в HCore/plugins и установка на сервер в этой проверке не выполнялись.