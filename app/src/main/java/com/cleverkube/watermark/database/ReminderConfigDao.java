package com.cleverkube.watermark.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface ReminderConfigDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ReminderConfig config);
    
    @Update
    void update(ReminderConfig config);
    
    @Query("SELECT * FROM reminder_config WHERE id = 1")
    LiveData<ReminderConfig> getConfig();
    
    @Query("SELECT * FROM reminder_config WHERE id = 1")
    ReminderConfig getConfigSync();
}


