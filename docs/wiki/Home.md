# HLib

HLib — публичная библиотека-плагин с общим API для разработчиков Minecraft-плагинов. Она предоставляет готовые решения для текста, задач, конфигураций, предметов, баз данных, сторонних плагинов и ProtocolLib без необходимости копировать одинаковый код между проектами.

Текущая версия: **{{lib_version_plugin}}** · [Скачать HLib.jar](https://github.com/deHasher/HLib/releases/latest/download/HLib.jar) · [Исходный код](https://github.com/deHasher/HLib)

## С чего начать

| Задача | Страница |
|---|---|
| Установить HLib и подключить к проекту | [Быстрый старт](Getting-Started) |
| Отправить сообщение, запустить задачу, создать предмет | [Основной API](Core-API) |
| Создать типизированный YAML-файл | [Конфигурации](Configuration) |
| Подключить MySQL, MariaDB или Redis | [Базы данных](Databases) |
| Использовать LuckPerms, WorldGuard, TAB, CMI и другие плагины | [Интеграции](Integrations) |
| Читать или отправлять пакеты ProtocolLib | [Пакеты](Packets) |
| Найти назначение конкретного класса | [Каталог API](API-Reference) |
| Собрать проект или решить ошибку запуска | [Сборка и решение проблем](Building-and-Troubleshooting) |

## Что даёт HLib

- Цвета `&a`, HEX, градиенты и Adventure-компоненты.
- Сообщения, title, action bar и boss bar для Paper/Bukkit и Velocity.
- Async-планировщик для обеих платформ и sync-задачи для Bukkit.
- YAML-конфигурации на основе Java-классов и аннотаций.
- `ItemBuilder`, NBT, custom model data и item model.
- HikariCP для MySQL/MariaDB и Jedis для Redis.
- Адаптеры сторонних плагинов.
- Обёртки пакетов ProtocolLib.
- Утилиты для cooldown, времени, чисел, опыта, мира и переводов.

## Поддерживаемая основа

| Компонент | Фактическая конфигурация версии {{lib_version_plugin}} |
|---|---|
| Java | собственные классы: bytecode {{java_version_target}}; сборка: JDK {{java_version_build}} |
| Paper compile classpath | API `{{dependency_version_bukkit}}` |
| Paper | `paper-plugin.yml` объявляет API {{api_version_paper}} |
| Bukkit loader | `plugin.yml` объявляет API {{api_version_bukkit}} |
| Velocity | annotation processor `{{dependency_version_velocity}}`; compile API из `libs/Velocity.jar` |

Один `HLib.jar` содержит все три дескриптора. Paper descriptor требует API {{api_version_paper}}. Наличие Bukkit descriptor не гарантирует совместимость с каждой старой версией сервера: в коде используются Paper API и версионные интеграции. Java для запуска выбирайте по требованиям сервера и его плагинов.

## Перед использованием

1. HLib пока не публикуется в Maven-репозиторий. Для компиляции скачайте release JAR и подключите его через `compileOnly(files("libs/HLib.jar"))`.
2. HLib должен быть установлен отдельным плагином. Не встраивайте его в JAR своего плагина.
3. PlaceholderAPI необязателен: без него подстановка PAPI пропускается. Для предметов ItemsAdder/Nexo установите соответствующий плагин.
4. На Velocity передавайте в `Informer.send` объект `Component`: строковый путь использует plugin messaging, для которого в этом репозитории нет готового backend-обработчика.
5. На системах, отличных от Windows, HLib синхронно проверяет обновление при загрузке. Недоступный сервис останавливает платформу; найденное обновление заменяет текущий JAR и также приводит к остановке для перезапуска.

Документация генерируется из [docs/wiki](https://github.com/deHasher/HLib/tree/master/docs/wiki), параметры — из [gradle.properties](https://github.com/deHasher/HLib/blob/master/gradle.properties). Номер версии обновляется автоматически; изменения поведения API требуют правки соответствующей страницы в исходниках.