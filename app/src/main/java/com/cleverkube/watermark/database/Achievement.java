package com.cleverkube.watermark.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "achievements")
public class Achievement {
    @PrimaryKey
    @NonNull
    public String id; // e.g., "first_log", "streak_7", "goal_met_30"
    
    public String title;
    public String description;
    public String icon; // Emoji or icon identifier
    public boolean unlocked;
    public long unlockedAt; // Timestamp when unlocked
    
    public Achievement() {
        this.id = "";
        this.title = "";
        this.description = "";
        this.icon = "";
        this.unlocked = false;
        this.unlockedAt = 0;
    }
    
    public Achievement(@NonNull String id, String title, String description, String icon) {
        this.id = id;
        this.title = title != null ? title : "";
        this.description = description != null ? description : "";
        this.icon = icon != null ? icon : "";
        this.unlocked = false;
        this.unlockedAt = 0;
    }
}

