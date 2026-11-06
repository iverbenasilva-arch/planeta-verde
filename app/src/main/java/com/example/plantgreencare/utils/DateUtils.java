package com.example.plantgreencare.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DateUtils {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));

    public static String formatDate(long millis) {
        if (millis <= 0) return "-";
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .format(DATE_FORMATTER);
    }

    public static String formatDateTime(long millis) {
        if (millis <= 0) return "-";
        return Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime()
                .format(DATE_TIME_FORMATTER);
    }

    public static String formatRelativeOrDate(long millis) {
        if (millis <= 0) return "-";
        LocalDate date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        LocalDate today = LocalDate.now(ZoneId.systemDefault());

        if (date.isEqual(today)) {
            return "Hoje";
        } else if (date.isEqual(today.plusDays(1))) {
            return "Amanhã";
        } else if (date.isEqual(today.minusDays(1))) {
            return "Ontem";
        } else {
            return date.format(DATE_FORMATTER);
        }
    }

    public static String getTodayFormatted() {
        return LocalDate.now(ZoneId.systemDefault()).format(DATE_FORMATTER);
    }

    public static long parseDate(String dateString) {
        try {
            LocalDate localDate = LocalDate.parse(dateString, DATE_FORMATTER);
            return localDate.atTime(8, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }
}
