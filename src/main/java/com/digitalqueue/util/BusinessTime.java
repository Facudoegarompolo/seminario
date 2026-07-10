package com.digitalqueue.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public final class BusinessTime {

    public static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");
    public static final ZoneId STORAGE_ZONE = ZoneOffset.UTC;

    private BusinessTime() {
    }

    public static LocalDate today() {
        return LocalDate.now(BUSINESS_ZONE);
    }

    public static LocalDateTime nowStorage() {
        return LocalDateTime.now(STORAGE_ZONE);
    }

    public static LocalDateTime businessToStorage(LocalDateTime businessDateTime) {
        return businessDateTime
                .atZone(BUSINESS_ZONE)
                .withZoneSameInstant(STORAGE_ZONE)
                .toLocalDateTime();
    }

    public static LocalDateTime storageToBusiness(LocalDateTime storageDateTime) {
        if (storageDateTime == null) {
            return null;
        }

        return storageDateTime
                .atZone(STORAGE_ZONE)
                .withZoneSameInstant(BUSINESS_ZONE)
                .toLocalDateTime();
    }

    public static String storageToBusinessOffsetIso(LocalDateTime storageDateTime) {
        if (storageDateTime == null) {
            return "";
        }

        return storageDateTime
                .atZone(STORAGE_ZONE)
                .withZoneSameInstant(BUSINESS_ZONE)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
