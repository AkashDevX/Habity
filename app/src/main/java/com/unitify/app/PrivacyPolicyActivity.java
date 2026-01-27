package com.unitify.app;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class PrivacyPolicyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);
        
        setupToolbar();
    }
    
    private void setupToolbar() {
        try {
            ImageButton backButton = findViewById(R.id.backButton);
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

