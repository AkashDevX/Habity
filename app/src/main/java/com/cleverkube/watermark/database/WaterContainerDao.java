package com.cleverkube.watermark.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface WaterContainerDao {
    @Insert
    long insert(WaterContainer container);
    
    @Update
    void update(WaterContainer container);
    
    @Delete
    void delete(WaterContainer container);
    
    @Query("SELECT * FROM water_containers ORDER BY `order` ASC, amountMl ASC")
    LiveData<List<WaterContainer>> getAllContainers();
    
    @Query("SELECT * FROM water_containers ORDER BY `order` ASC, amountMl ASC")
    List<WaterContainer> getAllContainersSync();
    
    @Query("SELECT * FROM water_containers WHERE id = :id")
    WaterContainer getContainerById(long id);
}


