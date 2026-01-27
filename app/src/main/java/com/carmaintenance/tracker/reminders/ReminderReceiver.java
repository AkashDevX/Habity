package com.carmaintenance.tracker.reminders;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.carmaintenance.tracker.database.AppDatabase;
import com.carmaintenance.tracker.database.ServiceDao;
import com.carmaintenance.tracker.database.ServiceEntity;
import com.carmaintenance.tracker.database.ReminderDao;
import com.carmaintenance.tracker.database.ReminderEntity;

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
            
            // Load service title
            ServiceDao serviceDao = db.serviceDao();
            ServiceEntity service = serviceDao.getServiceById(reminder.serviceId);
            
            if (service == null || !service.enabled) {
                return;
            }
            
            // Show notification (this can be called from any thread)
            NotificationUtil.showNotification(context, reminderId, service.title);
            
            // Reschedule next occurrence
            ReminderScheduler.scheduleReminder(context, reminderId);
        });
    }
}

