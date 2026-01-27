package com.carmaintenance.tracker.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "reminders",
    foreignKeys = @ForeignKey(
        entity = ServiceEntity.class,
        parentColumns = "id",
        childColumns = "serviceId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("serviceId"), @Index("date")}
)
public class ReminderEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long serviceId;
    
    // Date format: "yyyy-MM-dd" - the date this reminder is for
    @NonNull
    public String date;
    
    // Hour in 24-hour format (0-23)
    public int hour;
    
    // Minute (0-59)
    public int minute;
    
    // Days mask: Sun=1, Mon=2, Tue=4, Wed=8, Thu=16, Fri=32, Sat=64 (all=127)
    public int daysMask;
    
    public boolean enabled;
    
    public ReminderEntity() {
        this.daysMask = 127; // All days by default
        this.enabled = true;
        this.date = "";
    }
    
    public ReminderEntity(long serviceId, String date, int hour, int minute, int daysMask, boolean enabled) {
        this.serviceId = serviceId;
        this.date = date;
        this.hour = hour;
        this.minute = minute;
        this.daysMask = daysMask;
        this.enabled = enabled;
    }
}





