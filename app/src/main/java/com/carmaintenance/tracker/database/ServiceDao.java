package com.carmaintenance.tracker.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ServiceDao {
    @Insert
    long insert(ServiceEntity service);
    
    @Update
    void update(ServiceEntity service);
    
    @Delete
    void delete(ServiceEntity service);
    
    @Query("SELECT * FROM services ORDER BY date DESC, id DESC")
    LiveData<List<ServiceEntity>> getAllServices();
    
    @Query("SELECT * FROM services ORDER BY date DESC, id DESC")
    List<ServiceEntity> getAllServicesSync();
    
    @Query("SELECT * FROM services WHERE id = :id")
    ServiceEntity getServiceById(long id);
    
    @Query("SELECT * FROM services WHERE serviceType = :serviceType ORDER BY date DESC")
    LiveData<List<ServiceEntity>> getServicesByType(String serviceType);
    
    @Query("SELECT * FROM services WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    LiveData<List<ServiceEntity>> getServicesByDateRange(String startDate, String endDate);
    
    @Query("SELECT SUM(cost) FROM services WHERE cost IS NOT NULL")
    LiveData<Double> getTotalCost();
}

