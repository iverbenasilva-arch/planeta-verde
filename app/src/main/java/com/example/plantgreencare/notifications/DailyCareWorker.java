package com.example.plantgreencare.notifications;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.plantgreencare.data.database.AppDatabase;
import com.example.plantgreencare.data.entities.AppSettingsEntity;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.utils.CareScheduleCalculator;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DailyCareWorker extends Worker {

    private static final String WORK_NAME = "DailyPlantCareReminderWork";

    public DailyCareWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);

        AppSettingsEntity settings = db.settingsDao().getSettingsSync();
        if (settings != null && !settings.isNotificationsEnabled()) {
            return Result.success();
        }

        long start = CareScheduleCalculator.getStartOfTodayMillis();
        long end = CareScheduleCalculator.getEndOfTodayMillis();

        List<CareScheduleEntity> todaySchedules = db.careScheduleDao().getTodaySchedulesSync(start, end);
        List<CareScheduleEntity> overdueSchedules = db.careScheduleDao().getOverdueSchedulesSync(start);

        int totalPending = todaySchedules.size() + overdueSchedules.size();

        if (totalPending > 0) {
            PlantCareNotificationManager notificationManager = new PlantCareNotificationManager(context);
            String title = "Hora de cuidar da sua planta 🌱";
            String message;

            if (!todaySchedules.isEmpty()) {
                CareScheduleEntity first = todaySchedules.get(0);
                PlantEntity plant = db.plantDao().getPlantByIdSync(first.getPlantId());
                String plantName = plant != null ? plant.getName() : "Sua planta";
                message = "A " + plantName + " precisa de cuidados hoje.";
                if (totalPending > 1) {
                    message += " Mais " + (totalPending - 1) + " cuidado(s) pendente(s).";
                }
            } else {
                CareScheduleEntity firstOverdue = overdueSchedules.get(0);
                PlantEntity plant = db.plantDao().getPlantByIdSync(firstOverdue.getPlantId());
                String plantName = plant != null ? plant.getName() : "Sua planta";
                message = "Você tem cuidados atrasados para " + plantName + "!";
            }

            notificationManager.showCareNotification(1001, title, message);
        }

        return Result.success();
    }

    /**
     * Agenda ou reagenda o lembrete diário usando WorkManager no horário configurado.
     */
    public static void scheduleDailyCareReminder(Context context, int targetHour, int targetMinute) {
        LocalDateTime now = LocalDateTime.now(ZoneId.systemDefault());
        LocalDateTime targetTime = now.with(LocalTime.of(targetHour, targetMinute, 0));

        if (targetTime.isBefore(now)) {
            targetTime = targetTime.plusDays(1);
        }

        long initialDelayMinutes = Duration.between(now, targetTime).toMinutes();
        if (initialDelayMinutes < 0) initialDelayMinutes = 0;

        Constraints constraints = new Constraints.Builder()
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                DailyCareWorker.class,
                24, TimeUnit.HOURS
        )
        .setInitialDelay(initialDelayMinutes, TimeUnit.MINUTES)
        .setConstraints(constraints)
        .build();

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
        );
    }

    public static void cancelReminder(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }
}
