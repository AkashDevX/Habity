package com.cleverkube.watermark.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.cleverkube.watermark.database.AppDatabase;
import com.cleverkube.watermark.database.ReminderConfig;
import com.cleverkube.watermark.database.ReminderConfigDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderReceiver extends BroadcastReceiver {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // Show notification
        NotificationUtil.showWaterReminderNotification(context);
        
        // Reschedule next reminder
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            ReminderConfigDao configDao = db.reminderConfigDao();
            ReminderConfig config = configDao.getConfigSync();
            
            if (config != null && config.enabled) {
                ReminderScheduler.scheduleNextReminder(context, config);
            }
        });
    }
}


