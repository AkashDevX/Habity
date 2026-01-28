package com.passwordmanager.app;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.passwordmanager.app.util.PinManager;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (PinManager.isPinSet(this)) {
            Intent intent = new Intent(this, PinLockActivity.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, PinSetupActivity.class);
            startActivity(intent);
        }
        finish();
    }
}
