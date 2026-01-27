package com.unitify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DashboardActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        // Animate tiles on load
        animateTiles();
        
        // Setup tile click handlers with animations
        CardView tileConverter = findViewById(R.id.tileConverter);
        CardView tileFavorites = findViewById(R.id.tileFavorites);
        CardView tileHistory = findViewById(R.id.tileHistory);
        CardView tileSettings = findViewById(R.id.tileSettings);
        CardView tileAbout = findViewById(R.id.tileAbout);
        CardView tilePrivacy = findViewById(R.id.tilePrivacy);
        
        if (tileConverter == null || tileFavorites == null || tileHistory == null || tileSettings == null) {
            return; // Can't set up click handlers if views are null
        }
        
        tileConverter.setOnClickListener(v -> {
            try {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() -> {
                        try {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            Intent intent = new Intent(DashboardActivity.this, ConverterActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent(DashboardActivity.this, ConverterActivity.class);
                startActivity(intent);
            }
        });
        
        tileFavorites.setOnClickListener(v -> {
            try {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() -> {
                        try {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            Intent intent = new Intent(DashboardActivity.this, FavoritesActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent(DashboardActivity.this, FavoritesActivity.class);
                startActivity(intent);
            }
        });
        
        tileHistory.setOnClickListener(v -> {
            try {
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                    .withEndAction(() -> {
                        try {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();
            } catch (Exception e) {
                e.printStackTrace();
                Intent intent = new Intent(DashboardActivity.this, HistoryActivity.class);
                startActivity(intent);
            }
        });
        
        if (tileSettings != null) {
            tileSettings.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    // Try again without animation
                    try {
                        Intent intent = new Intent(DashboardActivity.this, SettingsActivity.class);
                        startActivity(intent);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
        
        if (tileAbout != null) {
            tileAbout.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(DashboardActivity.this, AboutActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        
        if (tilePrivacy != null) {
            tilePrivacy.setOnClickListener(v -> {
                try {
                    Intent intent = new Intent(DashboardActivity.this, PrivacyPolicyActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
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
        
        // Slide in row 3
        View row3 = findViewById(R.id.row3);
        if (row3 != null) {
            row3.setTranslationY(50f);
            row3.setAlpha(0f);
            row3.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(600)
                .start();
        }
    }
}

