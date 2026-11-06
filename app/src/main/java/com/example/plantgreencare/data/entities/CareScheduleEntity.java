package com.example.plantgreencare.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "care_schedules",
    foreignKeys = @ForeignKey(
        entity = PlantEntity.class,
        parentColumns = "id",
        childColumns = "plantId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("plantId"), @Index("nextDate")}
)
public class CareScheduleEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long plantId;
    private long careTypeId;
    private int frequency; // em dias
    private long nextDate; // timestamp em millis
    private boolean enabled;
    private boolean notificationEnabled;

    public CareScheduleEntity() {
        this.enabled = true;
        this.notificationEnabled = true;
    }

    @androidx.room.Ignore
    public CareScheduleEntity(long plantId, long careTypeId, int frequency, long nextDate,
                              boolean enabled, boolean notificationEnabled) {
        this.plantId = plantId;
        this.careTypeId = careTypeId;
        this.frequency = frequency;
        this.nextDate = nextDate;
        this.enabled = enabled;
        this.notificationEnabled = notificationEnabled;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPlantId() {
        return plantId;
    }

    public void setPlantId(long plantId) {
        this.plantId = plantId;
    }

    public long getCareTypeId() {
        return careTypeId;
    }

    public void setCareTypeId(long careTypeId) {
        this.careTypeId = careTypeId;
    }

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }

    public long getNextDate() {
        return nextDate;
    }

    public void setNextDate(long nextDate) {
        this.nextDate = nextDate;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isNotificationEnabled() {
        return notificationEnabled;
    }

    public void setNotificationEnabled(boolean notificationEnabled) {
        this.notificationEnabled = notificationEnabled;
    }
}
