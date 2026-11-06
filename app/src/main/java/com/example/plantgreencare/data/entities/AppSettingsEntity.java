package com.example.plantgreencare.data.entities;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "app_settings")
public class AppSettingsEntity {

    @PrimaryKey
    private int id = 1;

    private boolean notificationsEnabled = true;
    private int notificationHour = 8;
    private int notificationMinute = 0;
    private int darkMode = 0; // 0 = Seguir Sistema, 1 = Claro, 2 = Escuro
    private float fontScale = 1.0f;
    private boolean highContrast = false;
    private boolean reducedMotion = false;
    private boolean onboardingCompleted = false;

    public AppSettingsEntity() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    public int getNotificationHour() {
        return notificationHour;
    }

    public void setNotificationHour(int notificationHour) {
        this.notificationHour = notificationHour;
    }

    public int getNotificationMinute() {
        return notificationMinute;
    }

    public void setNotificationMinute(int notificationMinute) {
        this.notificationMinute = notificationMinute;
    }

    public int getDarkMode() {
        return darkMode;
    }

    public void setDarkMode(int darkMode) {
        this.darkMode = darkMode;
    }

    public float getFontScale() {
        return fontScale;
    }

    public void setFontScale(float fontScale) {
        this.fontScale = fontScale;
    }

    public boolean isHighContrast() {
        return highContrast;
    }

    public void setHighContrast(boolean highContrast) {
        this.highContrast = highContrast;
    }

    public boolean isReducedMotion() {
        return reducedMotion;
    }

    public void setReducedMotion(boolean reducedMotion) {
        this.reducedMotion = reducedMotion;
    }

    public boolean isOnboardingCompleted() {
        return onboardingCompleted;
    }

    public void setOnboardingCompleted(boolean onboardingCompleted) {
        this.onboardingCompleted = onboardingCompleted;
    }
}
