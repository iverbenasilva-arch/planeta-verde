package com.example.plantgreencare.data.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "plants")
public class PlantEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String scientificName;
    private String photoUri;
    private String location;
    private String acquisitionDate;
    private String description;
    private String lightLevel;
    private String humidityLevel;
    private long createdAt;
    private long updatedAt;

    public PlantEntity() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    @Ignore
    public PlantEntity(String name, String scientificName, String photoUri, String location,
                       String acquisitionDate, String description, String lightLevel,
                       String humidityLevel) {
        this.name = name;
        this.scientificName = scientificName;
        this.photoUri = photoUri;
        this.location = location;
        this.acquisitionDate = acquisitionDate;
        this.description = description;
        this.lightLevel = lightLevel;
        this.humidityLevel = humidityLevel;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getAcquisitionDate() {
        return acquisitionDate;
    }

    public void setAcquisitionDate(String acquisitionDate) {
        this.acquisitionDate = acquisitionDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLightLevel() {
        return lightLevel;
    }

    public void setLightLevel(String lightLevel) {
        this.lightLevel = lightLevel;
    }

    public String getHumidityLevel() {
        return humidityLevel;
    }

    public void setHumidityLevel(String humidityLevel) {
        this.humidityLevel = humidityLevel;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
