# Основной API

Эта страница описывает классы, с которых стоит начинать. Полный перечень публичных классов находится в [каталоге API](API-Reference).

## Цвета и компоненты

Класс `Colors` понимает несколько форматов:

| Формат | Пример |
|---|---|
| Legacy | `&aГотово`, `&lЖирный` |
| HEX с амперсандом | `&#55ff55Текст` |
| HEX в скобках | `{#55ff55}Текст` |
| Mojang HEX | `&x&5&5&f&f&5&5` |
| Градиент | `{#ff5500>}Текст{#aa00ff<}` |

### Файл `Messages.java`

```java
package com.example.plugin;

import net.dehasher.hlib.Colors;
import net.kyori.adventure.text.Component;

public final class Messages {
	public static Component ready() {
		return Colors.setComponent("{#55ff55>}HLib готов{#00aaff<}");
	}
}
```

Основные методы:

- `Colors.set(String)` возвращает строку с Minecraft-кодами `§`.
- `Colors.setComponent(String)` возвращает Adventure `Component`.
- `Colors.clear(Object)` удаляет цвета и оформление.
- `Colors.plain(Component)` извлекает простой текст.
- `Colors.cleanEquals*` сравнивает текст без оформления.
- `Colors.get(String)` возвращает стандартный или произвольный HEX-цвет.

Не передавайте `null`. Для градиента используйте непустой текст без вложенной `{`.

## Сообщения

`Informer` выбирает реализацию по текущей платформе.

### Paper/Bukkit

```java
Informer.send(player, Informer.component("&aОперация завершена"));
Informer.sendTitle(player, "&6Заголовок\n&fПодзаголовок", 10, 60, 10);
Informer.sendActionBar(player, "&eПодсказка");
Informer.broadcast("&bОбъявление");
```

Пять аргументов `sendTitle` означают `player`, `message`, `fadeIn`, `stay`, `fadeOut`. Время задаётся в тиках. Передача только части времён не поддерживается.

Строковые сообщения, title и action bar используют PlaceholderAPI только при его наличии. Без него текст остаётся без подстановки внешних placeholders. Переданный `Component` отправляется напрямую; PlaceholderAPI внутри него автоматически не вызывается.

### Velocity

```java
Informer.send(source, Informer.component("&aОперация завершена"));
Informer.sendTitle(player, "&6Заголовок\n&fПодзаголовок", 10, 60, 10);
Informer.sendActionBar(player, "&eПодсказка");
```

Для игрока Velocity всегда используйте `Component`. Строка отправляется через backend plugin message, а готового обработчика и регистрации канала в HLib нет.

`Informer.broadcast(...)` также отправляет строки через этот канал. Для самостоятельного broadcast на Velocity обходите игроков через `ProxyServer.getAllPlayers()` и вызывайте `Informer.send(player, component)`.

### Boss bar

```java
org.bukkit.boss.BossBar bar = Informer.BossBar.createBukkit(
	Colors.set("&aЗагрузка"),
	Informer.BossBar.Color.GREEN,
	Informer.BossBar.Style.SOLID,
	0.5D
);
```

Progress должен находиться в диапазоне `0.0–1.0`.

### HTTP helper

`Informer.url(...)` и `Informer.head(...)` выполняют синхронный сетевой запрос. Не вызывайте их в main thread сервера. При ошибке методы возвращают пустую строку или `null` и печатают stack trace; статус ответа отдельным объектом не предоставляется.

Поддерживаются GET, POST, JSON POST и HEAD; timeout указывается в секундах, по умолчанию — `3`. `url` возвращает тело ответа, в том числе тело HTTP-ошибки; `head` при статусе от `400` возвращает `null`.

```java
Scheduler.doAsync(() -> {
	String response = Informer.url(
		"https://example.com/api/status",
		Map.of("X-Client", "example-plugin")
	);
	Informer.send(response);
});
```

Дополнительные overload принимают `Map<String, String>` с заголовками. Они переопределяют заголовки для того же URL из `info.yml`; точное устройство настройки описано в [конфигурациях](Configuration).

## Планировщик

Общий facade `Scheduler` предоставляет async-задачи. Delay и period задаются в Minecraft-тиках; на Velocity один тик преобразуется в 50 миллисекунд.

```java
Scheduler.doAsyncLater(() -> Informer.send("Прошла одна секунда"), 20L);

int taskId = Scheduler.doAsyncRepeatWithId(
	() -> Informer.send("Периодическая задача"),
	0L,
	20L
);

Scheduler.stop(taskId);
```

Для Bukkit доступны sync-методы:

```java
Scheduler.Bukkit.doSync(() -> player.teleport(location));
Scheduler.Bukkit.doSyncLater(() -> player.sendMessage("Готово"), 20L);
```

Правила потоков:

- Bukkit API, мир, сущности и инвентари меняйте в main thread.
- SQL, HTTP и файловые операции выполняйте вне main thread.
- Общего `Scheduler.doSync` для Velocity нет.
- Завершившиеся id автоматически не удаляются из внутреннего реестра; отменяйте долгоживущие задачи явно.

## Cooldown

`Cooldowner` хранит время окончания в памяти. Некоторые встроенные типы помечены как strict и дополнительно требуют MySQL; `COMMAND` работает без базы.

```java
String key = Cooldowner.key("example", player.getUniqueId().toString());

if (Cooldowner.inCooldown(key, Cooldowner.Type.COMMAND)) {
	long seconds = Cooldowner.getTimeLeft(key, Cooldowner.Type.COMMAND);
	Informer.send(player, Informer.component("&cПодождите " + seconds + " сек."));
	return;
}

Cooldowner.start(key, Cooldowner.Type.COMMAND, 10L);
```

Типы cooldown зафиксированы в enum и расширить их без изменения HLib нельзя. Всегда добавляйте namespace своего плагина в ключ, чтобы избежать коллизий.

Для strict-типов сначала зарегистрируйте пул через `Tools.setMysqlInstance(...)` и вызовите `Cooldowner.initMySQL()`. Инициализация асинхронна; её готовность отдельным callback не сообщается. Обычный `COMMAND` этого не требует.

## Предметы

`ItemBuilder` — Bukkit-only API.

```java
ItemStack item = new ItemBuilder()
	.setMaterial(Material.DIAMOND)
	.setName("&bРедкий алмаз")
	.setLore(List.of("&7Создан через HLib"))
	.setAmount(1)
	.setGlowing(true)
	.addNBT("example-item", true)
	.build(player);
```

Доступные операции:

- Bukkit `Material` или строковый material id.
- Имя, lore, количество и unsafe enchantments.
- Unbreakable, glow и item flags.
- Pattern для banner и shield.
- Custom model data и item model для 1.21.
- Строковые и boolean NBT-значения внутри compound `hlib`.
- Разрешение namespace-предметов ItemsAdder и Nexo при наличии соответствующего плагина.

NBT API версии `{{dependency_version_itemnbt}}` включён в HLib. PlaceholderAPI для сборки обычного предмета не обязателен; он используется только для подстановок в material, имени и lore, если установлен.

Не переиспользуйте один builder после смены material между potion, armor, banner и shield: внутренние type-флаги не сбрасываются. Не передавайте `null` в `setAmount`.

## Время и числа

### Длительность

```java
String text = DateTimeFormatter.format(3_661L);
Long millis = DateTimeFormatter.getMillisFromCustomString("15m");
```

Поддерживаются суффиксы `s`, `m`, `h`, `d`, `w`, `y`. Месяц в форматтере считается как 30 дней, год — 365 дней. Форматирование ориентировано на русские строки из `info.yml`.

### Короткие числа

```java
String value = ShortNumbersFormatter.format(1_250_000L);
```

Суффиксы настраиваются в `short-numbers-suffixes` файла `info.yml` из data directory HLib. На Paper/Bukkit типичный путь — `plugins/HLib/info.yml`. Деление целочисленное: дробная часть отбрасывается.

### Безопасный parsing

`Tools.parseInt`, `parseLong`, `parseDouble` и похожие методы возвращают `0` при любой ошибке. Используйте их только там, где нулевое значение допустимо; для валидации пользовательского ввода лучше применить обычный parser с обработкой исключения.

## Мир, игроки и опыт

Следующие операции используют Bukkit/Paper API:

| Класс | Назначение |
|---|---|
| `Locationer` | центр блока, направления, 2D-дистанция и повороты `BlockFace` |
| `Experience` | total experience и расчёт опыта уровня |
| `Vector3D` | конструкторы из `Location` и `Vector`; арифметические операции возвращают новый вектор |
| `Tools` | ближайшие блоки/игроки/сущности, damager, target player, UUID и skin data |

Проверяйте `null`, совпадение миров и правильный поток. `Tools.getNearbyBlocks` возвращает куб, а не сферу.

## Переводы Minecraft

`Translator` асинхронно скачивает vanilla locale-файлы и устанавливает Adventure `TranslationRegistry`.

```java
Translator.init();
String materialName = Translator.getMaterialName(Material.DIAMOND, player);
```

Кроме `getMaterialName(...)`, доступны `getBlockName(...)`, `getName(...)` для translation key и overload с явным `Locale`. По умолчанию используется `Locale.US`, для игрока — его locale.

Готовность не возвращается через `Future` или callback. До завершения загрузки метод может вернуть translation key. Инициализация запрашивает manifest по адресу `{{url_repos}}/minecraft/version_manifest.json`; наличие файлов в `plugins/HLib/lang` не отменяет этот сетевой запрос. Используйте перевод Minecraft только на Bukkit/Paper.

## Базовые классы команд

`HCommandBukkit`, `HCommandVelocity` и `HCommandAnimatedBukkit` не являются универсальным command framework:

- Создание объекта не регистрирует команду в платформе.
- Право строится как `<имя класса плагина без Loader>.command.<имя команды>` в нижнем регистре и должно существовать в фиксированном enum `Permission`, иначе выполнение завершится `IllegalArgumentException`.
- Bukkit-конструктор изменяет список aliases, поэтому `List.of(...)` использовать нельзя.
- Lifetime-limit автоматически не сбрасывается.
- Сообщения об отсутствии права, cooldown и лимите из `StorageController` по умолчанию пустые. Их задаёт использующий библиотеку плагин через `setNoPermMessage`, `setCommandCooldownMessage` и `setCommandLimitMessage`; для времени cooldown используется `{time}`.

Для команд с произвольными permission nodes используйте штатный command API Paper или Velocity. Базовые классы HLib подходят проектам, чьи права уже представлены в `Permission`.

## Другие специализированные классы

| Класс | Когда использовать |
|---|---|
| `Reflections` | загрузка собственного versioned NMS bridge |
| `Requestor` | временные запросы; корректно используйте только `TimeUnit.SECONDS` |
| `Performance` | локальные метрики и обмен через Redis; CPU/TPS/MSPT через Spark |
| `data.HPlayer` | собственный числовой ID и имя игрока в SQL; требуется явная инициализация |
| `StringRepairer` | удаление bidi и RTL-диапазонов; не для multilingual chat |
| `Console` | упрощённый Bukkit sender с безусловными правами, не полная консоль |

Подробные ограничения этих классов приведены в [каталоге API](API-Reference) и разделе [решения проблем](Building-and-Troubleshooting).

`Performance.init()`, периодические вызовы `Performance.update()`, `Translator.init()` и инициализацию SQL-моделей запускает использующий библиотеку плагин. HLib не включает эти подсистемы автоматически.