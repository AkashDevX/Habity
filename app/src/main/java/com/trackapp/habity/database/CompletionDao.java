package com.trackapp.habity.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CompletionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertCompletion(CompletionEntity completion);
    
    @Query("SELECT * FROM completions WHERE date = :date AND habitId = :habitId")
    CompletionEntity getCompletionForDate(String date, long habitId);
    
    @Query("SELECT * FROM completions WHERE date = :date")
    List<CompletionEntity> getCompletionsForDate(String date);
    
    @Query("SELECT * FROM completions WHERE habitId = :habitId AND date = :date")
    CompletionEntity getCompletion(String date, long habitId);
}

