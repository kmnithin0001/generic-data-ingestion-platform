package com.intentwise.ingestion.domain.service;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ProgressCalculatorTest {

    private final ProgressCalculator calculator = new DefaultProgressCalculator();

    @Test
    void shouldCalculatePercentageSuccessfully() {
        double pct = calculator.calculatePercentage(50, 100);
        assertEquals(50.0, pct);

        double pctZero = calculator.calculatePercentage(50, null);
        assertEquals(0.0, pctZero);

        double pctCap = calculator.calculatePercentage(120, 100);
        assertEquals(100.0, pctCap);
    }

    @Test
    void shouldCalculateEstimatedCompletionSuccessfully() {
        LocalDateTime startTime = LocalDateTime.now().minusMinutes(10);
        LocalDateTime now = LocalDateTime.now();

        // 10 minutes for 50 records means 20 minutes total for 100 records
        LocalDateTime est = calculator.calculateEstimatedCompletion(startTime, 50, 100, now);
        assertNotNull(est);
        assertTrue(est.isAfter(now));

        // Unknown total records
        LocalDateTime estNull = calculator.calculateEstimatedCompletion(startTime, 50, null, now);
        assertNull(estNull);
    }
}
