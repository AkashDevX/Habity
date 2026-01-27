package com.carmaintenance.tracker;

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
import com.google.android.material.card.MaterialCardView;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;

import com.carmaintenance.tracker.reminders.NotificationUtil;
import com.carmaintenance.tracker.reminders.ReminderScheduler;


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
        MaterialCardView tileServiceHistory = findViewById(R.id.tileServiceHistory);
        MaterialCardView tileFuelLog = findViewById(R.id.tileFuelLog);
        MaterialCardView tileReminders = findViewById(R.id.tileReminders);
        MaterialCardView tileStats = findViewById(R.id.tileStats);
        MaterialCardView tileAbout = findViewById(R.id.tileAbout);
        
        tileServiceHistory.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, ServiceHistoryActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }).start();
        });
        
        tileFuelLog.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, FuelLogActivity.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                }).start();
        });
        
        tileReminders.setOnClickListener(v -> {
            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> {
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    Intent intent = new Intent(DashboardActivity.this, RemindersActivity.class);
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
        
//        tileAddService.setOnClickListener(v -> {
//            v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
//                .withEndAction(() -> {
//                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
//                    Intent intent = new Intent(DashboardActivity.this, AddEditServiceActivity.class);
//                    startActivity(intent);
//                    overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
//                }).start();
//        });
    }
    
    private void animateTiles() {
        View headerSection = findViewById(R.id.headerSection);
        View mainContent = findViewById(R.id.mainContent);
        
        // Fade in header
        headerSection.setAlpha(0f);
        headerSection.animate().alpha(1f).setDuration(600).start();
        
        // Slide in main content
        mainContent.setTranslationY(50f);
        mainContent.setAlpha(0f);
        mainContent.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(500)
            .setStartDelay(200)
            .start();
    }
}

