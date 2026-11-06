package com.example.plantgreencare.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.plantgreencare.data.entities.AppSettingsEntity;

@Dao
public interface SettingsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AppSettingsEntity settings);

    @Update
    void update(AppSettingsEntity settings);

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    LiveData<AppSettingsEntity> getSettings();

    @Query("SELECT * FROM app_settings WHERE id = 1 LIMIT 1")
    AppSettingsEntity getSettingsSync();
}
