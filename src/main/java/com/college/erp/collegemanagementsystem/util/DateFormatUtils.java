package com.college.erp.collegemanagementsystem.util;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

public final class DateFormatUtils {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private DateFormatUtils() {
    }

    public static String formatDate(LocalDate date) {
        return date == null ? null : DATE_FORMAT.format(date);
    }

    public static String formatDateTime(OffsetDateTime dateTime) {
        return dateTime == null ? null : DATE_TIME_FORMAT.format(dateTime);
    }
}
