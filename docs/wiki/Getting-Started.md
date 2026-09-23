# Быстрый старт

На этой странице HLib устанавливается как отдельный плагин и подключается к вашему проекту только для компиляции. Это позволяет всем вашим плагинам использовать один экземпляр библиотеки и её общие подключения.

## 1. Проверьте окружение

- Для сборки исходников используйте JDK {{java_version_build}}; полный `build` также использует JDK {{java_version_checkstyle}} для Checkstyle.
- Paper descriptor объявляет API `{{api_version_paper}}`, Bukkit descriptor — `{{api_version_bukkit}}`.
- Для запуска выберите Java по требованиям своей версии сервера и плагинов. Для Paper 1.20–1.21.11 рекомендована Java 21, для Paper 26.1+ — Java 25. [Требования Paper](https://docs.papermc.io/paper/getting-started/).

Собственные классы HLib компилируются в bytecode Java {{java_version_target}}. Это не обещание поддержки любой платформы на Java {{java_version_target}}; низкоуровневые API и интеграции зависят от версии Minecraft.

## 2. Установите HLib

1. Скачайте [последний HLib.jar](https://github.com/deHasher/HLib/releases/latest/download/HLib.jar).
2. Скопируйте его в корневой каталог `plugins/` Paper или Velocity.
3. Полностью перезапустите платформу.
4. На Paper выполните `/plugins`, на Velocity — `/velocity plugins`.

На любой ОС, кроме Windows, HLib проверяет обновление до загрузки конфигурации. Если `{{url_site_api}}/plugins` недоступен, текущая реализация завершает работу всей платформы. Если найдена новая версия, HLib скачивает её поверх текущего JAR и тоже завершает платформу для последующего перезапуска.

## 3. Подключите JAR к сборке

Создайте в проекте каталог `libs/` и положите туда тот же `HLib.jar`.

HLib добавляется поверх обычной зависимости Paper API или Velocity API. Локальный JAR не содержит Maven-метаданных и не заменяет API платформы; API используемых hook-плагинов также подключаются отдельно.

### Файл `build.gradle.kts`

```kotlin
dependencies {
	compileOnly(files("libs/HLib.jar"))
}
```

### Файл `build.gradle`

```groovy
dependencies {
	compileOnly files("libs/HLib.jar")
}
```

HLib не имеет официальных Maven-координат. `compileOnly` здесь принципиален: библиотека уже будет загружена платформой как отдельный плагин.

## 4. Объявите зависимость плагина

Без декларации зависимости ваш плагин может загрузиться раньше HLib или не получить доступ к его classpath.

### Файл `plugin.yml`

```yaml
depend: [HLib]
```

### Файл `paper-plugin.yml`

```yaml
dependencies: {server: {HLib: {load: BEFORE, required: true, join-classpath: true}}}
```

### Файл `ExamplePlugin.java` для Velocity

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

Не обращайтесь к HLib из конструктора Velocity-плагина: dependency ещё может быть не готова. Начинайте работу с HLib в обработчике `ProxyInitializeEvent` или позже.

## 5. Отправьте первое сообщение на Paper

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
		Informer.send(event.getPlayer(), "&aHLib подключён. {#ffaa00>}Добро пожаловать!{#ff55ff<}");
	}
}
```

## 6. Отправляйте `Component` на Velocity

### Файл `VelocityMessages.java`

```java
package com.example.plugin;

import com.velocitypowered.api.command.CommandSource;
import net.dehasher.hlib.Informer;

public final class VelocityMessages {
	public static void sendReady(CommandSource source) {
		Informer.send(source, Informer.component("&aHLib готов к работе"));
	}
}
```

Строка, отправленная игроку Velocity, направляется через канал `hlib:main`. HLib создаёт идентификатор этого канала, но не регистрирует канал и не содержит backend listener. `Component` отправляется напрямую средствами Velocity и не требует HLib на backend-сервере для доставки сообщения. Установка HLib на обеих сторонах сама по себе не добавляет отсутствующий обработчик строкового канала.

## 7. Подключайте только нужные дополнения

Если код импортирует `ProtocolLibHook`, `LuckPermsHook`, `ItemsAdderHook` или другой hook, API соответствующего плагина тоже должно быть в `compileOnly` вашего проекта, а сам плагин — установлен на сервере.

Перед вызовом hook проверяйте его после загрузки всех плагинов:

```java
if (net.dehasher.hlib.data.Plugin.WORLD_GUARD.isEnabled()) {
	boolean canBuild = net.dehasher.hlib.hook.WorldGuardHook.canBuild(player, location);
}
```

PlaceholderAPI необязателен: без него `PlaceholderAPIHook` оставляет текст без изменений. Наличие стороннего плагина не означает, что все его версии совместимы с текущим hook.

Первый результат `Plugin.isEnabled()` кэшируется. Не вызывайте проверку слишком рано, например из статического инициализатора класса.

## Куда дальше

- [Основной API](Core-API) — сообщения, задачи, cooldown, предметы и утилиты.
- [Конфигурации](Configuration) — типизированные YAML-файлы.
- [Базы данных](Databases) — MySQL, MariaDB и Redis.
- [Интеграции](Integrations) — возможности и требования каждого hook.
- [Пакеты](Packets) — ProtocolLib listeners и wrappers.

## Официальные справочники платформ

- [Paper: plugin.yml](https://docs.papermc.io/paper/dev/plugin-yml/)
- [Paper: paper-plugin.yml и classpath dependencies](https://docs.papermc.io/paper/dev/getting-started/paper-plugins/)
- [Velocity: зависимости плагинов](https://docs.papermc.io/velocity/dev/dependency-management/)
- [Paper: требования Java](https://docs.papermc.io/paper/getting-started/)