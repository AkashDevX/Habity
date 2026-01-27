package com.carmaintenance.tracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FuelDao {
    @Insert
    long insert(FuelEntity fuel);
    
    @Update
    void update(FuelEntity fuel);
    
    @Delete
    void delete(FuelEntity fuel);
    
    @Query("SELECT * FROM fuel_logs ORDER BY date DESC, mileage DESC")
    LiveData<List<FuelEntity>> getAllFuelLogs();
    
    @Query("SELECT * FROM fuel_logs ORDER BY date DESC, mileage DESC")
    List<FuelEntity> getAllFuelLogsSync();
    
    @Query("SELECT * FROM fuel_logs WHERE id = :id")
    FuelEntity getFuelLogById(long id);
    
    @Query("SELECT * FROM fuel_logs WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<FuelEntity>> getFuelLogsByDateRange(String startDate, String endDate);
    
    @Query("SELECT SUM(totalCost) FROM fuel_logs")
    LiveData<Double> getTotalFuelCost();
    
    @Query("SELECT AVG(totalCost) FROM fuel_logs")
    LiveData<Double> getAverageFuelCost();
    
    @Query("SELECT * FROM fuel_logs ORDER BY mileage DESC LIMIT 1")
    FuelEntity getLatestFuelLog();
}

