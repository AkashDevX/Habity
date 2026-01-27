package com.cleverkube.watermark.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WaterDao {
    @Insert
    long insert(WaterEntry entry);
    
    @Update
    void update(WaterEntry entry);
    
    @Delete
    void delete(WaterEntry entry);
    
    @Query("SELECT * FROM water_entries WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    LiveData<List<WaterEntry>> getEntriesForDay(long startOfDay, long endOfDay);
    
    @Query("SELECT * FROM water_entries WHERE timestamp >= :startOfDay AND timestamp < :endOfDay ORDER BY timestamp DESC")
    List<WaterEntry> getEntriesForDaySync(long startOfDay, long endOfDay);
    
    @Query("SELECT * FROM water_entries WHERE timestamp >= :startTimestamp AND timestamp < :endTimestamp ORDER BY timestamp DESC")
    List<WaterEntry> getEntriesForRange(long startTimestamp, long endTimestamp);
    
    @Query("SELECT SUM(amountMl) FROM water_entries WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    LiveData<Integer> getTotalForDay(long startOfDay, long endOfDay);
    
    @Query("SELECT SUM(amountMl) FROM water_entries WHERE timestamp >= :startOfDay AND timestamp < :endOfDay")
    Integer getTotalForDaySync(long startOfDay, long endOfDay);
    
    @Query("SELECT * FROM water_entries WHERE id = :id")
    WaterEntry getEntryById(long id);
    
    @Query("SELECT * FROM water_entries ORDER BY timestamp DESC LIMIT :limit")
    List<WaterEntry> getRecentEntries(int limit);
    
    @Query("DELETE FROM water_entries WHERE id = :id")
    void deleteById(long id);
}


