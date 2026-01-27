package com.carmaintenance.tracker.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {
    @Insert
    long insert(ReminderEntity reminder);
    
    @Update
    void update(ReminderEntity reminder);
    
    @Delete
    void delete(ReminderEntity reminder);
    
    @Query("SELECT * FROM reminders WHERE serviceId = :serviceId ORDER BY hour, minute")
    List<ReminderEntity> getRemindersForService(long serviceId);
    
    @Query("SELECT * FROM reminders WHERE enabled = 1")
    List<ReminderEntity> getEnabledReminders();
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    ReminderEntity getReminderById(long id);
    
    @Query("DELETE FROM reminders WHERE serviceId = :serviceId")
    void deleteRemindersForService(long serviceId);
    
    @Query("SELECT * FROM reminders WHERE date = :date")
    List<ReminderEntity> getRemindersForDate(String date);
    
    @Query("SELECT * FROM reminders WHERE serviceId = :serviceId AND date = :date")
    List<ReminderEntity> getRemindersForServiceAndDate(long serviceId, String date);
}





