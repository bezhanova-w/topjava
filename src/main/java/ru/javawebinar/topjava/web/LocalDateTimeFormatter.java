package ru.javawebinar.topjava.web;

import org.springframework.format.Formatter;
import ru.javawebinar.topjava.util.DateTimeUtil;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public final class LocalDateTimeFormatter {

    public static final class LocalDateFormatter implements Formatter<LocalDate> {
        @Override
        public LocalDate parse(String formatted, Locale locale) throws ParseException {
            return DateTimeUtil.parseLocalDate(formatted);
        }

        @Override
        public String print(LocalDate localDate, Locale locale) {
            return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
        }
    }

    public static final class LocalTimeFormatter implements Formatter<LocalTime> {
        @Override
        public LocalTime parse(String formatted, Locale locale) throws ParseException {
            return DateTimeUtil.parseLocalTime(formatted);
        }

        @Override
        public String print(LocalTime localTime, Locale locale) {
            return localTime.format(DateTimeFormatter.ISO_LOCAL_TIME);
        }
    }

}
