package com.example.plantgreencare.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;

/**
 * Calculador de agendamento de cuidados e prazos.
 * Utiliza APIs modernas de data do Java (java.time / ZoneId) e mantém compatibilidade.
 */
public class CareScheduleCalculator {

    public enum CareStatus {
        UP_TO_DATE,
        PENDING_TODAY,
        OVERDUE,
        COMPLETED
    }

    /**
     * Calcula o próximo timestamp (em millis) a partir da data realizada e da frequência (em dias).
     * O horário do próximo cuidado é normalizado para o início da manhã (08:00).
     */
    public static long calculateNextDate(long lastPerformedMillis, int frequencyDays) {
        if (frequencyDays <= 0) {
            frequencyDays = 1;
        }

        LocalDate performedDate = Instant.ofEpochMilli(lastPerformedMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        LocalDate nextDate = performedDate.plusDays(frequencyDays);

        return nextDate.atTime(8, 0)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    /**
     * Retorna o status de um cuidado com base na data agendada.
     */
    public static CareStatus getCareStatus(long nextDateMillis) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate scheduledDate = Instant.ofEpochMilli(nextDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (scheduledDate.isBefore(today)) {
            return CareStatus.OVERDUE;
        } else if (scheduledDate.isEqual(today)) {
            return CareStatus.PENDING_TODAY;
        } else {
            return CareStatus.UP_TO_DATE;
        }
    }

    /**
     * Calcula quantos dias um cuidado está atrasado em relação à data atual.
     * Retorna 0 se estiver em dia ou for hoje.
     */
    public static long getDaysOverdue(long nextDateMillis) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate scheduledDate = Instant.ofEpochMilli(nextDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        if (scheduledDate.isBefore(today)) {
            return ChronoUnit.DAYS.between(scheduledDate, today);
        }
        return 0;
    }

    /**
     * Verifica se o agendamento é para hoje.
     */
    public static boolean isDueToday(long nextDateMillis) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate scheduledDate = Instant.ofEpochMilli(nextDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return scheduledDate.isEqual(today);
    }

    /**
     * Verifica se o agendamento é para amanhã.
     */
    public static boolean isDueTomorrow(long nextDateMillis) {
        LocalDate tomorrow = LocalDate.now(ZoneId.systemDefault()).plusDays(1);
        LocalDate scheduledDate = Instant.ofEpochMilli(nextDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
        return scheduledDate.isEqual(tomorrow);
    }

    /**
     * Verifica se o agendamento é para os próximos 7 dias.
     */
    public static boolean isDueThisWeek(long nextDateMillis) {
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate weekEnd = today.plusDays(7);
        LocalDate scheduledDate = Instant.ofEpochMilli(nextDateMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        return !scheduledDate.isBefore(today) && !scheduledDate.isAfter(weekEnd);
    }

    /**
     * Retorna o início do dia atual em milissegundos.
     */
    public static long getStartOfTodayMillis() {
        return LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }

    /**
     * Retorna o final do dia atual em milissegundos.
     */
    public static long getEndOfTodayMillis() {
        return LocalDate.now(ZoneId.systemDefault())
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .minusNanos(1000)
                .toInstant()
                .toEpochMilli();
    }

    /**
     * Retorna o final da semana (7 dias) em milissegundos.
     */
    public static long getEndOfWeekMillis() {
        return LocalDate.now(ZoneId.systemDefault())
                .plusDays(7)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
    }
}
