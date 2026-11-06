package com.example.plantgreencare.data.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.plantgreencare.data.dao.CareRecordDao;
import com.example.plantgreencare.data.dao.CareScheduleDao;
import com.example.plantgreencare.data.dao.PlantDao;
import com.example.plantgreencare.data.database.AppDatabase;
import com.example.plantgreencare.data.entities.CareRecordEntity;
import com.example.plantgreencare.data.entities.CareScheduleEntity;
import com.example.plantgreencare.data.entities.PlantEntity;
import com.example.plantgreencare.utils.CareScheduleCalculator;

import java.util.List;

public class CareRepository {

    private final CareScheduleDao careScheduleDao;
    private final CareRecordDao careRecordDao;
    private final PlantDao plantDao;

    public CareRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        this.careScheduleDao = db.careScheduleDao();
        this.careRecordDao = db.careRecordDao();
        this.plantDao = db.plantDao();
    }

    public LiveData<List<CareScheduleEntity>> getSchedulesByPlant(long plantId) {
        return careScheduleDao.getSchedulesByPlant(plantId);
    }

    public LiveData<List<CareRecordEntity>> getRecordsByPlant(long plantId) {
        return careRecordDao.getRecordsByPlant(plantId);
    }

    public LiveData<CareRecordEntity> getLastRecordForPlant(long plantId) {
        return careRecordDao.getLastRecordForPlant(plantId);
    }

    public LiveData<List<CareScheduleEntity>> getTodaySchedules() {
        long start = CareScheduleCalculator.getStartOfTodayMillis();
        long end = CareScheduleCalculator.getEndOfTodayMillis();
        return careScheduleDao.getTodaySchedules(start, end);
    }

    public LiveData<List<CareScheduleEntity>> getOverdueSchedules() {
        long start = CareScheduleCalculator.getStartOfTodayMillis();
        return careScheduleDao.getOverdueSchedules(start);
    }

    public LiveData<List<CareScheduleEntity>> getUpcomingSchedules() {
        long start = CareScheduleCalculator.getStartOfTodayMillis();
        long end = CareScheduleCalculator.getEndOfWeekMillis();
        return careScheduleDao.getUpcomingSchedules(start, end);
    }

    public LiveData<List<CareScheduleEntity>> getAllEnabledSchedules() {
        return careScheduleDao.getAllEnabledSchedules();
    }

    public LiveData<CareScheduleEntity> getNextSchedule() {
        return careScheduleDao.getNextSchedule();
    }

    public PlantEntity getPlantByIdSync(long plantId) {
        return plantDao.getPlantByIdSync(plantId);
    }

    /**
     * Regra de Registro de Cuidado:
     * 1. Salva no histórico (CareRecordEntity).
     * 2. Recalcula a próxima ocorrência para a frequência configurada.
     * 3. Atualiza o agendamento ativo.
     */
    public void registerCare(long plantId, long careTypeId, long performedAt, String notes, Runnable onComplete) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            CareRecordEntity record = new CareRecordEntity(plantId, careTypeId, performedAt, notes);
            careRecordDao.insert(record);

            CareScheduleEntity schedule = careScheduleDao.getScheduleByPlantAndTypeSync(plantId, careTypeId);
            if (schedule != null) {
                long nextDate = CareScheduleCalculator.calculateNextDate(performedAt, schedule.getFrequency());
                schedule.setNextDate(nextDate);
                careScheduleDao.update(schedule);
            }

            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
