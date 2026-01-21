package com.trackapp.habity.reminders;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.trackapp.habity.MainActivity;
import com.trackapp.habity.R;

public class NotificationUtil {
    private static final String CHANNEL_ID = "habit_reminders";
    private static final String CHANNEL_NAME = "Habit Reminders";
    private static final int NOTIFICATION_ID_BASE = 1000;
    
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Notifications for habit reminders");
            
            NotificationManager notificationManager = 
                context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    public static void showNotification(Context context, long reminderId, String habitTitle) {
        createNotificationChannel(context);
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            (int) reminderId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time for: " + habitTitle)
            .setContentText("✅")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
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

