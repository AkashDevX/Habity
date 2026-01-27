package com.unitify.app;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
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

