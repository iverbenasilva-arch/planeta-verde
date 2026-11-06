package com.example.plantgreencare.model;

import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.utils.CareScheduleCalculator;

public class PlantCardItem {
    private final PlantEntity plant;
    private final String nextCareText;
    private final String lastCareText;
    private final CareScheduleCalculator.CareStatus status;

    public PlantCardItem(PlantEntity plant, String nextCareText, String lastCareText,
                         CareScheduleCalculator.CareStatus status) {
        this.plant = plant;
        this.nextCareText = nextCareText;
        this.lastCareText = lastCareText;
        this.status = status;
    }

    public PlantEntity getPlant() {
        return plant;
    }

    public String getNextCareText() {
        return nextCareText;
    }

    public String getLastCareText() {
        return lastCareText;
    }

    public CareScheduleCalculator.CareStatus getStatus() {
        return status;
    }
}
