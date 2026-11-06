package com.example.plantgreencare.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.plantgreencare.data.entities.PlantEntity;

import java.util.List;

@Dao
public interface PlantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PlantEntity plant);

    @Update
    void update(PlantEntity plant);

    @Delete
    void delete(PlantEntity plant);

    @Query("DELETE FROM plants WHERE id = :id")
    void deleteById(long id);

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    LiveData<PlantEntity> getPlantById(long id);

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    PlantEntity getPlantByIdSync(long id);

    @Query("SELECT * FROM plants ORDER BY name ASC")
    LiveData<List<PlantEntity>> getAllPlants();

    @Query("SELECT * FROM plants ORDER BY name ASC")
    List<PlantEntity> getAllPlantsSync();

    @Query("SELECT * FROM plants WHERE name LIKE '%' || :query || '%' OR scientificName LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<PlantEntity>> searchPlants(String query);

    @Query("SELECT COUNT(*) FROM plants")
    LiveData<Integer> getPlantsCount();

    @Query("SELECT COUNT(*) FROM plants")
    int getPlantsCountSync();
}
