package com.carmaintenance.tracker.reminders;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;

import com.carmaintenance.tracker.database.AppDatabase;
import com.carmaintenance.tracker.database.ReminderDao;
import com.carmaintenance.tracker.database.ReminderEntity;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderScheduler {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    /**
     * Schedules a reminder alarm using AlarmManager.
     * Calculates the next trigger time based on hour, minute, and daysMask.
     * This method runs database operations on a background thread.
     */
    public static void scheduleReminder(Context context, long reminderId) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            ReminderDao reminderDao = db.reminderDao();
            
            // Load reminder and check if enabled
            ReminderEntity reminder = reminderDao.getReminderById(reminderId);
            if (reminder == null || !reminder.enabled) {
                return;
            }
            
            // Load service to check if enabled
            com.carmaintenance.tracker.database.ServiceDao serviceDao = db.serviceDao();
            com.carmaintenance.tracker.database.ServiceEntity service = serviceDao.getServiceById(reminder.serviceId);
            if (service == null || !service.enabled) {
                return;
            }
            
            // Calculate next trigger time
            long triggerMillis = computeNextTriggerMillis(
                System.currentTimeMillis(),
                reminder.hour,
                reminder.minute,
                reminder.daysMask
            );
            
            // Create PendingIntent for broadcast
            Intent intent = new Intent(context, ReminderReceiver.class);
            intent.putExtra("reminderId", reminderId);
            
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                (int) reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            
            // Schedule alarm (this needs to run on any thread, AlarmManager is thread-safe)
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            if (alarmManager != null) {
                // Check exact alarm permission for Android 12+ (API 31+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (!alarmManager.canScheduleExactAlarms()) {
                        // Permission not granted - use inexact alarm as fallback
                        // Note: This will be less precise but will still work
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerMillis,
                            pendingIntent
                        );
                        return; // Exit early - inexact alarm scheduled
                    }
                }
                
                // Schedule exact alarm
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerMillis,
                        pendingIntent
                    );
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent);
                }
            }
        });
    }
    
    /**
     * Cancels a reminder alarm.
     */
    public static void cancelReminder(Context context, long reminderId) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            (int) reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
    
    /**
     * Schedules all enabled reminders from the database.
     * This method runs database operations on a background thread.
     */
    public static void scheduleAllEnabled(Context context) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            ReminderDao reminderDao = db.reminderDao();
            
            List<ReminderEntity> enabledReminders = reminderDao.getEnabledReminders();
            com.carmaintenance.tracker.database.ServiceDao serviceDao = db.serviceDao();
            
            for (ReminderEntity reminder : enabledReminders) {
                com.carmaintenance.tracker.database.ServiceEntity service = serviceDao.getServiceById(reminder.serviceId);
                if (service != null && service.enabled) {
                    scheduleReminder(context, reminder.id);
                }
            }
        });
    }
    
    /**
     * Cancels all reminders for a specific service.
     * This method runs database operations on a background thread.
     */
    public static void cancelAllForService(Context context, long serviceId) {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(context);
            ReminderDao reminderDao = db.reminderDao();
            
            List<ReminderEntity> reminders = reminderDao.getRemindersForService(serviceId);
            for (ReminderEntity reminder : reminders) {
                cancelReminder(context, reminder.id);
            }
        });
    }
    
    /**
     * Computes the next trigger time in milliseconds (RTC) based on:
     * - Current time
     * - Desired hour and minute
     * - Days mask (bitmask for allowed weekdays)
     * 
     * Days mask bits: Sun=1, Mon=2, Tue=4, Wed=8, Thu=16, Fri=32, Sat=64
     * 
     * Returns the next time that matches the criteria.
     */
    public static long computeNextTriggerMillis(long now, int hour, int minute, int daysMask) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(now);
        
        // Set target hour and minute
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        // If the time has already passed today, move to tomorrow
        if (cal.getTimeInMillis() <= now) {
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        // Find the next allowed weekday
        int maxIterations = 8; // Safety limit
        int iterations = 0;
        
        while (iterations < maxIterations) {
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int dayBit = dayOfWeekToBit(dayOfWeek);
            
            // Check if this day is allowed in the mask
            if ((daysMask & dayBit) != 0) {
                return cal.getTimeInMillis();
            }
            
            // Move to next day
            cal.add(Calendar.DAY_OF_MONTH, 1);
            iterations++;
        }
        
        // Fallback: return tomorrow at the specified time
        cal.setTimeInMillis(now);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }
    
    /**
     * Converts Calendar.DAY_OF_WEEK to our bitmask format.
     * Calendar: SUNDAY=1, MONDAY=2, ..., SATURDAY=7
     * Our bits: Sun=1, Mon=2, Tue=4, Wed=8, Thu=16, Fri=32, Sat=64
     */
    private static int dayOfWeekToBit(int calendarDayOfWeek) {
        switch (calendarDayOfWeek) {
            case Calendar.SUNDAY: return 1;
            case Calendar.MONDAY: return 2;
            case Calendar.TUESDAY: return 4;
            case Calendar.WEDNESDAY: return 8;
            case Calendar.THURSDAY: return 16;
            case Calendar.FRIDAY: return 32;
            case Calendar.SATURDAY: return 64;
            default: return 1;
        }
    }
}

