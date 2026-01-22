package com.trackapp.habity.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
    entities = {HabitEntity.class, ReminderEntity.class, CompletionEntity.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HabitDao habitDao();
    public abstract ReminderDao reminderDao();
    public abstract CompletionDao completionDao();
    
    private static volatile AppDatabase INSTANCE;
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "habity_database"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // SQLite doesn't support adding foreign key constraints via ALTER TABLE
            // We need to recreate the table with the new foreign key
            
            // Step 1: Create new table with reminderId and foreign key
            database.execSQL("CREATE TABLE IF NOT EXISTS completions_new (" +
                "date TEXT NOT NULL, " +
                "habitId INTEGER NOT NULL, " +
                "reminderId INTEGER, " +
                "done INTEGER NOT NULL, " +
                "PRIMARY KEY(date, habitId), " +
                "FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE, " +
                "FOREIGN KEY(reminderId) REFERENCES reminders(id) ON DELETE SET NULL" +
                ")");
            
            // Step 2: Copy data from old table to new table
            database.execSQL("INSERT INTO completions_new (date, habitId, done) " +
                "SELECT date, habitId, done FROM completions");
            
            // Step 3: Drop old table
            database.execSQL("DROP TABLE completions");
            
            // Step 4: Rename new table to original name
            database.execSQL("ALTER TABLE completions_new RENAME TO completions");
            
            // Step 5: Create indices
            database.execSQL("CREATE INDEX IF NOT EXISTS index_completions_habitId ON completions(habitId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_completions_reminderId ON completions(reminderId)");
        }
    };
    
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Add date column to reminders table
            // SQLite doesn't support adding NOT NULL columns with ALTER TABLE, so we recreate the table
            
            // Step 1: Create new table with date column
            database.execSQL("CREATE TABLE IF NOT EXISTS reminders_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "habitId INTEGER NOT NULL, " +
                "date TEXT NOT NULL DEFAULT '', " +
                "hour INTEGER NOT NULL, " +
                "minute INTEGER NOT NULL, " +
                "daysMask INTEGER NOT NULL, " +
                "enabled INTEGER NOT NULL, " +
                "FOREIGN KEY(habitId) REFERENCES habits(id) ON DELETE CASCADE" +
                ")");
            
            // Step 2: Copy data from old table to new table (set date to empty string for existing records)
            database.execSQL("INSERT INTO reminders_new (id, habitId, date, hour, minute, daysMask, enabled) " +
                "SELECT id, habitId, '', hour, minute, daysMask, enabled FROM reminders");
            
            // Step 3: Drop old table
            database.execSQL("DROP TABLE reminders");
            
            // Step 4: Rename new table to original name
            database.execSQL("ALTER TABLE reminders_new RENAME TO reminders");
            
            // Step 5: Recreate indices
            database.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_habitId ON reminders(habitId)");
            database.execSQL("CREATE INDEX IF NOT EXISTS index_reminders_date ON reminders(date)");
        }
    };
}


