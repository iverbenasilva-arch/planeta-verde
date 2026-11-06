package com.example.plantgreencare.data.entities;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "care_records",
    foreignKeys = @ForeignKey(
        entity = PlantEntity.class,
        parentColumns = "id",
        childColumns = "plantId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("plantId"), @Index("performedAt")}
)
public class CareRecordEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long plantId;
    private long careTypeId;
    private long performedAt;
    private String notes;

    public CareRecordEntity() {
        this.performedAt = System.currentTimeMillis();
    }

    @androidx.room.Ignore
    public CareRecordEntity(long plantId, long careTypeId, long performedAt, String notes) {
        this.plantId = plantId;
        this.careTypeId = careTypeId;
        this.performedAt = performedAt;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getPlantId() {
        return plantId;
    }

    public void setPlantId(long plantId) {
        this.plantId = plantId;
    }

    public long getCareTypeId() {
        return careTypeId;
    }

    public void setCareTypeId(long careTypeId) {
        this.careTypeId = careTypeId;
    }

    public long getPerformedAt() {
        return performedAt;
    }

    public void setPerformedAt(long performedAt) {
        this.performedAt = performedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
