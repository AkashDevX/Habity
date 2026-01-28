package com.inventorymanager.app.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {ItemEntity.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class InventoryDatabase extends RoomDatabase {
    private static InventoryDatabase instance;

    public abstract ItemDao itemDao();

    public static synchronized InventoryDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    InventoryDatabase.class,
                    "inventory_database"
            ).fallbackToDestructiveMigration().build();
        }
        return instance;
    }
}
