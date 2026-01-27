package com.cleverkube.watermark.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.cleverkube.watermark.database.AppDatabase;
import com.cleverkube.watermark.database.ReminderConfig;
import com.cleverkube.watermark.database.ReminderConfigDao;

import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderScheduler {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final int REQUEST_CODE = 1001;
    
    public static void scheduleAllEnabled(Context context) {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(context);
                ReminderConfigDao configDao = db.reminderConfigDao();
                ReminderConfig config = configDao.getConfigSync();
                
                if (config != null && config.enabled) {
                    scheduleNextReminder(context, config);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    public static void scheduleNextReminder(Context context, ReminderConfig config) {
        try {
            if (config == null || !config.enabled) {
                cancelAllReminders(context);
                return;
            }
            
            long nextTrigger = computeNextTriggerTime(config);
            if (nextTrigger <= 0) {
                return; // No valid time found
            }
            
            Intent intent = new Intent(context, ReminderReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            nextTrigger,
                            pendingIntent
                        );
                        return;
                    }
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTrigger,
                        pendingIntent
                    );
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, nextTrigger, pendingIntent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void cancelAllReminders(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
    
    private static long computeNextTriggerTime(ReminderConfig config) {
        Calendar now = Calendar.getInstance();
        Calendar start = Calendar.getInstance();
        start.set(Calendar.HOUR_OF_DAY, config.startHour);
        start.set(Calendar.MINUTE, config.startMinute);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        
        Calendar end = Calendar.getInstance();
        end.set(Calendar.HOUR_OF_DAY, config.endHour);
        end.set(Calendar.MINUTE, config.endMinute);
        end.set(Calendar.SECOND, 0);
        end.set(Calendar.MILLISECOND, 0);
        
        // If we're before start time today, schedule for start time
        if (now.before(start)) {
            return start.getTimeInMillis();
        }
        
        // If we're after end time, schedule for start time tomorrow
        if (now.after(end) || now.equals(end)) {
            start.add(Calendar.DAY_OF_MONTH, 1);
            return start.getTimeInMillis();
        }
        
        // We're in the active window, find next interval
        Calendar next = (Calendar) start.clone();
        while (next.before(now) || next.equals(now)) {
            next.add(Calendar.HOUR_OF_DAY, config.intervalHours);
        }
        
        // If next is after end time, schedule for start time tomorrow
        if (next.after(end)) {
            start.add(Calendar.DAY_OF_MONTH, 1);
            return start.getTimeInMillis();
        }
        
        return next.getTimeInMillis();
    }
}

