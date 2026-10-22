# Интеграции

Hook-классы HLib — тонкие адаптеры над API сторонних плагинов. Они не устанавливают зависимости, обычно не проверяют их наличие и могут не загрузиться без соответствующего API. `HTokensHook` использует общий контракт HLib через Bukkit services; для его вызова не нужны классы HTokens в compile classpath.

## Безопасный шаблон

Проверяйте плагин после завершения загрузки optional dependencies:

```java
if (Plugin.WORLD_GUARD.isEnabled()) {
	boolean allowed = WorldGuardHook.canBuild(player, location);

	if (!allowed) {
		Informer.send(player, Informer.component("&cЗдесь нельзя строить"));
	}
}
```

`Plugin.isEnabled()` кэширует первый результат навсегда. На Bukkit он проверяет наличие плагина в менеджере, а не его текущее состояние `isEnabled()`. Не вызывайте его из static initializer или до завершения загрузки зависимостей; при необходимости дополнительно проверьте `Bukkit.getPluginManager().isPluginEnabled(...)`. Hot reload сторонних плагинов этот кэш не учитывает.

Если исходный код импортирует типы стороннего API, добавьте это API как `compileOnly` и в свой build. HLib не экспортирует compile-only JAR автоматически.

## Каталог hook-классов

| Hook | Требуемый плагин/API | Основные возможности | Важное ограничение |
|---|---|---|---|
| `BotSentryHook` | BotSentry | async добавление IP в whitelist | IP не валидируется, completion не возвращается |
| `ChunkyBorderHook` | ChunkyBorder | border center и size | синхронно читает `borders.json`; rectangular border не поддержан |
| `CMIHook` | CMI и CMILib | user, vanish, god, speed, jail, metadata | большинство изменений не переключает поток автоматически |
| `EmoteCraftHook` | Emotecraft, косвенно ProtocolLib | play/stop emote, client detection | `reload` ожидает отсутствующие ресурсы и может удалить старые JSON |
| `ForceResourcepacksHook` | ForceResourcepacks Velocity | поиск и обновление URL/SHA-1 pack | URL, UUID и hash не валидируются |
| `GadgetsMenuHook` | GadgetsMenu | `PlayerManager` игрока | `getPlugin(Player)` возвращает manager, не plugin instance |
| `HTokensHook` | HTokens с зарегистрированным provider | асинхронные `withdraw`/`deposit` с журналом баланса | `withdraw` требует main thread и online-игрока; неизвестный результат SQL передаётся exceptional future |
| `ItemJoinHook` | ItemJoin | проверка custom item | API создаётся в static initializer |
| `ItemsAdderHook` | ItemsAdder | custom item, model, permission, pack URL/SHA-1 | SHA-1 вычисляется синхронно с диска |
| `LibsDisguisesHook` | LibsDisguises | состояние и снятие disguise | сначала проверяйте `isDisguised` |
| `LiteBansHook` | LiteBans | ban, mute, warn и kick entries | запросы используют `ANY_SERVER_SCOPE` |
| `LuckPermsHook` | LuckPerms | permissions, expiry, prefix/suffix, groups | sync lookup блокирует; setPrefix/setSuffix удаляет все старые meta nodes |
| `NexoHook` | Nexo | custom item, model, permission и item config | config перечитывается с диска; Paper manifest HLib не объявляет Nexo |
| `PlaceholderAPIHook` | PlaceholderAPI, опционально | замена placeholders в строке или списке | без плагина возвращает вход без изменений |
| `PlasmoVoiceHook` | Plasmo Voice | только permission-константа | полноценной API-интеграции нет |
| `ProtocolLibHook` | ProtocolLib | listeners, fake armor stand, wrappers | low-level и version-sensitive; см. [Пакеты](Packets) |
| `SparkHook` | spark | CPU, TPS и MSPT | TPS/MSPT только Bukkit; `-1` означает недоступность |
| `TABHook` | TAB; для reset также LuckPerms | tab/tag prefix и suffix | изменения планируются sync; `null` reset требует LuckPerms |
| `UltimateTimberHook` | UltimateTimber | проверка falling block animation | только Bukkit entity state |
| `VaultHook` | Vault и economy provider | доступ к `Economy` | provider захватывается один раз при загрузке класса |
| `WorldEditHook` | WorldEdit/FAWE | selection и vertical expand | `expandVert` подавляет ошибки |
| `WorldGuardHook` | WorldGuard | regions, flags, build, owner/member, bypass | часть методов не проверяет отсутствующий RegionManager |

## Версии API в сборке

Значения берутся из `gradle.properties`. Это версии compile-only API, а не гарантия совместимости со всеми версиями Minecraft или самого плагина.

| API | Версия |
|---|---|
| PlaceholderAPI | `{{dependency_version_placeholderapi}}` |
| Vault | `{{dependency_version_vault}}` |
| LuckPerms | `{{dependency_version_luckperms}}` |
| WorldGuard | `{{dependency_version_worldguard}}` |
| LibsDisguises | `{{dependency_version_libsdisguises}}` |
| LiteBans | `{{dependency_version_litebans}}` |
| spark | `{{dependency_version_spark}}` |

Часть API подключена из локальных JAR в `libs`; у таких зависимостей нет отдельного параметра версии в `gradle.properties`.

## Telegram для HAuth

HLib 6.25 предоставляет `net.dehasher.hlib.TelegramClient` с конструктором `TelegramClient(String token)`, методом `JsonObject execute(String method, JsonObject payload) throws IOException` и `close()`. Метод синхронно отправляет JSON POST к официальному Bot API и возвращает полный envelope, включая `ok: false`, `error_code` и `parameters.retry_after`. HAuth разбирает ответ и отвечает за polling, команды, уведомления и повтор после 429. Вызовы следует выполнять вне основного потока.

Один клиент допускает до четырёх параллельных запросов, поэтому long polling не блокирует отправку уведомления. Лимиты: соединение и ожидание connection pool — 10 секунд, чтение — 30 секунд, весь запрос — 45 секунд, ответ — 4 MiB. Автоматические повторы, redirects и cookies выключены. HTTP CONNECT поддерживает scoped proxy credentials без JVM-флагов, SOCKS4a/SOCKS5 передают имя конечного хоста прокси. TLS использует стандартную проверку сертификата и hostname. Глобальные proxy properties и `Authenticator` не меняются.

Токен HAuth не записывается в `info.yml`. Сообщения исключений transport не включают URL, токен, credentials, содержимое запроса или вложенные сетевые исключения. `close()` отменяет активные запросы и закрывает пул; повторный `close()` безопасен, новые запросы после закрытия отклоняются. Настройки прокси описаны в [конфигурации HLib](Configuration#geoip-и-telegram-hauth).

## HTokens

`HTokensHook.Provider` находится в HLib `6.22`. HTokens регистрирует реализацию через Bukkit `ServicesManager` после готовности собственных данных и снимает регистрацию при отключении. Hook каждый раз получает регистрацию заново и проверяет включённость её владельца. `getProvider()` возвращает `null`, если сервиса нет; `isAvailable()` проверяет зарегистрированный сервис, а не кэш `Plugin.isEnabled()`.

Вызывающий плагин использует ту же установленную HLib без её встраивания в свой JAR. Обратной compile-only зависимости HLib от HTokens не требуется.

`HTokensHook.getBalance(HPlayer)`, `getBalance(Player)` и `getBalance(String)` возвращают `CompletableFuture<Long>` с балансом в фэйтиках. Объектные перегрузки передают имя в `Provider.getBalance(String)`. HTokens читает баланс асинхронно через SQL, используя существующий `TokenController.balance`; первый запрос известного HLib-профиля создаёт счёт с настроенной начальной суммой. `Player` не обязан быть online. Отсутствие provider даёт failed future с `IllegalStateException`, неизвестный профиль и ошибка БД сохраняются как исключение future; ноль означает настоящий нулевой баланс. `null` отклоняется синхронно через `Objects.requireNonNull`. Метод не выводит сообщения; поток завершения future не фиксирован. Полученная сумма является снимком и не заменяет проверку достаточности внутри `withdraw`.

`HTokensHook.withdraw(player, amount)` и `deposit(player, amount)` возвращают `CompletableFuture<Boolean>`; без сервиса результат — `false`. Положительная сумма обязательна. `withdraw` запускается на основном потоке и требует online-игрока; `deposit` допускает возврат отключившемуся игроку, если HTokens доступен. `true` подтверждает изменение баланса, `false` обозначает известный отказ. Неопределённый результат SQL передаётся exceptional future; поток завершения future не фиксирован.

HTokens выполняет эти операции через общий busy lock и `TokenController.change`, записывая `TAKE`/`GIVE` в `htokens_log`. Этот API не принимает команды выдачи, не создаёт `htokens_purchase` и не управляет исполнением внешних действий.

Потребитель сначала готовит команды и проверяет возможность действия, затем ждёт успешного `withdraw` и выполняет команды на основном потоке. Для предварительной проверки зарегистрированных Bukkit-команд используется [`HCommandPreflight` из command API](Core-API#предварительная-проверка-команд). Компенсацию через `deposit` можно делать одной попыткой, только когда действие заведомо ещё не запускалось. После попытки внешней команды или неизвестной SQL-ошибки автоматический возврат и повтор опасны из-за возможного уже выполненного действия.

В HCore `/iskin` используется фиксированная цена одного случайного скина и настроенный `forceopen` без ключей. Закрытие меню не отменяет оплату/действие. Подтверждённое списание компенсируется одной попыткой `deposit`, если до команды игрок вышел, HCore остановлен или sync-задачу нельзя запланировать; после попытки команды возврата нет. Магазины HTokens используют отдельный путь с аудитом покупок.

## PlaceholderAPI

```java
String message = PlaceholderAPIHook.setPlaceholders(player, "&aИгрок: %player_name%");
Informer.send(player, Informer.component(message));
```

Hook проверяет `Plugin.PLACEHOLDER_API.isEnabled()` до обращения к PlaceholderAPI. Если плагин отсутствует, исходная строка или список возвращается без изменений. Поэтому обычные `ItemBuilder`, строковые сообщения, title и action bar в `Informer` могут работать без PlaceholderAPI; незаменённые `%...%` при этом сохраняются в тексте.

Расширения placeholders устанавливаются отдельно. Для корректного порядка загрузки объявляйте используемую интеграцию в descriptor своего плагина; на Paper также обеспечьте доступ к её classpath.

## LuckPerms

Для неблокирующей проверки используйте async overload:

```java
LuckPermsHook.hasPermissionAsync(player.getUniqueId(), "example.feature")
	.thenAccept(allowed -> {
		if (!allowed) {
			return;
		}

		Scheduler.Bukkit.doSync(() -> grantFeature(player));
	});
```

Строковые overload по имени строят offline UUID и обычно не совпадают с UUID online-mode аккаунта. Передавайте реальный UUID.

`setPrefix` и `setSuffix` сначала удаляют все meta nodes соответствующего типа у пользователя, а не только значение HLib. Используйте их только если это ожидаемая политика.

## WorldGuard

```java
if (!Plugin.WORLD_GUARD.isEnabled()) {
	return;
}

if (WorldGuardHook.inRegion(location, "spawn")) {
	Informer.send(player, Informer.component("&eВы на спавне"));
}
```

`inGlobal(location)` означает отсутствие применимых локальных regions, а не наличие объекта `__global__`. Custom flags регистрируйте в правильной фазе загрузки WorldGuard и обрабатывайте `FlagConflictException`.

## ItemsAdder и Nexo

`ItemBuilder.setMaterial("namespace:id", player)` пытается разрешить предмет сначала через ItemsAdder, затем через Nexo. Registry контента может быть ещё не готов сразу после включения плагина.

В HLib есть `Plugin.Loaded.ITEMS_ADDER` и `Plugin.Loaded.NEXO` с callbacks, но этот репозиторий сам не переводит их в состояние loaded. Подписывайтесь на официальное событие готовности конкретного плагина или вызывайте адаптер после его полной загрузки.

## TAB и CMI

`TABHook.setPrefix` меняет tab prefix и nametag prefix отдельными sync-задачами. При наличии CMI tag prefix дополняется `%cmi_user_glow_code%`. Если передать `null`, HLib пытается восстановить group value через LuckPerms.

## Производительность

Следующие вызовы могут блокировать текущий поток:

- Чтение `ChunkyBorder` border JSON.
- Чтение item config в `NexoHook`.
- SHA-1 resource pack в `ItemsAdderHook`.
- Синхронные name/UUID lookup в `LuckPermsHook`.

Переносите файловые и сетевые операции в async-поток, но возвращайте любые изменения Bukkit entity/world/inventory в main thread.