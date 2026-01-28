package com.passwordmanager.app.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String name;
    public String color; // Hex color code
    
    public CategoryEntity(String name, String color) {
        this.name = name;
        this.color = color;
    }
}
