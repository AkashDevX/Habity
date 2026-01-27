package com.cleverkube.watermark.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_containers")
public class WaterContainer {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name; // e.g., "Water Bottle", "Glass", "Mug"
    public int amountMl;
    public String icon; // Emoji
    public boolean isDefault;
    public int order; // For sorting
    
    public WaterContainer() {
        this.isDefault = false;
        this.order = 0;
    }
    
    public WaterContainer(String name, int amountMl, String icon, boolean isDefault, int order) {
        this.name = name;
        this.amountMl = amountMl;
        this.icon = icon;
        this.isDefault = isDefault;
        this.order = order;
    }
}


