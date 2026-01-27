package com.cleverkube.watermark;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class AboutActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About");
        }
        
        TextView textViewVersion = findViewById(R.id.textViewVersion);
        textViewVersion.setText("Version 3.0");
        
        CardView cardPrivacy = findViewById(R.id.cardPrivacy);
        cardPrivacy.setOnClickListener(v -> {
            Intent intent = new Intent(AboutActivity.this, PrivacyPolicyActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_in_right);
        return true;
    }
}


