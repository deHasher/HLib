# HLib

[![Build](https://github.com/deHasher/HLib/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/deHasher/HLib/actions/workflows/build.yml)
[![Latest release](https://img.shields.io/github/v/release/deHasher/HLib?display_name=tag)](https://github.com/deHasher/HLib/releases/latest)
[![Java](https://img.shields.io/badge/Java-bytecode_17_%7C_build_25-ED8B00?logo=openjdk&logoColor=white)](https://github.com/deHasher/HLib/wiki/Getting-Started)
[![GPL-3.0](https://img.shields.io/github/license/deHasher/HLib)](LICENSE)

<img src="https://count.getloli.com/@:hlib?name=%3Ahlib&theme=booru-helltaker&padding=1&offset=0&align=top&scale=1&pixelated=1&darkmode=auto" alt=""/>

HLib — публичная библиотека-плагин с общим API для разработки Minecraft-плагинов. Она собирает в одном JAR работу с цветами и Adventure-компонентами, планировщиком, YAML-конфигурациями, предметами, MySQL/MariaDB, Redis, популярными плагинами и ProtocolLib.

Текущая версия: **6.14**.

Один `HLib.jar` содержит точки входа для Paper/Bukkit loader и Velocity. HLib устанавливается рядом с вашим плагином и подключается к проекту как `compileOnly`: в свой JAR библиотеку встраивать не нужно.

## Возможности

- Legacy, HEX и градиентные цвета, Adventure `Component`, сообщения, title, action bar и boss bar.
- Единый async-планировщик для Paper/Bukkit и Velocity, а также sync-задачи для Bukkit.
- Типизированные YAML-конфигурации с ключами, комментариями и значениями по умолчанию.
- Создание `ItemStack`, custom model data, item model, NBT и адаптеры ItemsAdder/Nexo.
- Пулы MySQL/MariaDB и Redis, prepared queries, batch и generated keys.
- Адаптеры LuckPerms, Vault, WorldGuard, WorldEdit, TAB, CMI, PlaceholderAPI и других плагинов.
- Низкоуровневые обёртки ProtocolLib для клиентских и серверных пакетов.
- Утилиты для cooldown, времени, чисел, опыта, локаций, переводов и версий сервера.

## Требования

| Компонент | Конфигурация проекта |
|---|---|
| Версия HLib | `6.14` |
| Bytecode собственных классов HLib | Java `17` |
| Сборка и CI | JDK `25` |
| Checkstyle при полном `build` | JDK `21` |
| Paper API при компиляции | `1.17-R0.1-SNAPSHOT` |
| API в `paper-plugin.yml` / `plugin.yml` | `1.21` / `1.16` |
| Velocity annotation processor | `4.2.1-SNAPSHOT` |
| Дополнения | Только для используемых интеграций |

Для запуска выбирайте Java по требованиям конкретной версии Paper/Velocity и установленных плагинов. Bytecode HLib сам по себе не гарантирует совместимость со старыми серверами. Для Paper 1.20–1.21.11 документация платформы рекомендует Java 21, для Paper 26.1+ — Java 25; совместимость HLib с каждой из этих версий отдельно не подтверждена. [Требования Paper](https://docs.papermc.io/paper/getting-started/).

## Установка

1. Скачайте [последний `HLib.jar`](https://github.com/deHasher/HLib/releases/latest/download/HLib.jar).
2. Положите файл в каталог `plugins/` Paper-сервера или Velocity-прокси.
3. Перезапустите платформу и проверьте HLib через `/plugins` или `/velocity plugins`.
4. Скопируйте тот же JAR в `libs/` проекта вашего плагина.

### Файл `build.gradle.kts`

Добавьте HLib поверх уже настроенной зависимости Paper API или Velocity API: локальный JAR не содержит Maven-метаданных и не заменяет API платформы.

```kotlin
dependencies {
	compileOnly(files("libs/HLib.jar"))
}
```

Официальных Maven-координат у HLib пока нет. API используемых hook-плагинов также подключаются отдельно. Не используйте `implementation` и не добавляйте HLib в shadow JAR своего плагина.

### Файл `plugin.yml`

```yaml
depend: [HLib]
```

### Файл `paper-plugin.yml`

```yaml
dependencies: {server: {HLib: {load: BEFORE, required: true, join-classpath: true}}}
```

### Главный класс Velocity

```java
package com.example.plugin;

import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;

@Plugin(
	id = "example",
	name = "Example",
	version = "1.0.0",
	dependencies = {@Dependency(id = "hlib")}
)
public final class ExamplePlugin {
}
```

## Первый пример

### Файл `ExamplePlugin.java`

```java
package com.example.plugin;

import org.bukkit.plugin.java.JavaPlugin;

public final class ExamplePlugin extends JavaPlugin {
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(new JoinListener(), this);
	}
}
```

### Файл `JoinListener.java`

```java
package com.example.plugin;

import net.dehasher.hlib.Informer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public final class JoinListener implements Listener {
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		Informer.send(
			event.getPlayer(),
			Informer.component("{#55ff55>}Добро пожаловать в игру!{#00aaff<}")
		);
	}
}
```

На Velocity также передавайте в `Informer` именно `Component`: строковый путь для игрока использует plugin messaging.

## Документация

- [Быстрый старт](https://github.com/deHasher/HLib/wiki/Getting-Started)
- [Основной API](https://github.com/deHasher/HLib/wiki/Core-API)
- [Конфигурации](https://github.com/deHasher/HLib/wiki/Configuration)
- [MySQL, MariaDB и Redis](https://github.com/deHasher/HLib/wiki/Databases)
- [Интеграции](https://github.com/deHasher/HLib/wiki/Integrations)
- [ProtocolLib и пакеты](https://github.com/deHasher/HLib/wiki/Packets)
- [Каталог классов](https://github.com/deHasher/HLib/wiki/API-Reference)
- [Сборка и решение проблем](https://github.com/deHasher/HLib/wiki/Building-and-Troubleshooting)

## Важные особенности

- PlaceholderAPI необязателен: без него `PlaceholderAPIHook` возвращает исходный текст. Предметы из ItemsAdder/Nexo требуют соответствующего плагина.
- Конфигурации работают через встроенный `StandaloneConfigurationProvider` или вашу реализацию `ConfigurationProvider`.
- Подключения MySQL/Redis создаёт использующий их плагин; SQL-запросы выполняются синхронно.
- `HCommand*` ограничены фиксированным enum `Permission`; для собственных прав используйте API команд платформы.
- Hook-классы не заменяют сторонние плагины: их API должны присутствовать и на сервере, и в compile classpath вашего проекта.
- На любой ОС, кроме Windows, HLib проверяет обновление при загрузке. Недоступный сервис приводит к остановке платформы; найденная новая версия автоматически скачивается поверх текущего JAR, после чего платформа также останавливается.
- Packet-wrapper’ы зависят от структуры пакетов Minecraft и версии ProtocolLib; проверяйте их на своей целевой версии.

## Сборка из исходников

CI использует JDK 25. Локальные compile-only JAR, включая Velocity и Nexo, содержат классы новее Java 17. Для `checkstyleMain` дополнительно нужен доступный Gradle toolchain JDK 21.

```powershell
.\gradlew.bat clean build
```

```bash
./gradlew clean build
```

Готовый файл появится в `result/HLib.jar`. Полный `build` включает компиляцию, Checkstyle, SpotBugs и создание shadow JAR; тестовых исходников в репозитории нет. CI релиза запускает `clean shadowJar`, поэтому успешный релиз не означает прохождение всех проверок `build`.

## Обновление документации

Исходники: [`docs/README.md`](docs/README.md) и [`docs/wiki/`](docs/wiki/). Версия, Java и параметры API подставляются из [`gradle.properties`](gradle.properties). После изменения исходников выполните:

```bash
python scripts/render-docs.py
```

Workflow [docs](https://github.com/deHasher/HLib/actions/workflows/docs.yml) обновляет README и GitHub Wiki при изменении параметров или документации в `master`. Подробности — в [инструкции для участников](CONTRIBUTING.md).

## Лицензия

HLib распространяется по [GNU GPL-3.0](LICENSE).