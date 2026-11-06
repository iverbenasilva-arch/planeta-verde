package com.example.plantgreencare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.plantgreencare.data.entities.AppSettingsEntity;
import com.example.plantgreencare.data.repository.SettingsRepository;
import com.example.plantgreencare.notifications.DailyCareWorker;

public class SettingsViewModel extends AndroidViewModel {

    private final SettingsRepository settingsRepository;
    private final LiveData<AppSettingsEntity> settingsLive;

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        settingsRepository = new SettingsRepository(application);
        settingsLive = settingsRepository.getSettings();
    }

    public LiveData<AppSettingsEntity> getSettings() {
        return settingsLive;
    }

    public void setDarkMode(int mode) {
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences(com.example.PlantCareApplication.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit().putInt(com.example.PlantCareApplication.KEY_DARK_MODE, mode).apply();

        AppSettingsEntity current = settingsLive.getValue();
        final AppSettingsEntity settings = (current != null) ? current : new AppSettingsEntity();
        settings.setDarkMode(mode);
        settingsRepository.updateSettings(settings, () -> applyTheme(mode));
    }

    public static void applyTheme(int mode) {
        if (mode == 1) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else if (mode == 2) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
    }

    public void setNotificationsEnabled(boolean enabled) {
        AppSettingsEntity current = settingsLive.getValue();
        final AppSettingsEntity settings = (current != null) ? current : new AppSettingsEntity();
        settings.setNotificationsEnabled(enabled);
        settingsRepository.updateSettings(settings, () -> {
            if (enabled) {
                DailyCareWorker.scheduleDailyCareReminder(
                        getApplication(),
                        settings.getNotificationHour(),
                        settings.getNotificationMinute()
                );
            } else {
                DailyCareWorker.cancelReminder(getApplication());
            }
        });
    }

    public void setReminderTime(int hour, int minute) {
        AppSettingsEntity current = settingsLive.getValue();
        final AppSettingsEntity settings = (current != null) ? current : new AppSettingsEntity();
        settings.setNotificationHour(hour);
        settings.setNotificationMinute(minute);
        settingsRepository.updateSettings(settings, () -> {
            if (settings.isNotificationsEnabled()) {
                DailyCareWorker.scheduleDailyCareReminder(getApplication(), hour, minute);
            }
        });
    }

    public void setHighContrast(boolean enabled) {
        AppSettingsEntity current = settingsLive.getValue();
        final AppSettingsEntity settings = (current != null) ? current : new AppSettingsEntity();
        settings.setHighContrast(enabled);
        settingsRepository.updateSettings(settings, null);
    }

    public void setVibration(boolean enabled) {
        AppSettingsEntity current = settingsLive.getValue();
        final AppSettingsEntity settings = (current != null) ? current : new AppSettingsEntity();
        settings.setReducedMotion(!enabled);
        settingsRepository.updateSettings(settings, null);
    }

    public void setOnboardingCompleted() {
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences(com.example.PlantCareApplication.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit().putBoolean(com.example.PlantCareApplication.KEY_ONBOARDING_COMPLETED, true).apply();

        AppSettingsEntity current = settingsLive.getValue();
        final AppSettingsEntity settings = (current != null) ? current : new AppSettingsEntity();
        settings.setOnboardingCompleted(true);
        settingsRepository.updateSettings(settings, null);
    }

    public void exportData(SettingsRepository.OnBackupCallback callback) {
        settingsRepository.exportDataToJson(callback);
    }

    public void importData(String json, SettingsRepository.OnRestoreCallback callback) {
        settingsRepository.importDataFromJson(json, callback);
    }
}
