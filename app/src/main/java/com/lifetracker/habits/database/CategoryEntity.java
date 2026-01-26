package com.lifetracker.habits.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    @NonNull
    public String name;
    public String color; // Hex color code
    public String icon; // Emoji or icon identifier
    
    public CategoryEntity() {
        this.name = "";
    }
    
    public CategoryEntity(@NonNull String name, String color, String icon) {
        this.name = name;
        this.color = color;
        this.icon = icon;
    }
}

