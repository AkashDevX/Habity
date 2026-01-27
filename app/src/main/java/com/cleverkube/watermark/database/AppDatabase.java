package com.cleverkube.watermark.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
    entities = {WaterEntry.class, ReminderConfig.class, Achievement.class, WaterContainer.class},
    version = 3,
    exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {
    public abstract WaterDao waterDao();
    public abstract ReminderConfigDao reminderConfigDao();
    public abstract AchievementDao achievementDao();
    public abstract WaterContainerDao waterContainerDao();
    
    private static volatile AppDatabase INSTANCE;
    
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        AppDatabase.class,
                        "watermark_database"
                    )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build();
                    
                    // Initialize default data
                    initializeDefaultData(context);
                }
            }
        }
        return INSTANCE;
    }
    
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Create achievements table - fields match entity (title, description, icon are nullable)
            database.execSQL("CREATE TABLE IF NOT EXISTS achievements (" +
                "id TEXT PRIMARY KEY NOT NULL, " +
                "title TEXT, " +
                "description TEXT, " +
                "icon TEXT, " +
                "unlocked INTEGER NOT NULL, " +
                "unlockedAt INTEGER NOT NULL" +
                ")");
            
            // Create water_containers table - fields match entity (name and icon are nullable)
            database.execSQL("CREATE TABLE IF NOT EXISTS water_containers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT, " +
                "amountMl INTEGER NOT NULL, " +
                "icon TEXT, " +
                "isDefault INTEGER NOT NULL, " +
                "`order` INTEGER NOT NULL" +
                ")");
        }
    };
    
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Fix achievements table if it was created with wrong constraints
            // Drop and recreate with correct nullable fields
            database.execSQL("DROP TABLE IF EXISTS achievements");
            database.execSQL("CREATE TABLE achievements (" +
                "id TEXT PRIMARY KEY NOT NULL, " +
                "title TEXT, " +
                "description TEXT, " +
                "icon TEXT, " +
                "unlocked INTEGER NOT NULL, " +
                "unlockedAt INTEGER NOT NULL" +
                ")");
            
            // Fix water_containers table if it was created with wrong constraints
            // Drop and recreate with correct nullable fields
            database.execSQL("DROP TABLE IF EXISTS water_containers");
            database.execSQL("CREATE TABLE water_containers (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "name TEXT, " +
                "amountMl INTEGER NOT NULL, " +
                "icon TEXT, " +
                "isDefault INTEGER NOT NULL, " +
                "`order` INTEGER NOT NULL" +
                ")");
        }
    };
    
    private static void initializeDefaultData(Context context) {
        new Thread(() -> {
            try {
                AppDatabase db = INSTANCE; // Use the already created instance
                if (db == null) return;
                
                // Initialize default achievements
                try {
                    if (db.achievementDao().getAllAchievementsSync().isEmpty()) {
                        db.achievementDao().insert(new Achievement("first_log", "First Drop", "Log your first water intake", "💧"));
                        db.achievementDao().insert(new Achievement("goal_met", "Goal Achiever", "Meet your daily goal for the first time", "🎯"));
                        db.achievementDao().insert(new Achievement("streak_3", "Getting Started", "Maintain a 3-day streak", "🔥"));
                        db.achievementDao().insert(new Achievement("streak_7", "Week Warrior", "Maintain a 7-day streak", "⭐"));
                        db.achievementDao().insert(new Achievement("streak_30", "Monthly Master", "Maintain a 30-day streak", "🏆"));
                        db.achievementDao().insert(new Achievement("goal_met_7", "Consistent", "Meet your goal 7 days in a row", "📅"));
                        db.achievementDao().insert(new Achievement("goal_met_30", "Dedicated", "Meet your goal 30 days in a row", "💎"));
                        db.achievementDao().insert(new Achievement("total_100", "Century Club", "Log 100 total water entries", "💯"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                // Initialize default containers
                try {
                    if (db.waterContainerDao().getAllContainersSync().isEmpty()) {
                        db.waterContainerDao().insert(new WaterContainer("Small Glass", 200, "🥛", true, 1));
                        db.waterContainerDao().insert(new WaterContainer("Regular Glass", 250, "🥛", true, 2));
                        db.waterContainerDao().insert(new WaterContainer("Large Glass", 300, "🥛", true, 3));
                        db.waterContainerDao().insert(new WaterContainer("Water Bottle", 500, "🍶", true, 4));
                        db.waterContainerDao().insert(new WaterContainer("Large Bottle", 750, "🍶", true, 5));
                        db.waterContainerDao().insert(new WaterContainer("1 Liter Bottle", 1000, "🍶", true, 6));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

