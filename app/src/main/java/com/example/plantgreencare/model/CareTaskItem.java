package com.example.plantgreencare.model;

import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;

public class CareTaskItem {
    private final PlantEntity plant;
    private final CareScheduleEntity schedule;
    private final String careTypeName;
    private final String statusText;
    private final boolean isOverdue;
    private final long daysOverdue;

    public CareTaskItem(PlantEntity plant, CareScheduleEntity schedule, String careTypeName,
                        String statusText, boolean isOverdue, long daysOverdue) {
        this.plant = plant;
        this.schedule = schedule;
        this.careTypeName = careTypeName;
        this.statusText = statusText;
        this.isOverdue = isOverdue;
        this.daysOverdue = daysOverdue;
    }

    public PlantEntity getPlant() {
        return plant;
    }

    public CareScheduleEntity getSchedule() {
        return schedule;
    }

    public String getCareTypeName() {
        return careTypeName;
    }

    public String getStatusText() {
        return statusText;
    }

    public boolean isOverdue() {
        return isOverdue;
    }

    public long getDaysOverdue() {
        return daysOverdue;
    }
}
