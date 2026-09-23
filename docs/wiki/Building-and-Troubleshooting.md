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

## Что входит в JAR

Shadow JAR включает Jedis, Commons Pool, HikariCP, Gson, SnakeYAML, MySQL Connector/J, MariaDB driver, GeoIP/MaxMind, NBT API, dnsjava и Vorbis core. Только NBT API relocates в namespace HLib; остальные библиотеки потенциально могут пересечься по версиям с окружением.

Paper, Velocity, ProtocolLib и API hook-плагинов являются `compileOnly` и внутрь HLib не входят. Используйте официальные дистрибутивы и соблюдайте лицензии сторонних компонентов; файлы из `libs/` не являются runtime-набором для копирования на сервер.

## Проверки проекта

Основные задачи сборки:

```text
generateTokenizedJava → compileJava
processResources → classes → jar / shadowJar
checkstyleMain + spotbugsMain + test → check
assemble + check + shadowJar → build
```

В текущем репозитории нет `src/test`, поэтому Gradle сообщает `test NO-SOURCE`. Полный `build` включает также Checkstyle и SpotBugs. Успех этих проверок не подтверждает совместимость каждой интеграции с реальным сервером.

Workflow `build` запускает `clean shadowJar` на Temurin {{java_version_build}}, создаёт или обновляет release и принудительно перемещает тег `v<version>` на текущий commit. Один и тот же version tag не следует считать неизменяемым артефактом. Этот workflow не запускает `checkstyleMain`, `spotbugsMain` и тесты.

## Сетевое поведение при загрузке

При загрузке класса `Tools` выполняется синхронная DNS-проверка режима разработки. На системах, отличных от Windows, entrypoint дополнительно проверяет `{{url_site_api}}/plugins`.

Если проверка обновления не вернула данные, HLib вызывает shutdown всей платформы. При наличии новой версии HLib скачивает JAR, заменяет текущий файл и также завершает платформу для перезапуска. В `info.yml` нет флага отключения updater.

Разрешите исходящие DNS/HTTPS-запросы и заранее проверьте доступность endpoint в закрытой сети.

## Частые проблемы

| Симптом | Причина | Решение |
|---|---|---|
| `UnknownDependencyException: HLib` | HLib не установлен или имя зависимости неверно | Положите `HLib.jar` в корень `plugins/`, объявите `HLib` в descriptor и полностью перезапустите сервер |
| Класс HLib компилируется, но недоступен на Paper | Paper plugin classpath изолирован | Добавьте `dependencies.server.HLib` с `join-classpath: true` |
| Плейсхолдеры PAPI остались в тексте | PlaceholderAPI отсутствует или ещё не загружен | Установите его для нужных подстановок; без PAPI hook возвращает исходный текст |
| `NoClassDefFoundError` из hook-класса | API optional-плагина отсутствует | Установите плагин, добавьте его API как `compileOnly` и проверяйте `Plugin.X.isEnabled()` после загрузки |
| Строка не приходит игроку Velocity | String path использует незарегистрированный `hlib:main` | Передавайте `Informer.component(...)` либо используйте native Velocity `sendMessage` |
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

Первая команда обновляет корневой README и создаёт страницы в `build/wiki/`. Вторая проверяет актуальность README и корректность токенов всех шаблонов без записи файлов. Python использует только стандартную библиотеку.

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