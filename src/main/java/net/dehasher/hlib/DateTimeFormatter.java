package net.dehasher.hlib;

import com.google.common.collect.Lists;
import net.dehasher.hlib.config.Info;
import net.dehasher.hlib.data.CompiledPattern;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DateTimeFormatter {
    private static final java.time.format.DateTimeFormatter HOURS_SECONDS_PATTERN = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");

    public static final long SECONDS_IN_MINUTE = 60;
    public static final long SECONDS_IN_HOUR   = 60  * SECONDS_IN_MINUTE;
    public static final long SECONDS_IN_DAY    = 24  * SECONDS_IN_HOUR;
    public static final long SECONDS_IN_WEEK   = 7   * SECONDS_IN_DAY;
    public static final long SECONDS_IN_MONTH  = 30  * SECONDS_IN_DAY;
    public static final long SECONDS_IN_YEAR   = 365 * SECONDS_IN_DAY;

    public static final Map<Character, Long> TIME_UNITS = new ConcurrentHashMap<>();
    static {
        TIME_UNITS.put('s', 1000L);
        TIME_UNITS.put('m', SECONDS_IN_MINUTE * 1000L);
        TIME_UNITS.put('h', SECONDS_IN_HOUR   * 1000L);
        TIME_UNITS.put('d', SECONDS_IN_DAY    * 1000L);
        TIME_UNITS.put('w', SECONDS_IN_WEEK   * 1000L);
        TIME_UNITS.put('y', SECONDS_IN_YEAR   * 1000L);
    }

    public static final SimpleDateFormat DATE      = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final String INFINITY            = "9999-01-01 00:00:00";
    private static final Calendar CALENDAR         = Calendar.getInstance();
    public static final Map<String,  String> DAYS  = new ConcurrentHashMap<>(){{
        Runnable runnable = () -> {
            try {
                CALENDAR.setTime(DATE.parse(LocalDate.now().toString()));
                CALENDAR.add(Calendar.DATE, -2);
                put(DATE.format(CALENDAR.getTime()), Info.DateTime.beforeYesterday);
                CALENDAR.add(Calendar.DATE, 1);
                put(DATE.format(CALENDAR.getTime()), Info.DateTime.yesterday);
                CALENDAR.add(Calendar.DATE, 1);
                put(DATE.format(CALENDAR.getTime()), Info.DateTime.today);
                CALENDAR.add(Calendar.DATE, 1);
                put(DATE.format(CALENDAR.getTime()), Info.DateTime.tomorrow);
                CALENDAR.add(Calendar.DATE, 1);
                put(DATE.format(CALENDAR.getTime()), Info.DateTime.afterTomorrow);
            } catch (Throwable ignored) {}
        };
        runnable.run();
        Scheduler.doAsyncRepeat(runnable, 60 * 20L, 60 * 20L);
    }};

    private static final Map<Integer, String> months = new ConcurrentHashMap<>(){{
        put(1,  Info.DateTime.january  ); put(2,  Info.DateTime.february);
        put(3,  Info.DateTime.march    ); put(4,  Info.DateTime.april   );
        put(5,  Info.DateTime.may      ); put(6,  Info.DateTime.june    );
        put(7,  Info.DateTime.july     ); put(8,  Info.DateTime.august  );
        put(9,  Info.DateTime.september); put(10, Info.DateTime.october );
        put(11, Info.DateTime.november ); put(12, Info.DateTime.december);
    }};

    // Осторожно с этой функцией, она довольно-таки сырая...
    public static String format(Date input, boolean withTime, boolean withYear, boolean withCustomDays, boolean color) {
        return format(input != null ? DATE_TIME.format(input) : DATE_TIME.format(new Date()), withTime, withYear, withCustomDays, color);
    }

    public static String format(String input, boolean withTime, boolean withYear, boolean withCustomDays, boolean color) {
        if (input == null || input.isEmpty()) return "";

        String[] all = input.split("\\.")[0].split(" ");
        String[] date = all[0].split("-");
        String[] time = all.length > 1 ? all[1].split(":") : new String[]{"00", "00", "00"};
        String month = months.get(Tools.parseInt(date[1]));
        if (Tools.parseInt(date[2]) < 10) date[2] = date[2].substring(1);
        String result = date[2] + " " + month;

        if (withCustomDays && DAYS.containsKey(all[0])) result = DAYS.get(all[0]);
        if (withTime) result += " в " + Tools.join(":", time[0], time[1]);
        if (withYear) result += " " + (color ? Colors.GRAY : "") + "(" + date[0] + ")";

        return result;
    }

    public static String format(Integer seconds) {
        if (seconds == null || seconds < 0) return Info.DateTime.undefined;
        return format((long) seconds);
    }

    public static String format(Long seconds) {
        if (seconds == null || seconds < 0) return Info.DateTime.undefined;

        long years   = seconds / SECONDS_IN_YEAR;
        seconds     %= SECONDS_IN_YEAR;
        long months  = seconds / SECONDS_IN_MONTH;
        seconds     %= SECONDS_IN_MONTH;
        long weeks   = seconds / SECONDS_IN_WEEK;
        seconds     %= SECONDS_IN_WEEK;
        long days    = seconds / SECONDS_IN_DAY;
        seconds     %= SECONDS_IN_DAY;
        long hours   = seconds / SECONDS_IN_HOUR;
        seconds     %= SECONDS_IN_HOUR;
        long minutes = seconds / SECONDS_IN_MINUTE;
        seconds     %= SECONDS_IN_MINUTE;

        List<String> parts = Lists.newArrayList();

        if (years   > 0) parts.add(years   + getCorrectForm(years,   Info.DateTime.year,    Info.DateTime.years1,   Info.DateTime.years2));
        if (months  > 0) parts.add(months  + getCorrectForm(months,  Info.DateTime.month,   Info.DateTime.months1,  Info.DateTime.months2));
        if (weeks   > 0) parts.add(weeks   + getCorrectForm(weeks,   Info.DateTime.week,    Info.DateTime.weeks1,   Info.DateTime.weeks2));
        if (days    > 0) parts.add(days    + getCorrectForm(days,    Info.DateTime.day,     Info.DateTime.days1,    Info.DateTime.days2));
        if (hours   > 0) parts.add(hours   + getCorrectForm(hours,   Info.DateTime.hour,    Info.DateTime.hours1,   Info.DateTime.hours2));
        if (minutes > 0) parts.add(minutes + getCorrectForm(minutes, Info.DateTime.minute,  Info.DateTime.minutes1, Info.DateTime.minutes2));
        if (seconds > 0 || parts.isEmpty()) parts.add(seconds + getCorrectForm(seconds, Info.DateTime.second,   Info.DateTime.seconds1, Info.DateTime.seconds2));

        return Tools.join(Info.DateTime.separator, parts);
    }

    private static String getCorrectForm(long number, String singular, String few, String many) {
        long lastDigit     = number % 10;
        long lastTwoDigits = number % 100;

        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) return many;

        if (lastDigit == 1) {
            return singular;
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            return few;
        } else {
            return many;
        }
    }

    public static int getSecondsUntil(String targetTime) {
        LocalTime current = LocalTime.parse(LocalTime.now().format(HOURS_SECONDS_PATTERN));
        LocalTime target = LocalTime.parse(targetTime);

        if (current.isAfter(target)) {
            long secondsToMidnight = Duration.between(current, LocalTime.MAX).getSeconds() + 1;
            long secondsFromMidnightToTarget = Duration.between(LocalTime.MIDNIGHT, target).getSeconds();
            return (int) (secondsToMidnight + secondsFromMidnightToTarget);
        } else {
            Duration duration = Duration.between(current, target);
            return (int) duration.getSeconds();
        }
    }

    public static Long getMillisFromCustomString(String input) {
        if (!CompiledPattern.CUSTOM_TIME_FORMAT.matches(input)) return null;

        char unit = input.charAt(input.length() - 1);
        String numberPart = input.substring(0, input.length() - 1);

        try {
            long value = Long.parseLong(numberPart);
            Long multiplier = TIME_UNITS.get(unit);

            if (multiplier == null) return null;

            return value * multiplier;
        } catch (Throwable t) {
            return null;
        }
    }
}