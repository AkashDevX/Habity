package com.passwordmanager.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;

import com.passwordmanager.app.util.PinManager;

public class PinLockActivity extends AppCompatActivity {
    private EditText pinEditText;
    private Button unlockButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_lock);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Enter PIN");
            getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.gradient_actionbar));
        }
        
        pinEditText = findViewById(R.id.pinEditText);
        unlockButton = findViewById(R.id.unlockButton);
        
        unlockButton.setOnClickListener(v -> {
            String pin = pinEditText.getText().toString();
            
            if (TextUtils.isEmpty(pin)) {
                Toast.makeText(this, "Please enter PIN", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (PinManager.verifyPin(pin, this)) {
                Intent intent = new Intent(this, PasswordListActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Incorrect PIN", Toast.LENGTH_SHORT).show();
                pinEditText.setText("");
            }
        });
    }
}
