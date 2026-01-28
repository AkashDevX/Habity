package com.passwordmanager.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface PasswordDao {
    @Query("SELECT * FROM passwords ORDER BY title ASC")
    LiveData<List<PasswordEntity>> getAll();
    
    @Query("SELECT * FROM passwords WHERE categoryId = :categoryId ORDER BY title ASC")
    List<PasswordEntity> getByCategory(long categoryId);
    
    @Query("SELECT * FROM passwords WHERE categoryId IS NULL ORDER BY title ASC")
    List<PasswordEntity> getUncategorized();
    
    @Query("SELECT * FROM passwords WHERE id = :id")
    PasswordEntity getById(long id);
    
    @Query("SELECT * FROM passwords WHERE title LIKE :query OR username LIKE :query OR website LIKE :query")
    List<PasswordEntity> search(String query);
    
    @Insert
    long insert(PasswordEntity password);
    
    @Update
    void update(PasswordEntity password);
    
    @Delete
    void delete(PasswordEntity password);
}
