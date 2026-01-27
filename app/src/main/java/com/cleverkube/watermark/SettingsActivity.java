package com.cleverkube.watermark;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.cleverkube.watermark.database.AppDatabase;
import com.cleverkube.watermark.database.ReminderConfig;
import com.cleverkube.watermark.database.ReminderConfigDao;
import com.cleverkube.watermark.database.WaterEntry;
import com.cleverkube.watermark.database.WaterDao;
import com.cleverkube.watermark.reminders.ReminderScheduler;
import com.cleverkube.watermark.utils.SettingsHelper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SettingsActivity extends AppCompatActivity {
    private SettingsHelper settingsHelper;
    private ReminderConfigDao reminderConfigDao;
    private ReminderConfig reminderConfig;
    private ExecutorService executor;
    
    private TextView textViewDailyGoal;
    private TextView textViewUnit;
    private Switch switchReminders;
    private TextView textViewStartTime;
    private TextView textViewEndTime;
    private TextView textViewInterval;
    
    private ActivityResultLauncher<Intent> exportLauncher;
    private ActivityResultLauncher<Intent> importLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }
        
        settingsHelper = new SettingsHelper(this);
        AppDatabase db = AppDatabase.getDatabase(this);
        reminderConfigDao = db.reminderConfigDao();
        executor = Executors.newSingleThreadExecutor();
        
        initializeViews();
        setupLaunchers();
        loadSettings();
    }
    
    private void initializeViews() {
        textViewDailyGoal = findViewById(R.id.textViewDailyGoal);
        textViewUnit = findViewById(R.id.textViewUnit);
        switchReminders = findViewById(R.id.switchReminders);
        textViewStartTime = findViewById(R.id.textViewStartTime);
        textViewEndTime = findViewById(R.id.textViewEndTime);
        textViewInterval = findViewById(R.id.textViewInterval);
        
        findViewById(R.id.cardDailyGoal).setOnClickListener(v -> showDailyGoalDialog());
        findViewById(R.id.cardUnit).setOnClickListener(v -> showUnitDialog());
        findViewById(R.id.cardStartTime).setOnClickListener(v -> showStartTimePicker());
        findViewById(R.id.cardEndTime).setOnClickListener(v -> showEndTimePicker());
        findViewById(R.id.cardInterval).setOnClickListener(v -> showIntervalDialog());
        findViewById(R.id.cardExport).setOnClickListener(v -> exportData());
        findViewById(R.id.cardImport).setOnClickListener(v -> importData());
        findViewById(R.id.cardAchievements).setOnClickListener(v -> {
            Intent intent = new Intent(SettingsActivity.this, AchievementsActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        });
        
        switchReminders.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (reminderConfig != null) {
                reminderConfig.enabled = isChecked;
                saveReminderConfig();
            }
        });
    }
    
    private void setupLaunchers() {
        exportLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        executor.execute(() -> writeExportData(uri));
                    }
                }
            }
        );
        
        importLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        executor.execute(() -> readImportData(uri));
                    }
                }
            }
        );
    }
    
    private void loadSettings() {
        int goal = settingsHelper.getDailyGoalMl();
        String unit = settingsHelper.getUnit();
        textViewDailyGoal.setText(settingsHelper.formatAmount(this, goal));
        textViewUnit.setText(unit.toUpperCase());
        
        executor.execute(() -> {
            reminderConfig = reminderConfigDao.getConfigSync();
            if (reminderConfig == null) {
                reminderConfig = new ReminderConfig();
                reminderConfigDao.insert(reminderConfig);
            }
            
            runOnUiThread(() -> {
                switchReminders.setChecked(reminderConfig.enabled);
                updateReminderViews();
            });
        });
    }
    
    private void updateReminderViews() {
        textViewStartTime.setText(String.format(Locale.getDefault(), "%02d:%02d", 
            reminderConfig.startHour, reminderConfig.startMinute));
        textViewEndTime.setText(String.format(Locale.getDefault(), "%02d:%02d", 
            reminderConfig.endHour, reminderConfig.endMinute));
        textViewInterval.setText(String.format(Locale.getDefault(), "Every %d hours", 
            reminderConfig.intervalHours));
    }
    
    private void showDailyGoalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Daily Goal");
        
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Enter goal in ml");
        input.setText(String.valueOf(settingsHelper.getDailyGoalMl()));
        builder.setView(input);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                int goal = Integer.parseInt(input.getText().toString());
                if (goal > 0) {
                    settingsHelper.setDailyGoalMl(goal);
                    textViewDailyGoal.setText(settingsHelper.formatAmount(this, goal));
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showUnitDialog() {
        String currentUnit = settingsHelper.getUnit();
        String[] units = {"ml", "oz"};
        int selected = currentUnit.equals("oz") ? 1 : 0;
        
        new AlertDialog.Builder(this)
            .setTitle("Units")
            .setSingleChoiceItems(units, selected, (dialog, which) -> {
                settingsHelper.setUnit(units[which]);
                textViewUnit.setText(units[which].toUpperCase());
                int goal = settingsHelper.getDailyGoalMl();
                textViewDailyGoal.setText(settingsHelper.formatAmount(this, goal));
                dialog.dismiss();
            })
            .show();
    }
    
    private void showStartTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            reminderConfig.startHour = hourOfDay;
            reminderConfig.startMinute = minute;
            saveReminderConfig();
        }, reminderConfig.startHour, reminderConfig.startMinute, true);
        dialog.show();
    }
    
    private void showEndTimePicker() {
        TimePickerDialog dialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            reminderConfig.endHour = hourOfDay;
            reminderConfig.endMinute = minute;
            saveReminderConfig();
        }, reminderConfig.endHour, reminderConfig.endMinute, true);
        dialog.show();
    }
    
    private void showIntervalDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reminder Interval");
        
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("Hours");
        input.setText(String.valueOf(reminderConfig.intervalHours));
        builder.setView(input);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                int interval = Integer.parseInt(input.getText().toString());
                if (interval > 0 && interval <= 24) {
                    reminderConfig.intervalHours = interval;
                    saveReminderConfig();
                } else {
                    Toast.makeText(this, "Interval must be between 1 and 24 hours", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void saveReminderConfig() {
        executor.execute(() -> {
            reminderConfigDao.update(reminderConfig);
            runOnUiThread(() -> {
                updateReminderViews();
                if (reminderConfig.enabled) {
                    ReminderScheduler.scheduleAllEnabled(this);
                } else {
                    ReminderScheduler.cancelAllReminders(this);
                }
            });
        });
    }
    
    private void exportData() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "watermark_backup_" + System.currentTimeMillis() + ".json");
        exportLauncher.launch(intent);
    }
    
    private void writeExportData(Uri uri) {
        try {
            OutputStream outputStream = getContentResolver().openOutputStream(uri);
            if (outputStream != null) {
                AppDatabase db = AppDatabase.getDatabase(this);
                WaterDao waterDao = db.waterDao();
                List<WaterEntry> entries = waterDao.getRecentEntries(10000);
                
                Gson gson = new Gson();
                String json = gson.toJson(entries);
                
                OutputStreamWriter writer = new OutputStreamWriter(outputStream);
                writer.write(json);
                writer.close();
                
                runOnUiThread(() -> Toast.makeText(this, "Data exported successfully", Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Export failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    
    private void importData() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        importLauncher.launch(intent);
    }
    
    private void readImportData(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
                reader.close();
                
                Gson gson = new Gson();
                Type listType = new TypeToken<List<WaterEntry>>(){}.getType();
                List<WaterEntry> entries = gson.fromJson(json.toString(), listType);
                
                AppDatabase db = AppDatabase.getDatabase(this);
                WaterDao waterDao = db.waterDao();
                for (WaterEntry entry : entries) {
                    waterDao.insert(entry);
                }
                
                runOnUiThread(() -> Toast.makeText(this, "Data imported successfully", Toast.LENGTH_SHORT).show());
            }
        } catch (Exception e) {
            runOnUiThread(() -> Toast.makeText(this, "Import failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_in_right);
        return true;
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}

