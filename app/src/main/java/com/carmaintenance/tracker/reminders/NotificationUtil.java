package com.carmaintenance.tracker.reminders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.carmaintenance.tracker.DashboardActivity;
import com.carmaintenance.tracker.R;

public class NotificationUtil {
    private static final String CHANNEL_ID = "service_reminders";
    private static final String CHANNEL_NAME = "Service Reminders";
    private static final int NOTIFICATION_ID_BASE = 1000;
    
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH  // Changed to HIGH for heads-up notifications
            );
            channel.setDescription("Notifications for car service reminders");
            channel.enableVibration(true);
            channel.enableLights(true);
            // Set sound to default notification sound
            channel.setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI, null);
            
            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    public static void showNotification(Context context, long reminderId, String serviceTitle) {
        createNotificationChannel(context);
        
        Intent intent = new Intent(context, DashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            (int) reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Service Reminder: " + serviceTitle)
            .setContentText("Time for car service or maintenance")
            .setPriority(NotificationCompat.PRIORITY_HIGH)  // Changed to HIGH for heads-up
            .setDefaults(NotificationCompat.DEFAULT_ALL)  // Sound, vibration, lights
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        
        NotificationManager notificationManager = 
            context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            try {
                notificationManager.notify((int) reminderId, builder.build());
            } catch (SecurityException e) {
                // User denied notification permission
                e.printStackTrace();
            }
        }
    }
}

