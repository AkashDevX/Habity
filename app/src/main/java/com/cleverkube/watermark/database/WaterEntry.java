package com.cleverkube.watermark.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "water_entries")
public class WaterEntry {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public int amountMl; // Amount in milliliters
    public long timestamp; // Unix timestamp in milliseconds
    public String note; // Optional note
    
    public WaterEntry() {
        this.note = "";
    }
    
    public WaterEntry(int amountMl, long timestamp, String note) {
        this.amountMl = amountMl;
        this.timestamp = timestamp;
        this.note = note != null ? note : "";
    }
}


