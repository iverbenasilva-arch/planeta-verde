package com.example.plantgreencare.data.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "care_types")
public class CareTypeEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String description;
    private String icon;

    public CareTypeEntity() {
    }

    @Ignore
    public CareTypeEntity(String name, String description, String icon) {
        this.name = name;
        this.description = description;
        this.icon = icon;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }
}
