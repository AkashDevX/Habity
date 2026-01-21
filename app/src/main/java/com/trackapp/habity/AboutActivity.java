package com.trackapp.habity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
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
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

