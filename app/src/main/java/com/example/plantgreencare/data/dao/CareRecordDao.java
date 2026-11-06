package com.example.plantgreencare.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.plantgreencare.data.entities.CareRecordEntity;

import java.util.List;

@Dao
public interface CareRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(CareRecordEntity record);

    @Delete
    void delete(CareRecordEntity record);

    @Query("DELETE FROM care_records WHERE plantId = :plantId")
    void deleteByPlantId(long plantId);

    @Query("SELECT * FROM care_records WHERE plantId = :plantId ORDER BY performedAt DESC")
    LiveData<List<CareRecordEntity>> getRecordsByPlant(long plantId);

    @Query("SELECT * FROM care_records WHERE plantId = :plantId ORDER BY performedAt DESC")
    List<CareRecordEntity> getRecordsByPlantSync(long plantId);

    @Query("SELECT * FROM care_records WHERE plantId = :plantId ORDER BY performedAt DESC LIMIT 1")
    LiveData<CareRecordEntity> getLastRecordForPlant(long plantId);

    @Query("SELECT * FROM care_records WHERE plantId = :plantId ORDER BY performedAt DESC LIMIT 1")
    CareRecordEntity getLastRecordForPlantSync(long plantId);

    @Query("SELECT * FROM care_records WHERE plantId = :plantId AND careTypeId = :careTypeId ORDER BY performedAt DESC LIMIT 1")
    CareRecordEntity getLastRecordForPlantAndType(long plantId, long careTypeId);

    @Query("SELECT * FROM care_records ORDER BY performedAt DESC")
    LiveData<List<CareRecordEntity>> getAllRecords();

    @Query("SELECT * FROM care_records ORDER BY performedAt DESC")
    List<CareRecordEntity> getAllRecordsSync();
}
