package com.lifetracker.habits;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AboutActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        // Override transition
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About");
        }
        
        TextView textViewVersion = findViewById(R.id.textViewVersion);
        try {
            String versionName = getPackageManager()
                .getPackageInfo(getPackageName(), 0).versionName;
            textViewVersion.setText("Version " + versionName);
        } catch (Exception e) {
            textViewVersion.setText("Version 1.0");
        }
        
        // Setup Privacy Policy card click
        CardView cardPrivacyPolicy = findViewById(R.id.cardPrivacyPolicy);
        if (cardPrivacyPolicy != null) {
            cardPrivacyPolicy.setOnClickListener(v -> {
                Intent intent = new Intent(AboutActivity.this, PrivacyPolicyActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            });
        }
        
        // Animate cards
        animateCards();
    }
    
    private void animateCards() {
        // Find all CardViews and animate them
        android.view.ViewGroup root = findViewById(android.R.id.content);
        animateCardViews(root);
    }
    
    private void animateCardViews(android.view.ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child instanceof androidx.cardview.widget.CardView) {
                child.setAlpha(0f);
                child.setTranslationY(30f);
                child.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(i * 200)
                    .start();
            } else if (child instanceof android.view.ViewGroup) {
                animateCardViews((android.view.ViewGroup) child);
            }
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_in_right);
        return true;
    }
}

