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

public class PinSetupActivity extends AppCompatActivity {
    private EditText pinEditText;
    private EditText confirmPinEditText;
    private Button saveButton;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_setup);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Set Up PIN");
            getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.gradient_actionbar));
        }
        
        pinEditText = findViewById(R.id.pinEditText);
        confirmPinEditText = findViewById(R.id.confirmPinEditText);
        saveButton = findViewById(R.id.saveButton);
        
        saveButton.setOnClickListener(v -> {
            String pin = pinEditText.getText().toString();
            String confirmPin = confirmPinEditText.getText().toString();
            
            if (TextUtils.isEmpty(pin) || pin.length() < 4) {
                Toast.makeText(this, "PIN must be at least 4 digits", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (!pin.equals(confirmPin)) {
                Toast.makeText(this, "PINs do not match", Toast.LENGTH_SHORT).show();
                return;
            }
            
            PinManager.setPin(pin, this);
            Toast.makeText(this, "PIN set successfully", Toast.LENGTH_SHORT).show();
            
            Intent intent = new Intent(this, PasswordListActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
