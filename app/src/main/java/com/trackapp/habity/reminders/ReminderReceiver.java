package com.trackapp.habity.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.trackapp.habity.database.AppDatabase;
import com.trackapp.habity.database.HabitDao;
import com.trackapp.habity.database.ReminderDao;
import com.trackapp.habity.database.ReminderEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderReceiver extends BroadcastReceiver {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    @Override
    public void onReceive(Context context, Intent intent) {
        long reminderId = intent.getLongExtra("reminderId", -1);
        if (reminderId == -1) {
            return;
        }
        
        // Run database operations on background thread
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            ReminderDao reminderDao = db.reminderDao();
            ReminderEntity reminder = reminderDao.getReminderById(reminderId);
            
            if (reminder == null || !reminder.enabled) {
                return;
            }
            
            // Load habit title
            HabitDao habitDao = db.habitDao();
            com.trackapp.habity.database.HabitEntity habit = habitDao.getHabitById(reminder.habitId);
            
            if (habit == null || !habit.enabled) {
                return;
            }
            
            // Show notification (this can be called from any thread)
            NotificationUtil.showNotification(context, reminderId, habit.title);
            
            // Reschedule next occurrence
            ReminderScheduler.scheduleReminder(context, reminderId);
        });
    }
}

