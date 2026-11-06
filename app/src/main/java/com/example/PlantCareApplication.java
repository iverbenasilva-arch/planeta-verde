package com.example;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;

import com.example.plantgreencare.data.database.AppDatabase;
import com.example.plantgreencare.data.entities.AppSettingsEntity;
import com.example.plantgreencare.notifications.DailyCareWorker;
import com.example.plantgreencare.notifications.PlantCareNotificationManager;

public class PlantCareApplication extends Application {

    public static final String PREFS_NAME = "plant_green_care_prefs";
    public static final String KEY_ONBOARDING_COMPLETED = "onboarding_completed";
    public static final String KEY_DARK_MODE = "dark_mode";

    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Carregar tema imediatamente de SharedPreferences antes de inflar qualquer Activity
        // Isso evita que a Activity seja recriada durante a abertura do app
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int darkMode = prefs.getInt(KEY_DARK_MODE, 0);
        if (darkMode == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (darkMode == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }

        // 2. Inicializar canais de notificação e agendamentos em background sem travar abertura
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                new PlantCareNotificationManager(this);

                AppDatabase db = AppDatabase.getInstance(this);
                AppSettingsEntity settings = db.settingsDao().getSettingsSync();
                if (settings != null) {
                    if (settings.isNotificationsEnabled()) {
                        DailyCareWorker.scheduleDailyCareReminder(
                                this,
                                settings.getNotificationHour(),
                                settings.getNotificationMinute()
                        );
                    }
                } else {
                    DailyCareWorker.scheduleDailyCareReminder(this, 8, 0);
                }
            } catch (Exception ignored) {}
        });
    }
}
