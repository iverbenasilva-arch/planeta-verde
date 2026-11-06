package com.example.plantgreencare.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.plantgreencare.data.entities.CareRecordEntity;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.data.repository.CareRepository;
import com.example.plantgreencare.data.repository.PlantRepository;

import java.util.List;

public class PlantDetailsViewModel extends AndroidViewModel {

    private final PlantRepository plantRepository;
    private final CareRepository careRepository;

    public PlantDetailsViewModel(@NonNull Application application) {
        super(application);
        plantRepository = new PlantRepository(application);
        careRepository = new CareRepository(application);
    }

    public LiveData<PlantEntity> getPlant(long plantId) {
        return plantRepository.getPlantById(plantId);
    }

    public LiveData<List<CareScheduleEntity>> getSchedules(long plantId) {
        return careRepository.getSchedulesByPlant(plantId);
    }

    public LiveData<List<CareRecordEntity>> getCareHistory(long plantId) {
        return careRepository.getRecordsByPlant(plantId);
    }

    public void deletePlant(PlantEntity plant, Runnable onComplete) {
        plantRepository.deletePlant(plant, onComplete);
    }

    public void registerCare(long plantId, long careTypeId, long performedAt, String notes, Runnable onComplete) {
        careRepository.registerCare(plantId, careTypeId, performedAt, notes, onComplete);
    }
}
