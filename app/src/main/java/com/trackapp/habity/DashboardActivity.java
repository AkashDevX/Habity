package com.trackapp.habity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.trackapp.habity.reminders.NotificationUtil;
import com.trackapp.habity.reminders.ReminderScheduler;

public class DashboardActivity extends AppCompatActivity {
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        // Create notification channel
        NotificationUtil.createNotificationChannel(this);
        
        // Request notification permission for Android 13+
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, 
                        "Notification permission denied. Notifications may not work.", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        
        // Reschedule all enabled reminders on app start
        ReminderScheduler.scheduleAllEnabled(this);
        
        // Setup tile click handlers
        CardView tileToday = findViewById(R.id.tileToday);
        CardView tileCalendar = findViewById(R.id.tileCalendar);
        CardView tileStats = findViewById(R.id.tileStats);
        CardView tileAbout = findViewById(R.id.tileAbout);
        CardView tileAddHabit = findViewById(R.id.tileAddHabit);
        
        tileToday.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, TodayActivity.class);
            startActivity(intent);
        });
        
        tileCalendar.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, CalendarActivity.class);
            startActivity(intent);
        });
        
        tileStats.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, StatsActivity.class);
            startActivity(intent);
        });
        
        tileAbout.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AboutActivity.class);
            startActivity(intent);
        });
        
        tileAddHabit.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddEditHabitActivity.class);
            startActivity(intent);
        });
    }
}

