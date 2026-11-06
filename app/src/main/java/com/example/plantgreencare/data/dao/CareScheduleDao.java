package com.example.plantgreencare.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.plantgreencare.data.entities.CareScheduleEntity;

import java.util.List;

@Dao
public interface CareScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CareScheduleEntity schedule);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CareScheduleEntity> schedules);

    @Update
    void update(CareScheduleEntity schedule);

    @Delete
    void delete(CareScheduleEntity schedule);

    @Query("DELETE FROM care_schedules WHERE plantId = :plantId")
    void deleteByPlantId(long plantId);

    @Query("SELECT * FROM care_schedules WHERE plantId = :plantId ORDER BY nextDate ASC")
    LiveData<List<CareScheduleEntity>> getSchedulesByPlant(long plantId);

    @Query("SELECT * FROM care_schedules WHERE plantId = :plantId ORDER BY nextDate ASC")
    List<CareScheduleEntity> getSchedulesByPlantSync(long plantId);

    @Query("SELECT * FROM care_schedules WHERE plantId = :plantId AND careTypeId = :careTypeId LIMIT 1")
    CareScheduleEntity getScheduleByPlantAndTypeSync(long plantId, long careTypeId);

    @Query("SELECT * FROM care_schedules WHERE enabled = 1 ORDER BY nextDate ASC")
    LiveData<List<CareScheduleEntity>> getAllEnabledSchedules();

    @Query("SELECT * FROM care_schedules WHERE enabled = 1 AND nextDate BETWEEN :startOfDay AND :endOfDay ORDER BY nextDate ASC")
    LiveData<List<CareScheduleEntity>> getTodaySchedules(long startOfDay, long endOfDay);

    @Query("SELECT * FROM care_schedules WHERE enabled = 1 AND nextDate BETWEEN :startOfDay AND :endOfDay ORDER BY nextDate ASC")
    List<CareScheduleEntity> getTodaySchedulesSync(long startOfDay, long endOfDay);

    @Query("SELECT * FROM care_schedules WHERE enabled = 1 AND nextDate < :startOfDay ORDER BY nextDate ASC")
    LiveData<List<CareScheduleEntity>> getOverdueSchedules(long startOfDay);

    @Query("SELECT * FROM care_schedules WHERE enabled = 1 AND nextDate < :startOfDay ORDER BY nextDate ASC")
    List<CareScheduleEntity> getOverdueSchedulesSync(long startOfDay);

    @Query("SELECT * FROM care_schedules WHERE enabled = 1 AND nextDate BETWEEN :startOfDay AND :endOfWeek ORDER BY nextDate ASC")
    LiveData<List<CareScheduleEntity>> getUpcomingSchedules(long startOfDay, long endOfWeek);

    @Query("SELECT * FROM care_schedules WHERE enabled = 1 ORDER BY nextDate ASC LIMIT 1")
    LiveData<CareScheduleEntity> getNextSchedule();

    @Query("SELECT * FROM care_schedules WHERE enabled = 1 ORDER BY nextDate ASC LIMIT 1")
    CareScheduleEntity getNextScheduleSync();
}
