package com.cleverkube.watermark.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminder_config")
public class ReminderConfig {
    @PrimaryKey
    public long id = 1; // Single row
    
    public boolean enabled;
    public int startHour; // Start time hour (0-23)
    public int startMinute; // Start time minute (0-59)
    public int endHour; // End time hour (0-23)
    public int endMinute; // End time minute (0-59)
    public int intervalHours; // Interval in hours (e.g., 2 for every 2 hours)
    
    public ReminderConfig() {
        this.enabled = false;
        this.startHour = 8;
        this.startMinute = 0;
        this.endHour = 22;
        this.endMinute = 0;
        this.intervalHours = 2;
    }
    
    public ReminderConfig(boolean enabled, int startHour, int startMinute, int endHour, int endMinute, int intervalHours) {
        this.enabled = enabled;
        this.startHour = startHour;
        this.startMinute = startMinute;
        this.endHour = endHour;
        this.endMinute = endMinute;
        this.intervalHours = intervalHours;
    }
}


