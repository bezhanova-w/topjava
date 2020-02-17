package ru.javawebinar.topjava.util;

import org.springframework.util.StringUtils;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static <T extends Comparable<T>> boolean isBetweenInclusive(T ldt, T start, T end) {
        return (start == null || ldt.compareTo(start) >= 0) && (end == null || ldt.compareTo(end) <= 0);
    }

    /* оставлено для истории
    public static <T> T parse(String value, Class<T> clazz) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (clazz == LocalDate.class) {
            return clazz.cast(LocalDate.parse(value));
        } else if (clazz == LocalTime.class) {
            return clazz.cast(LocalTime.parse(value));
        }
        return null;
    }
     */

    public static LocalDate parseLocalDate(String value) {
        return StringUtils.isEmpty(value) ? null : LocalDate.parse(value);
    }

    public static LocalTime parseLocalTime(String value) {
        return StringUtils.isEmpty(value) ? null : LocalTime.parse(value);
    }

    public static String toString(LocalDateTime ldt) {
        return ldt == null ? "" : ldt.format(DATE_TIME_FORMATTER);
    }
}

