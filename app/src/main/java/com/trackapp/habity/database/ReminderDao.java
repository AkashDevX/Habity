package com.trackapp.habity.database;

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
    
    @Query("SELECT * FROM reminders WHERE habitId = :habitId ORDER BY hour, minute")
    List<ReminderEntity> getRemindersForHabit(long habitId);
    
    @Query("SELECT * FROM reminders WHERE enabled = 1")
    List<ReminderEntity> getEnabledReminders();
    
    @Query("SELECT * FROM reminders WHERE id = :id")
    ReminderEntity getReminderById(long id);
    
    @Query("DELETE FROM reminders WHERE habitId = :habitId")
    void deleteRemindersForHabit(long habitId);
    
    @Query("SELECT * FROM reminders WHERE date = :date")
    List<ReminderEntity> getRemindersForDate(String date);
    
    @Query("SELECT * FROM reminders WHERE habitId = :habitId AND date = :date")
    List<ReminderEntity> getRemindersForHabitAndDate(long habitId, String date);
}

