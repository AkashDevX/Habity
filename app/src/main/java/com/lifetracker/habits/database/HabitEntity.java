package com.lifetracker.habits.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class HabitEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String title;
    public boolean enabled;
    public Long categoryId; // Optional category
    public String icon; // Optional emoji icon
    public String color; // Optional color for the habit
    
    public HabitEntity() {
        this.enabled = true;
    }
    
    public HabitEntity(String title, boolean enabled) {
        this.title = title;
        this.enabled = enabled;
    }
}





