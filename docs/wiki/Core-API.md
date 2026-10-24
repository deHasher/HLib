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
Scheduler.Bukkit.doSyncLater(() -> Informer.send(player, "Готово"), 20L);
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

Перегрузки `start` и `getTimeLeft` принимают `TimeUnit`, в том числе `MILLISECONDS`. Перевод остатка в секунды округляется вниз: для проверки короткой задержки используйте `inCooldown` или остаток в миллисекундах. `remove(name, type)` очищает только указанный ключ и тип. Для локальных типов с `strict == false` эти операции не обращаются к БД.

Хранилище потокобезопасно, но последовательность `inCooldown` → `start` не является одной атомарной операцией. Если один ключ используется несколькими потоками, защищайте такую последовательность общей блокировкой вызывающего кода.

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

Для optional custom item предусмотрен `setMaterial("namespace:id", player, Material.EMERALD)`: если идентификатор не разрешился в ItemsAdder/Nexo или Bukkit Material, либо строка пуста, используется указанный fallback. Это относится и к Nexo ID без namespace. `null` fallback означает `STONE`; overload без явного fallback также использует `STONE`. Обычные Bukkit material names дополнительно валидируйте при чтении настройки.

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

`data.CompiledPattern.IDENTIFIER.matches(value)` проверяет полное совпадение идентификатора из 1–32 символов: строчные ASCII-буквы `a–z`, цифры `0–9` и дефис `-`.

## Геолокация IP

`Tools.getGeo(String)` и `Tools.getGeo(InetAddress)` возвращают `data.Geo` либо `null`, когда база ещё недоступна, адрес неверен или в базе нет записи. Строковый overload принимает только IPv4/IPv6 и не выполняет DNS-запросы. `Geo` — record с компонентами `countryCode`, `country`, `region`, `city`, `latitude`, `longitude`. Названия локализованы согласно `Info.GeoIP.locale` с fallback на английский; отсутствующие названия — пустые строки, координаты могут быть `null`. IP-геолокация приблизительна.

HLib сам открывает сохранённый `GeoLite2-City.mmdb` при загрузке и асинхронно скачивает свежую копию при каждом запуске. Lookup читает общий потокобезопасный reader в памяти и не обращается к сети. Загружаемый файл ограничен 256 MiB и проверяется как GeoLite2-City до замены; при неудаче сохраняется прежняя рабочая база. На первом запуске до завершения скачивания API может вернуть `null`. При выключении HLib загрузка отменяется и reader закрывается.

По умолчанию используется существующий источник проекта `{{url_site}}/libs/files/GeoLite2-City.mmdb`; HLib не требует license key и не гарантирует свежесть самого внешнего источника. Официальные загрузки MaxMind требуют аккаунт и ключ: [GeoLite documentation](https://dev.maxmind.com/geoip/geolite2-free-geolocation-data/). HLib не обходит требования официального сервиса и не содержит чужих учётных данных.

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

Для HFriends предусмотрены `HFRIENDS_COMMAND_FRIENDS` (`hfriends.command.friends`), `HFRIENDS_COMMAND_FMSG` (`hfriends.command.fmsg`), `HFRIENDS_ADMIN_RELOAD` (`hfriends.admin.reload`) и `HFRIENDS_BYPASS_FRIENDSHIP` (`hfriends.bypass.friendship`). Последнее право позволяет отправлять личные сообщения без дружбы. Все четыре элемента допускают стандартный обход для `Info.admins` при проверке через `Tools.isPerm`; значение enum само по себе не выдаёт право игрокам.

### Предварительная проверка команд

`net.dehasher.hlib.command.HCommandPreflight` и `HCommandBukkit.canExecute(sender, alias, arguments)` добавлены в HLib `6.24`. Базовый `canExecute` возвращает `true`; подкласс команды переопределяет его для проверки доменных условий без запуска команды, оплаты или изменения игрового состояния. При отказе реализация может сообщить причину нужному адресату и вернуть `false`. `HCommandPreflight` не отправляет сообщения и не выполняет команды.

Потребитель на основном потоке вызывает `HCommandPreflight.prepare(sender, commands)`. Пробелы по краям и один начальный `/` убираются; label приводится к нижнему регистру, а аргументы делятся по одиночному пробелу с сохранением пустых аргументов между пробелами, как при Bukkit dispatch. Для каждой строки сохраняются объект реально зарегистрированной команды, label и неизменяемый список аргументов. Внешние изменения исходного списка команд не меняют подготовленную проверку.

`Prepared.check()` также требует основной поток и возвращает `ALLOWED`, `DENIED` или `UNAVAILABLE`. Пустой список команд, неизвестная или пустая команда, замена объекта команды и отключённый владелец `PluginIdentifiableCommand` дают `UNAVAILABLE`. Для команд типа `HCommandBukkit` вызывается `canExecute`; первый `false` даёт `DENIED`. Каждый вызов получает отдельный массив аргументов, поэтому реализация не меняет аргументы повторной проверки. Если отказов нет, результат — `ALLOWED`. Исключения `canExecute` передаются вызывающему коду без подавления.

Проверяйте результат до списания и снова на основном потоке после подтверждённой асинхронной оплаты, непосредственно перед первым внешним действием. При отказе до первой попытки команды потребитель может выполнить одну компенсацию; сообщения об общей недоступности и обработку ошибок также определяет потребитель. `HCommandPreflight` не резервирует призы или ресурсы, не проводит оплату и не делает список произвольных команд атомарной транзакцией. Для других Bukkit-команд `ALLOWED` означает только неизменность регистрации и включённость владельца, если он доступен через `PluginIdentifiableCommand`. Доменную проверку предоставляет сама команда через `canExecute`; обычные проверки прав, cooldown и лимита внутри `HCommandBukkit.execute` остаются на этапе выполнения и отдельно не запускаются.

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