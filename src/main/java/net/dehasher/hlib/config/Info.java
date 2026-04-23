package net.dehasher.hlib.config;

import com.google.common.collect.Lists;
import net.dehasher.hlib.data.Platform;
import net.dehasher.hlib.file.Configuration;
import net.dehasher.hlib.file.Annotations.Final;
import net.dehasher.hlib.file.Annotations.Key;
import net.dehasher.hlib.file.Annotations.Comment;
import net.dehasher.hlib.file.ConfigurationSection;
import java.util.List;

public class Info extends Configuration {
    @Comment("Версия конфигурации, не изменять.")
    @Key("version")
    @Final
    public static String version = "${lib_version_info}";

    @Comment({
            "",
            "Имя консоли.",
            "Если менять - то сразу на всех серверах.",
    })
    @Key("console-name")
    public static String consoleName = "Консоль";

    @Comment({
            "",
            "Название сервера и его идентификатор.",
            "Используется для различных переменных по типу %hcore_online_survival-1%.",
            "Чтобы работали зачарования высокого уровня, укажите name - anarcy или grief.",
    })
    @Key("server")
    public Server server = new Server();
    public static class Server implements ConfigurationSection {
        @Key("id")
        public static int id = 1;
        @Key("name")
        public static String name = Platform.get() == Platform.BUKKIT ? "survival" : "proxy";
    }

    @Comment({
            "",
            "Кастомные заполнители формата {prefix} и {site_url}.",
            "Доступны везде.",
    })
    @Key("placeholders")
    public Placeholders placeholders = new Placeholders();
    public static class Placeholders implements ConfigurationSection {
        @Key("enabled")
        public static boolean enabled = true;

        @Key("prefix")
        public static String prefix = "&9HCore &7> &f";

        @Key("site_url")
        public static String site_url = "${author_lower}.net";
    }

    @Comment({
            "",
            "Администрация у которой будет полный доступ.",
            "Возможности:",
            "Доступ ко всем возможностям этого плагина и других плагинов, которые используют этот плагин.",
            "Возможность писать команды через # от имени консоли. Пример: #gm 1 ${author}",
    })
    @Key("admins")
    public static List<String> admins = Lists.newArrayList(List.of("${author}", "${author_twink}"));

    @Comment({
            "",
            "Фейковый онлайн сервера.",
            "Если значение 400% и текущий онлайн 5 игроков, то будет отображаться",
            "онлайн 25 игроков через заполнитель по типу %hcore_online_survival-1_fake%.",
            "",
            "need-randomize - Прибавляет случайным образом к онлайну от 1 до 9 игроков.",
    })
    @Key("fake-online")
    public FakeOnline fakeOnline = new FakeOnline();
    public static class FakeOnline implements ConfigurationSection {
        @Key("need-randomize")
        public static boolean needRandomize = true;
        @Key("percent")
        public static String percent = "50%";
    }

    @Comment({
            "",
            "Отправка callback уведомлений о событиях на API.",
            "Заполнитель {name} заменяется на server.name.",
            "Заполнитель {id} заменяется на server.id.",
            "POST[\"msg\"] - Тело сообщения.",
            "POST[\"type\"] - Тип события.",
            "",
            "Чтобы отключить уведомления - ничего не указывайте в сообщении.",
    })
    @Key("api-notifications")
    public ApiNotifications apiNotifications = new ApiNotifications();
    public static class ApiNotifications implements ConfigurationSection {
        @Comment({
                "",
                "Ссылка на сервер-обработчик.",
        })
        @Key("url")
        public static String url = "${url_site_api}/alert";

        @Key("messages")
        public Messages messages = new Messages();
        public static class Messages implements ConfigurationSection {
            @Comment({
                    "",
                    "Включение сервера.",
            })
            @Key("on-enable")
            public static String onEnable = "Сервер {name} #{id} запущен.";
            @Comment({
                    "",
                    "Выключение сервера.",
            })
            @Key("on-disable")
            public static String onDisable = "Сервер {name} #{id} остановлен.";
            @Comment({
                    "",
                    "Ошибка в консоли.",
            })
            @Key("on-thrown")
            public static String onThrown = "Ошибка на сервере {name} #{id}:```{error}```";
            @Comment({
                    "",
                    "Получение игроком запрещённого права.",
                    "Если игрок получил /op [ник], то запрещённое право будет: 'minecraft.op'.",
            })
            @Key("on-dangerous-permissions")
            public static String onDangerousPermissions = "Игрок {player} попытался получить опасное право {permission} на сервере {name} #{id} и был заблокирован!";
            @Comment({
                    "",
                    "Если был включён вайтлист при включении сервера.",
            })
            @Key("is-whitelist-enabled")
            public static String isWhitelistEnabled = "Сервер {name} #{id} находится на тех. работах.";
        }
    }

    @Comment({
            "",
            "Форматы времени в винительном падеже.",
            "Форматы месяцев в родительном падеже.",
            "Форматы наречий само собой не измеряются по падежам.",
    })
    @Key("date-time")
    public DateTime dateTime = new DateTime();
    public static class DateTime implements ConfigurationSection {
        @Key("before-yesterday")
        public static String beforeYesterday = "Позавчера";
        @Key("yesterday")
        public static String yesterday = "Вчера";
        @Key("today")
        public static String today = "Сегодня";
        @Key("tomorrow")
        public static String tomorrow = "Завтра";
        @Key("after-tomorrow")
        public static String afterTomorrow = "Послезавтра";
        @Key("january")
        public static String january = "января";
        @Key("february")
        public static String february = "февраля";
        @Key("march")
        public static String march = "марта";
        @Key("april")
        public static String april = "апреля";
        @Key("may")
        public static String may = "мая";
        @Key("june")
        public static String june = "июня";
        @Key("july")
        public static String july = "июля";
        @Key("august")
        public static String august = "августа";
        @Key("september")
        public static String september = "сентября";
        @Key("october")
        public static String october = "октября";
        @Key("november")
        public static String november = "ноября";
        @Key("december")
        public static String december = "декабря";
        @Key("year")
        public static String year = " год";
        @Key("years-1")
        public static String years1 = " года";
        @Key("years-2")
        public static String years2 = " лет";
        @Key("month")
        public static String month = " месяц";
        @Key("months-1")
        public static String months1 = " месяца";
        @Key("months-2")
        public static String months2 = " месяцев";
        @Key("week")
        public static String week = " неделю";
        @Key("weeks-1")
        public static String weeks1 = " недели";
        @Key("weeks-2")
        public static String weeks2 = " недель";
        @Key("day")
        public static String day = " день";
        @Key("days-1")
        public static String days1 = " дня";
        @Key("days-2")
        public static String days2 = " дней";
        @Key("hour")
        public static String hour = " час";
        @Key("hours-1")
        public static String hours1 = " часа";
        @Key("hours-2")
        public static String hours2 = " часов";
        @Key("minute")
        public static String minute = " минуту";
        @Key("minutes-1")
        public static String minutes1 = " минуты";
        @Key("minutes-2")
        public static String minutes2 = " минут";
        @Key("second")
        public static String second = " секунду";
        @Key("seconds-1")
        public static String seconds1 = " секунды";
        @Key("seconds-2")
        public static String seconds2 = " секунд";
        @Key("separator")
        public static String separator = ", ";
        @Key("undefined")
        public static String undefined = "-";
    }
}