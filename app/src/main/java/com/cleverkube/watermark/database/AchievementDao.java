package com.cleverkube.watermark.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AchievementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Achievement achievement);
    
    @Update
    void update(Achievement achievement);
    
    @Query("SELECT * FROM achievements ORDER BY unlocked DESC, id ASC")
    LiveData<List<Achievement>> getAllAchievements();
    
    @Query("SELECT * FROM achievements ORDER BY unlocked DESC, id ASC")
    List<Achievement> getAllAchievementsSync();
    
    @Query("SELECT * FROM achievements WHERE id = :id")
    Achievement getAchievementById(String id);
    
    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1")
    LiveData<Integer> getUnlockedCount();
    
    @Query("SELECT COUNT(*) FROM achievements WHERE unlocked = 1")
    Integer getUnlockedCountSync();
}


