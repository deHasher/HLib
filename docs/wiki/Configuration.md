# Конфигурации

HLib содержит два уровня конфигураций:

1. Объектная модель `net.dehasher.hlib.file.Configuration` — Java-поля превращаются в YAML и обратно.
2. Bukkit-подобный API `net.dehasher.hlib.file.configuration.*` — ручная работа с путями и YAML-секциями.

Для обычного плагина рекомендуется объектная модель с `StandaloneConfigurationProvider`. Один и тот же файловый provider используется на Bukkit/Paper и Velocity; отдельная реализация provider со стороны другого плагина не нужна.

## Объектная конфигурация

### Файл `PluginSettings.java`

```java
package com.example.plugin.config;

import net.dehasher.hlib.file.Annotations.Comment;
import net.dehasher.hlib.file.Annotations.Final;
import net.dehasher.hlib.file.Annotations.Key;
import net.dehasher.hlib.file.Configuration;
import net.dehasher.hlib.file.ConfigurationSection;

@Comment("Основные настройки плагина.")
public final class PluginSettings extends Configuration {
	@Final
	@Key("version")
	public String version = "1";

	@Key("messages")
	public Messages messages = new Messages();

	public static final class Messages implements ConfigurationSection {
		@Comment("Сообщение при отсутствии права.")
		@Key("no-permission")
		public String noPermission = "&cНедостаточно прав.";
	}
}
```

### Файл `ConfigService.java`

```java
package com.example.plugin.config;

import net.dehasher.hlib.file.Configuration;
import net.dehasher.hlib.file.provider.StandaloneConfigurationProvider;

import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigService {
	public static PluginSettings load(Path dataFolder) {
		Path file = dataFolder.resolve("config.yml");

		PluginSettings settings = Configuration.builder(PluginSettings.class)
			.file(file)
			.provider(StandaloneConfigurationProvider.class)
			.build();

		boolean loaded = settings.load();

		if (!loaded && Files.exists(file)) {
			throw new IllegalStateException("Файл config.yml повреждён или недоступен");
		}

		if (!loaded) {
			settings.save();
			loaded = settings.load();
		}

		if (!loaded) {
			throw new IllegalStateException("Не удалось создать config.yml");
		}

		return settings;
	}
}
```

Первый `load()` инициализирует внутренний `YamlConfiguration` и serializer даже при отсутствии файла. Вызов `save()` сразу после `build()` без этой инициализации приведёт к ошибке. `load()` сообщает об успешном чтении YAML, но не гарантирует корректность каждого поля: несовместимый тип поля логируется, а старое значение остаётся. Проверяйте бизнес-ограничения отдельно.

## Аннотации

| Аннотация | Поведение |
|---|---|
| `@Key("name")` | задаёт имя YAML-ключа вместо имени поля |
| `@Comment("text")` | добавляет один или несколько комментариев |
| `@Final` | сохраняет поле, но не загружает его значение из YAML |
| `@Ignore` | полностью исключает поле из загрузки и сохранения |

Вложенная объектная секция должна реализовывать маркер `net.dehasher.hlib.file.ConfigurationSection` и уже существовать в поле. `null`-секция автоматически не создаётся.

## Правила модели

- Класс конфигурации должен иметь доступный constructor без аргументов.
- Обрабатываются поля, объявленные непосредственно в каждом классе модели; не рассчитывайте на унаследованные config-поля.
- Для коллекций используйте `List`, `Map` и простые типы. Загрузка массивов в текущей версии неисправна.
- Неизвестные ключи будут потеряны при следующем полном `save()`.
- Объект конфигурации и SnakeYAML state не потокобезопасны.
- File I/O синхронный. Не сохраняйте файл в горячем обработчике main thread.
- Параллельное сохранение нескольких конфигураций в одной директории может конфликтовать из-за общего временного имени `___tmpconfig`.
- Ошибка файловой записи high-level `save()` логируется, но не возвращается вызывающему коду; ошибка сериализации может выбросить исключение.

## Reload

```java
if (!settings.load()) {
	throw new IllegalStateException("Не удалось перечитать config.yml");
}
```

`load()` обновляет поля существующего объекта. Для ручного `YamlConfiguration.loadFromString(...)` создавайте новый объект на каждую полную загрузку: метод не очищает ключи, удалённые из новой строки.

## Serializable-объекты

Низкоуровневый YAML API поддерживает `ConfigurationSerializable`. Объект должен вернуть `Map<String, Object>`, иметь alias и один из следующих deserializer-контрактов:

1. Статический `deserialize(Map)`.
2. Статический `valueOf(Map)`.
3. Публичный constructor с `Map`.

### YAML

```yaml
spawn: {"==": "point", "x": 10, "y": 64, "z": -5}
```

Регистрируйте класс через `ConfigurationSerialization.registerClass(...)` до загрузки. `ConfigurationBuilder.addSerializable(...)` с новым, ещё не инициализированным provider вызывает serializer до его создания и завершается ошибкой.

## Два интерфейса `ConfigurationSection`

| Полное имя | Назначение |
|---|---|
| `net.dehasher.hlib.file.ConfigurationSection` | пустой marker для вложенных Java-объектов high-level модели |
| `net.dehasher.hlib.file.configuration.ConfigurationSection` | полный API путей, typed getters, defaults и вложенных YAML-секций |

При ошибочном import код может компилироваться не так, как ожидается, либо не сериализовать вложенный объект.

## Каталог конфигураций плагина

`Tools.reloadFiles(dataFolder, ConfigFiles.class)` принимает enum, реализующий `PluginCfg`, и использует `StandaloneConfigurationProvider`. Для каждой константы `Settings` загрузчик ищет модель сначала в `<пакет enum>.config.Settings`, затем в `<пакет enum>.Settings`, используя classloader самого enum. Если класса нет, файл пропускается.

Каждая модель должна содержать ключ `version`. Существующий файл без этого ключа переименовывается в `<имя файла>.old.<число>`, после чего создаются настройки по умолчанию. Политику обновления схемы реализует `PluginCfg.reload(...)`; универсальной миграции значений нет. Для прямой загрузки одной конфигурации достаточно builder из примера выше.

## Собственный `info.yml` HLib

После загрузки HLib создаёт `info.yml` в своём data directory. На Paper/Bukkit типичный путь — `plugins/HLib/info.yml`; на другой платформе используйте каталог данных, предоставленный loader. Это внутренняя конфигурация HLib, но некоторые общие утилиты читают её значения.

| Раздел | Для чего используется |
|---|---|
| `version` | версия схемы `{{lib_version_info}}`; не изменять |
| `console-name` | отображаемое имя консоли |
| `server.id`, `server.name` | идентификатор сервера и DB/placeholder namespace |
| `placeholders` | `{prefix}` и `{site_url}` |
| `admins` | fallback-администраторы для `Tools.isPerm` |
| `fake-online` | расчёт отображаемого online |
| `headers` | JSON с HTTP-заголовками по точному URL для `Informer.url` и `Informer.head` |
| `api-notifications` | URL и шаблоны callback-сообщений |
| `short-numbers-suffixes` | правила `ShortNumbersFormatter` |
| `date-time` | русские формы дат и длительности |

`api-notifications` не являются фоновой очередью: `APINotificationController.send` выполняет синхронный HTTP-запрос с большими timeout. Не вызывайте его в server main thread.

По умолчанию `admins` содержит `{{author}}` и `{{author_twink}}`. Перед использованием `Tools.isPerm` настройте список для своего сервера; пустой список отключает этот обход.

`admins` — глобальный fallback для `Tools.isPerm` при разрешённом для конкретного элемента `Permission` обходе. Он распространяется и на плагины, которые используют эту проверку; для собственных прав можно вызывать API платформы напрямую.

### HTTP-заголовки

Значение `headers` — строка JSON. Ключ верхнего уровня должен точно совпадать с URL запроса после подстановки `{prefix}` и `{site_url}`; значение — объект со строковыми заголовками. Заголовки из аргумента метода имеют приоритет над конфигурацией, совпадение имени заголовка следует правилам HTTP.

Файл `info.yml`:

```yaml
headers: "{\"https://example.com/api\":{\"Authorization\":\"Bearer EXAMPLE_KEY\",\"X-Client\":\"example-plugin\"}}"
```

По умолчанию `headers` пуст. Настройка применяется только к HTTP helper-методам `Informer`; произвольные сторонние HTTP-клиенты её не используют.

## Низкоуровневый YAML API

Пакет `net.dehasher.hlib.file.configuration` повторяет привычную модель Bukkit:

- `MemoryConfiguration` — конфигурация в памяти.
- `YamlConfiguration` — UTF-8 YAML-файл или строка.
- `ConfigurationSection` — `getString`, `getInt`, `getList`, `getObject`, `createSection` и defaults.
- `FileConfiguration` — загрузка/сохранение `File`, `Reader` и строки.

Переданный в `FileConfiguration.load(Reader)` reader закрывается внутри метода. Настройка `YamlConfiguration.options().indent(...)` при прямом API в текущей версии фактически не меняет отступ SnakeYAML dump.