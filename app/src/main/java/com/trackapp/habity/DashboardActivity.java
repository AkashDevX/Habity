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
        
        // Animate tiles on load
        animateTiles();
        
        // Setup tile click handlers with animations
        CardView tileToday = findViewById(R.id.tileToday);
        CardView tileCalendar = findViewById(R.id.tileCalendar);
        CardView tileStats = findViewById(R.id.tileStats);
        CardView tileAbout = findViewById(R.id.tileAbout);
        CardView tileAddHabit = findViewById(R.id.tileAddHabit);
        
        tileToday.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, TodayActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }).start();
        });
        
        tileCalendar.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, CalendarActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }).start();
        });
        
        tileStats.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, StatsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }).start();
        });
        
        tileAbout.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, AboutActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }).start();
        });
        
        tileAddHabit.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, AddEditHabitActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }).start();
        });
    }
    
    private void animateTiles() {
        View headerSection = findViewById(R.id.headerSection);
        View row1 = findViewById(R.id.row1);
        View row2 = findViewById(R.id.row2);
        View tileAddHabit = findViewById(R.id.tileAddHabit);
        
        // Fade in header
        headerSection.setAlpha(0f);
        headerSection.animate().alpha(1f).setDuration(600).start();
        
        // Slide in row 1
        row1.setTranslationY(50f);
        row1.setAlpha(0f);
        row1.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(200)
            .start();
        
        // Slide in row 2
        row2.setTranslationY(50f);
        row2.setAlpha(0f);
        row2.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(400)
            .start();
        
        // Scale in add habit tile
        tileAddHabit.setScaleX(0.8f);
        tileAddHabit.setScaleY(0.8f);
        tileAddHabit.setAlpha(0f);
        tileAddHabit.animate()
            .scaleX(1f)
            .scaleY(1f)
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(600)
            .start();
    }
}

