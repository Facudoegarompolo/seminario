package com.digitalqueue.util;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessTimeTest {

    @Test
    void convierteEntreUtcDeAlmacenamientoYHorarioArgentina() {
        LocalDateTime storageDateTime = LocalDateTime.of(2026, 7, 8, 3, 30);
        LocalDateTime businessDateTime = LocalDateTime.of(2026, 7, 8, 0, 30);

        assertEquals(businessDateTime, BusinessTime.storageToBusiness(storageDateTime));
        assertEquals(storageDateTime, BusinessTime.businessToStorage(businessDateTime));
        assertEquals("2026-07-08T00:30:00-03:00", BusinessTime.storageToBusinessOffsetIso(storageDateTime));
    }
}
