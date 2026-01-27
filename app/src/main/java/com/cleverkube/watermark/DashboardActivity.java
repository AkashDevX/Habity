package com.cleverkube.watermark;

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

import com.cleverkube.watermark.reminders.NotificationUtil;
import com.cleverkube.watermark.reminders.ReminderScheduler;

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
        try {
            ReminderScheduler.scheduleAllEnabled(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Animate tiles on load
        animateTiles();
        
        // Setup tile click handlers with animations
        CardView tileHome = findViewById(R.id.tileToday);
        CardView tileHistory = findViewById(R.id.tileCalendar);
        CardView tileStats = findViewById(R.id.tileStats);
        CardView tileSettings = findViewById(R.id.tileSettings);
        CardView tileAbout = findViewById(R.id.tileAbout);
        
        tileHome.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }).start();
        });
        
        tileHistory.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
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
        
        tileSettings.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
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
        
        // Add achievements badge to Stats tile if there are new achievements
        checkNewAchievements();
    }
    
    private void checkNewAchievements() {
        new Thread(() -> {
            try {
                com.cleverkube.watermark.database.AppDatabase db = 
                    com.cleverkube.watermark.database.AppDatabase.getDatabase(this);
                if (db != null) {
                    int unlocked = db.achievementDao().getUnlockedCountSync();
                    // Could add a badge indicator here if needed
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    private void animateTiles() {
        View headerSection = findViewById(R.id.headerSection);
        View row1 = findViewById(R.id.row1);
        View row2 = findViewById(R.id.row2);
        
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
    }
}

