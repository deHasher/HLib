# MySQL, MariaDB и Redis

HLib включает HikariCP, MySQL Connector/J, MariaDB driver, Jedis и Commons Pool. Эти библиотеки уже находятся в `HLib.jar`, поэтому отдельное встраивание в ваш plugin JAR не требуется.

| Библиотека | Версия в сборке |
|---|---|
| HikariCP | `{{dependency_version_hikaricp}}` |
| MySQL Connector/J | `{{dependency_version_mysql}}` |
| MariaDB Connector/J | `{{dependency_version_mariadb}}` |
| Jedis | `{{dependency_version_redis}}` |
| Commons Pool | `{{dependency_version_apache_commons}}` |

Все DB-вызовы HLib синхронные. Планирование вне main thread — ответственность вызывающего кода.

HLib сам не открывает SQL- или Redis-подключения. Их создаёт и настраивает использующий библиотеку плагин; для обычных утилит база данных не нужна.

## MySQL и MariaDB

### Создание пула

```java
MySQL mysql = new MySQL(
	"127.0.0.1:3306",
	"minecraft",
	"plugin",
	"password",
	null,
	"mariadb",
	300,
	5,
	1800,
	10
);

if (!mysql.isEnabled()) {
	throw new IllegalStateException("Подключение к MariaDB не создано");
}
```

Аргументы конструктора:

| Аргумент | Значение | Default при `null`/пустом значении |
|---|---|---|
| `address` | `host:port` | обязателен |
| `database` | имя базы | обязателен |
| `username`, `password` | учётные данные | передаются как есть |
| `url` | JDBC template с `{driver}`, `{address}`, `{database}` | стандартный JDBC URL |
| `driver` | `mysql` или `mariadb` | `mysql` |
| `keepaliveTime` | секунды | `300` |
| `connectionTimeout` | секунды | `5` |
| `maxLifetime` | секунды | `1800` |
| `poolSize` | maximum pool size | `10` |

Если HLib-компоненты должны использовать этот pool, зарегистрируйте его:

```java
Tools.setMysqlInstance(mysql);
```

Это один глобальный пул HLib. Не заменяйте чужое подключение, если нескольким плагинам нужен общий доступ. Для собственных таблиц можно хранить `MySQL` локально и вообще не регистрировать его в `Tools`.

## Prepared query

```java
Scheduler.doAsync(() -> {
	AtomicReference<String> name = new AtomicReference<>();

	boolean success = mysql.query("SELECT name FROM players WHERE id = ?")
		.setArgs(playerId)
		.setResult(resultSet -> {
			if (resultSet.next()) {
				name.set(resultSet.getString("name"));
			}
		})
		.execute();

	Scheduler.Bukkit.doSync(() -> {
		String result = success ? name.get() : null;
		Informer.send(player, Informer.component("&7Результат: &f" + result));
	});
});
```

Callback `setResult` выполняется в том же потоке, что и запрос, пока `ResultSet` открыт. Не сохраняйте `ResultSet` для последующего использования.

## INSERT и generated key

```java
Optional<Integer> id = mysql.query("INSERT INTO players (name) VALUES (?)")
	.setArgs(name)
	.executeInsert();
```

Пустой `Optional` означает либо отсутствие generated key, либо ошибку. Эти состояния API не различает.

## Batch

```java
int[] result = mysql.query("INSERT INTO player_tags (player_id, tag) VALUES (?, ?)")
	.addBatch(10, "builder")
	.addBatch(11, "admin")
	.executeBatch();
```

Batch выполняется без явной транзакции. Пустой массив означает и пустой batch, и ошибку.

## Своя DDL-константа

```java
public enum Tables implements MySQLTable {
	PLAYER_TAGS("CREATE TABLE IF NOT EXISTS player_tags (player_id INT NOT NULL, tag VARCHAR(64) NOT NULL)");

	private final String value;

	Tables(String value) {
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}
}
```

```java
mysql.query(Tables.PLAYER_TAGS).setQuiet(false).execute();
```

Overload `query(MySQLTable)` включает quiet-режим автоматически. `setQuiet(false)` нужен, чтобы ошибка DDL была видна в логе и `execute()` вернул `false`.

## SQL-модели HLib

При использовании встроенных моделей после регистрации глобального пула отдельно инициализируйте нужные таблицы:

```java
Scheduler.doAsync(HPlayer::initMySQL);
Cooldowner.initMySQL();
```

`HPlayer.initMySQL()` синхронно создаёт и загружает `hlib_player`; поэтому в примере он запускается вне main thread. `Cooldowner.initMySQL()` сам планирует асинхронную загрузку `hlib_cooldown`. Не используйте эти подсистемы до завершения их загрузки; единого callback готовности нет.

`HPlayer.createHPlayer(name)` создаёт запись или получает её из кэша/SQL, а `getHPlayer(name)` и `getHPlayer(id)` читают только кэш и могут вернуть `null`. Вызывать `initMySQL()` повторно для заполненного кэша нельзя: повторяющиеся ID и имена приводят к исключению. HLib не регистрирует обработчики входа игроков для заполнения модели — это делает использующий её плагин.

## Ограничения MySQL API

- `MySQL.Query` изменяемый и не должен использоваться одновременно из нескольких потоков.
- SELECT требует `setResult`; иначе HLib вызывает `executeUpdate`.
- Query с `setResult` всегда использует `executeQuery`, даже если SQL является UPDATE.
- `query(sql, true).execute()` возвращает `true` и при `SQLException`; quiet-режим не подходит для контроля успеха.
- Batch не открывает явную транзакцию.
- Аргументы SQL-ошибки выводятся в лог. Не передавайте секреты как параметры запросов, если их нельзя логировать.
- `setEnabled(true)` не восстанавливает уже закрытый pool.
- Вызывайте `mysql.shutdown()` при остановке владельца подключения.

## Redis

### Подключение и проверка

```java
Redis redis = new Redis("127.0.0.1:6379", "");

try (Jedis jedis = redis.getPool().getResource()) {
	String pong = jedis.ping();

	if (!"PONG".equals(pong)) {
		throw new IllegalStateException("Redis не ответил PONG");
	}
}

Tools.setRedisInstance(redis);
```

Constructor не проверяет соединение. Выполните `PING` самостоятельно. IPv6-адреса текущим parser не поддерживаются.

### Publish

```java
Scheduler.doAsync(() -> redis.publish("example:updates", "reload"));
```

При ошибке `publish` подавляет исключение и закрывает Redis. Вызывающий код не получает результат публикации.

### Subscribe

`Jedis.subscribe` блокирует поток до unsubscribe. Используйте отдельный executor, а не main thread и не общий короткоживущий task.

```java
ExecutorService subscriberExecutor = Executors.newSingleThreadExecutor();

subscriberExecutor.execute(() -> {
	try (Jedis jedis = redis.getPool().getResource()) {
		jedis.subscribe(new Redis.PubSub() {
			@Override
			public void onMessage(String channel, String message) {
				Informer.send(channel + ": " + message);
			}
		}, "example:updates");
	}
});
```

При shutdown вызовите:

```java
redis.shutdown();
subscriberExecutor.shutdown();
```

Все `Redis.PubSub` хранятся в одном глобальном set и не удаляются автоматически. Shutdown одного Redis вызывает unsubscribe для активных subscribers всех экземпляров; предпочтительно иметь один общий Redis instance.

На Bukkit HLib закрывает зарегистрированные глобальные SQL/Redis-пулы при собственном отключении. На Velocity такого закрытия в entrypoint нет; владелец подключений должен закрывать их сам. Отдельные, не зарегистрированные в `Tools` экземпляры всегда закрывает их владелец.