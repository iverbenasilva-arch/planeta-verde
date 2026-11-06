package com.example;

import com.example.plantgreencare.utils.CareScheduleCalculator;

import org.junit.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Testes unitários para o cálculo de agendamento de cuidados das plantas (Seção 15 e 42).
 */
public class CareScheduleCalculatorTest {

    @Test
    public void testCalculateNextDate_sevenDaysFrequency() {
        // Data base: 25/09/2026 às 10:00
        LocalDate baseDate = LocalDate.of(2026, 9, 25);
        long baseMillis = baseDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        long nextDateMillis = CareScheduleCalculator.calculateNextDate(baseMillis, 7);

        LocalDate nextDate = Instant.ofEpochMilli(nextDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        // 25/09 + 7 dias = 02/10
        assertEquals(LocalDate.of(2026, 10, 2), nextDate);
    }

    @Test
    public void testCalculateNextDate_oneDayFrequency() {
        LocalDate baseDate = LocalDate.of(2026, 9, 25);
        long baseMillis = baseDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        long nextDateMillis = CareScheduleCalculator.calculateNextDate(baseMillis, 1);

        LocalDate nextDate = Instant.ofEpochMilli(nextDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        assertEquals(LocalDate.of(2026, 9, 26), nextDate);
    }

    @Test
    public void testCalculateNextDate_zeroOrNegativeFrequencyDefaultsToOneDay() {
        LocalDate baseDate = LocalDate.of(2026, 9, 25);
        long baseMillis = baseDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        long nextDateMillis = CareScheduleCalculator.calculateNextDate(baseMillis, 0);

        LocalDate nextDate = Instant.ofEpochMilli(nextDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        assertEquals(LocalDate.of(2026, 9, 26), nextDate);
    }

    @Test
    public void testCareStatus_overdueDetection() {
        // Data no passado (3 dias atrás)
        LocalDate pastDate = LocalDate.now(ZoneId.systemDefault()).minusDays(3);
        long pastMillis = pastDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        CareScheduleCalculator.CareStatus status = CareScheduleCalculator.getCareStatus(pastMillis);
        assertEquals(CareScheduleCalculator.CareStatus.OVERDUE, status);

        long daysOverdue = CareScheduleCalculator.getDaysOverdue(pastMillis);
        assertEquals(3, daysOverdue);
    }

    @Test
    public void testCareStatus_todayDetection() {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        long todayMillis = today.atTime(8, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        CareScheduleCalculator.CareStatus status = CareScheduleCalculator.getCareStatus(todayMillis);
        assertEquals(CareScheduleCalculator.CareStatus.PENDING_TODAY, status);
        assertTrue(CareScheduleCalculator.isDueToday(todayMillis));
        assertFalse(CareScheduleCalculator.isDueTomorrow(todayMillis));
    }

    @Test
    public void testCareStatus_futureDateSetUpToDate() {
        LocalDate futureDate = LocalDate.now(ZoneId.systemDefault()).plusDays(4);
        long futureMillis = futureDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();

        CareScheduleCalculator.CareStatus status = CareScheduleCalculator.getCareStatus(futureMillis);
        assertEquals(CareScheduleCalculator.CareStatus.UP_TO_DATE, status);
        assertEquals(0, CareScheduleCalculator.getDaysOverdue(futureMillis));
        assertTrue(CareScheduleCalculator.isDueThisWeek(futureMillis));
    }
}
