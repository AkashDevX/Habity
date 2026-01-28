package com.passwordmanager.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.passwordmanager.app.database.PasswordEntity;
import com.passwordmanager.app.util.EncryptionUtil;
import com.passwordmanager.app.viewmodel.PasswordViewModel;

public class AddEditPasswordActivity extends AppCompatActivity {
    private EditText titleEditText;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private EditText websiteEditText;
    private EditText notesEditText;
    
    private PasswordViewModel viewModel;
    private long passwordId = -1;
    private boolean passwordLoaded = false;
    private PasswordEntity currentPassword;
    private TextView headerTitle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_password);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.gradient_actionbar));
        }
        
        headerTitle = findViewById(R.id.headerTitle);
        titleEditText = findViewById(R.id.titleEditText);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        websiteEditText = findViewById(R.id.websiteEditText);
        notesEditText = findViewById(R.id.notesEditText);
        
        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        
        passwordId = getIntent().getLongExtra("password_id", -1);
        if (passwordId != -1) {
            headerTitle.setText("Edit Password");
            viewModel.getPasswordById(passwordId).observe(this, password -> {
                if (password != null && !passwordLoaded) {
                    passwordLoaded = true;
                    currentPassword = password;
                    titleEditText.setText(password.title);
                    usernameEditText.setText(password.username);
                    String decrypted = EncryptionUtil.decrypt(password.encryptedPassword, this);
                    passwordEditText.setText(decrypted != null ? decrypted : "");
                    websiteEditText.setText(password.website != null ? password.website : "");
                    notesEditText.setText(password.notes != null ? password.notes : "");
                }
            });
        } else {
            headerTitle.setText("Add Password");
        }
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_save) {
            savePassword();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }
    
    private void savePassword() {
        String title = titleEditText.getText().toString().trim();
        String username = usernameEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString();
        String website = websiteEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String encryptedPassword = EncryptionUtil.encrypt(password, this);
        if (encryptedPassword == null) {
            Toast.makeText(this, "Error encrypting password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (passwordId == -1) {
            PasswordEntity passwordEntity = new PasswordEntity(title, username, encryptedPassword, website, notes, null);
            viewModel.insert(passwordEntity);
        } else {
            if (currentPassword != null) {
                currentPassword.title = title;
                currentPassword.username = username;
                currentPassword.encryptedPassword = encryptedPassword;
                currentPassword.website = website;
                currentPassword.notes = notes;
                currentPassword.updatedAt = System.currentTimeMillis();
                viewModel.update(currentPassword);
            }
        }
        
        Toast.makeText(this, "Password saved", Toast.LENGTH_SHORT).show();
        finish();
    }
}
