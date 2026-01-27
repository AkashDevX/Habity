package com.cleverkube.watermark.utils;

import android.content.Context;

import com.cleverkube.watermark.database.AppDatabase;
import com.cleverkube.watermark.database.Achievement;
import com.cleverkube.watermark.database.AchievementDao;
import com.cleverkube.watermark.database.WaterDao;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AchievementChecker {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public static void checkAchievements(Context context, int dailyGoal) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(context);
                AchievementDao achievementDao = db.achievementDao();
                WaterDao waterDao = db.waterDao();
                
                // Check first log
                checkFirstLog(achievementDao, waterDao);
                
                // Check goal achievements
                checkGoalAchievements(achievementDao, waterDao, dailyGoal);
                
                // Check streak achievements
                checkStreakAchievements(achievementDao, waterDao, dailyGoal);
                
                // Check total entries
                checkTotalEntries(achievementDao, waterDao);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private static void checkFirstLog(AchievementDao achievementDao, WaterDao waterDao) {
        try {
            Achievement achievement = achievementDao.getAchievementById("first_log");
            if (achievement != null && !achievement.unlocked) {
                List<com.cleverkube.watermark.database.WaterEntry> entries = waterDao.getRecentEntries(1);
                if (!entries.isEmpty()) {
                    achievement.unlocked = true;
                    achievement.unlockedAt = System.currentTimeMillis();
                    achievementDao.update(achievement);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void checkGoalAchievements(AchievementDao achievementDao, WaterDao waterDao, int dailyGoal) {
        try {
            Calendar cal = Calendar.getInstance();
            int consecutiveDays = 0;
            int totalMet = 0;
            
            for (int i = 0; i < 30; i++) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startOfDay = cal.getTimeInMillis();
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
                long endOfDay = cal.getTimeInMillis();
                
                Integer total = waterDao.getTotalForDaySync(startOfDay, endOfDay);
                if (total != null && total >= dailyGoal) {
                    consecutiveDays++;
                    totalMet++;
                } else {
                    consecutiveDays = 0;
                }
                
                cal.add(Calendar.DAY_OF_MONTH, -1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
            }
            
            // Check goal_met (first time)
            Achievement goalMet = achievementDao.getAchievementById("goal_met");
            if (goalMet != null && !goalMet.unlocked && totalMet > 0) {
                goalMet.unlocked = true;
                goalMet.unlockedAt = System.currentTimeMillis();
                achievementDao.update(goalMet);
            }
            
            // Check goal_met_7
            Achievement goal7 = achievementDao.getAchievementById("goal_met_7");
            if (goal7 != null && !goal7.unlocked && consecutiveDays >= 7) {
                goal7.unlocked = true;
                goal7.unlockedAt = System.currentTimeMillis();
                achievementDao.update(goal7);
            }
            
            // Check goal_met_30
            Achievement goal30 = achievementDao.getAchievementById("goal_met_30");
            if (goal30 != null && !goal30.unlocked && consecutiveDays >= 30) {
                goal30.unlocked = true;
                goal30.unlockedAt = System.currentTimeMillis();
                achievementDao.update(goal30);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void checkStreakAchievements(AchievementDao achievementDao, WaterDao waterDao, int dailyGoal) {
        try {
            Calendar cal = Calendar.getInstance();
            int streak = 0;
            
            while (true) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startOfDay = cal.getTimeInMillis();
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
                long endOfDay = cal.getTimeInMillis();
                
                Integer total = waterDao.getTotalForDaySync(startOfDay, endOfDay);
                if (total != null && total >= dailyGoal) {
                    streak++;
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                } else {
                    break;
                }
                
                if (streak > 365) break;
            }
            
            // Check streak_3
            Achievement streak3 = achievementDao.getAchievementById("streak_3");
            if (streak3 != null && !streak3.unlocked && streak >= 3) {
                streak3.unlocked = true;
                streak3.unlockedAt = System.currentTimeMillis();
                achievementDao.update(streak3);
            }
            
            // Check streak_7
            Achievement streak7 = achievementDao.getAchievementById("streak_7");
            if (streak7 != null && !streak7.unlocked && streak >= 7) {
                streak7.unlocked = true;
                streak7.unlockedAt = System.currentTimeMillis();
                achievementDao.update(streak7);
            }
            
            // Check streak_30
            Achievement streak30 = achievementDao.getAchievementById("streak_30");
            if (streak30 != null && !streak30.unlocked && streak >= 30) {
                streak30.unlocked = true;
                streak30.unlockedAt = System.currentTimeMillis();
                achievementDao.update(streak30);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void checkTotalEntries(AchievementDao achievementDao, WaterDao waterDao) {
        try {
            List<com.cleverkube.watermark.database.WaterEntry> entries = waterDao.getRecentEntries(1000);
            int totalCount = entries != null ? entries.size() : 0;
            
            Achievement total100 = achievementDao.getAchievementById("total_100");
            if (total100 != null && !total100.unlocked && totalCount >= 100) {
                total100.unlocked = true;
                total100.unlockedAt = System.currentTimeMillis();
                achievementDao.update(total100);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

