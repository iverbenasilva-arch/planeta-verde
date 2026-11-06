package com.example.plantgreencare.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.plantgreencare.data.dao.CareRecordDao;
import com.example.plantgreencare.data.dao.CareScheduleDao;
import com.example.plantgreencare.data.dao.PlantDao;
import com.example.plantgreencare.data.database.AppDatabase;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.utils.CareScheduleCalculator;

import java.util.ArrayList;
import java.util.List;

public class PlantRepository {

    public interface OnPlantSavedCallback {
        void onSaved(long plantId);
    }

    private final PlantDao plantDao;
    private final CareScheduleDao careScheduleDao;
    private final CareRecordDao careRecordDao;

    public PlantRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.plantDao = db.plantDao();
        this.careScheduleDao = db.careScheduleDao();
        this.careRecordDao = db.careRecordDao();
    }

    public LiveData<List<PlantEntity>> getAllPlants() {
        return plantDao.getAllPlants();
    }

    public LiveData<PlantEntity> getPlantById(long id) {
        return plantDao.getPlantById(id);
    }

    public LiveData<List<PlantEntity>> searchPlants(String query) {
        return plantDao.searchPlants(query);
    }

    public LiveData<Integer> getPlantsCount() {
        return plantDao.getPlantsCount();
    }

    /**
     * Salva a planta e inicializa seus agendamentos de cuidados na mesma transação de persistência.
     */
    public void insertPlantWithSchedules(PlantEntity plant,
                                         int wateringFreq, boolean wateringEnabled,
                                         int fertilizingFreq, boolean fertilizingEnabled,
                                         int pruningFreq, boolean pruningEnabled,
                                         OnPlantSavedCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long plantId = plantDao.insert(plant);
            long now = System.currentTimeMillis();

            List<CareScheduleEntity> schedules = new ArrayList<>();

            if (wateringEnabled) {
                long nextDate = CareScheduleCalculator.calculateNextDate(now, wateringFreq);
                schedules.add(new CareScheduleEntity(plantId, 1, wateringFreq, nextDate, true, true));
            }

            if (fertilizingEnabled) {
                long nextDate = CareScheduleCalculator.calculateNextDate(now, fertilizingFreq);
                schedules.add(new CareScheduleEntity(plantId, 2, fertilizingFreq, nextDate, true, true));
            }

            if (pruningEnabled) {
                long nextDate = CareScheduleCalculator.calculateNextDate(now, pruningFreq);
                schedules.add(new CareScheduleEntity(plantId, 3, pruningFreq, nextDate, true, true));
            }

            if (!schedules.isEmpty()) {
                careScheduleDao.insertAll(schedules);
            }

            if (callback != null) {
                callback.onSaved(plantId);
            }
        });
    }

    /**
     * Insere múltiplas plantas (até 20) com seus agendamentos correspondentes.
     */
    public void insertMultiplePlantsWithSchedules(List<PlantEntity> plants,
                                                 int wateringFreq, boolean wateringEnabled,
                                                 int fertilizingFreq, boolean fertilizingEnabled,
                                                 int pruningFreq, boolean pruningEnabled,
                                                 Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long now = System.currentTimeMillis();
            for (PlantEntity plant : plants) {
                long plantId = plantDao.insert(plant);
                List<CareScheduleEntity> schedules = new ArrayList<>();

                if (wateringEnabled) {
                    long nextDate = CareScheduleCalculator.calculateNextDate(now, wateringFreq);
                    schedules.add(new CareScheduleEntity(plantId, 1, wateringFreq, nextDate, true, true));
                }

                if (fertilizingEnabled) {
                    long nextDate = CareScheduleCalculator.calculateNextDate(now, fertilizingFreq);
                    schedules.add(new CareScheduleEntity(plantId, 2, fertilizingFreq, nextDate, true, true));
                }

                if (pruningEnabled) {
                    long nextDate = CareScheduleCalculator.calculateNextDate(now, pruningFreq);
                    schedules.add(new CareScheduleEntity(plantId, 3, pruningFreq, nextDate, true, true));
                }

                if (!schedules.isEmpty()) {
                    careScheduleDao.insertAll(schedules);
                }
            }

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    /**
     * Atualiza dados da planta e sincroniza frequências e ativação dos cuidados.
     */
    public void updatePlantWithSchedules(PlantEntity plant,
                                         int wateringFreq, boolean wateringEnabled,
                                         int fertilizingFreq, boolean fertilizingEnabled,
                                         int pruningFreq, boolean pruningEnabled,
                                         Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            plant.setUpdatedAt(System.currentTimeMillis());
            plantDao.update(plant);

            syncSchedule(plant.getId(), 1, wateringFreq, wateringEnabled);
            syncSchedule(plant.getId(), 2, fertilizingFreq, fertilizingEnabled);
            syncSchedule(plant.getId(), 3, pruningFreq, pruningEnabled);

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    private void syncSchedule(long plantId, long careTypeId, int freq, boolean enabled) {
        CareScheduleEntity existing = careScheduleDao.getScheduleByPlantAndTypeSync(plantId, careTypeId);
        long now = System.currentTimeMillis();
        if (existing != null) {
            existing.setFrequency(freq);
            existing.setEnabled(enabled);
            existing.setNextDate(CareScheduleCalculator.calculateNextDate(now, freq));
            careScheduleDao.update(existing);
        } else if (enabled) {
            long nextDate = CareScheduleCalculator.calculateNextDate(now, freq);
            careScheduleDao.insert(new CareScheduleEntity(plantId, careTypeId, freq, nextDate, true, true));
        }
    }

    /**
     * Exclui a planta; o Room cuida automaticamente das tabelas filhas via CASCADE foreign keys.
     */
    public void deletePlant(PlantEntity plant, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            plantDao.delete(plant);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }

    public void deletePlantById(long plantId, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            plantDao.deleteById(plantId);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
