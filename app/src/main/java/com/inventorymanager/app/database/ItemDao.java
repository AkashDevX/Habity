package com.inventorymanager.app.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

@Dao
public interface ItemDao {
    @Query("SELECT * FROM items ORDER BY name ASC")
    LiveData<List<ItemEntity>> getAllItems();

    @Query("SELECT * FROM items WHERE category = :category ORDER BY name ASC")
    LiveData<List<ItemEntity>> getItemsByCategory(String category);

    @Query("SELECT * FROM items WHERE name LIKE '%' || :query || '%' ORDER BY name ASC")
    LiveData<List<ItemEntity>> searchItems(String query);

    @Query("SELECT DISTINCT category FROM items ORDER BY category ASC")
    LiveData<List<String>> getAllCategories();

    @Query("SELECT * FROM items WHERE id = :id")
    LiveData<ItemEntity> getItemById(long id);

    @Query("SELECT COUNT(*) FROM items")
    LiveData<Integer> getTotalItemCount();

    @Query("SELECT COUNT(DISTINCT category) FROM items")
    LiveData<Integer> getTotalCategoryCount();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(ItemEntity item);

    @Update
    void update(ItemEntity item);

    @Delete
    void delete(ItemEntity item);

    @Query("DELETE FROM items WHERE id = :id")
    void deleteById(long id);
}
