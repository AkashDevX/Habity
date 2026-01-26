package com.lifetracker.habits.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    long insert(CategoryEntity category);
    
    @Update
    void update(CategoryEntity category);
    
    @Delete
    void delete(CategoryEntity category);
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    LiveData<List<CategoryEntity>> getAllCategories();
    
    @Query("SELECT * FROM categories ORDER BY name ASC")
    List<CategoryEntity> getAllCategoriesSync();
    
    @Query("SELECT * FROM categories WHERE id = :id")
    CategoryEntity getCategoryById(long id);
}

