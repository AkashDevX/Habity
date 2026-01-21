package com.trackapp.habity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.trackapp.habity.adapter.ReminderInlineAdapter;
import com.trackapp.habity.database.AppDatabase;
import com.trackapp.habity.database.HabitEntity;
import com.trackapp.habity.database.ReminderDao;
import com.trackapp.habity.database.ReminderEntity;
import com.trackapp.habity.reminders.ReminderScheduler;
import com.trackapp.habity.viewmodel.HabitViewModel;
import com.trackapp.habity.viewmodel.ReminderViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditHabitActivity extends AppCompatActivity {
    private TextInputEditText editTextTitle;
    private Switch switchHabitEnabled;
    private Button buttonAddReminder;
    private Button buttonSave;
    private RecyclerView recyclerViewReminders;
    
    private HabitViewModel habitViewModel;
    private ReminderViewModel reminderViewModel;
    private ReminderInlineAdapter reminderAdapter;
    private ExecutorService executor;
    
    private long habitId = -1;
    private boolean isEditMode = false;
    private List<ReminderEntity> originalReminders = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_habit_new);
        
        // Override transition
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        executor = Executors.newSingleThreadExecutor();
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);
        
        editTextTitle = findViewById(R.id.editTextTitle);
        switchHabitEnabled = findViewById(R.id.switchHabitEnabled);
        buttonAddReminder = findViewById(R.id.buttonAddReminder);
        buttonSave = findViewById(R.id.buttonSave);
        recyclerViewReminders = findViewById(R.id.recyclerViewReminders);
        
        // Setup RecyclerView for reminders
        reminderAdapter = new ReminderInlineAdapter();
        reminderAdapter.setOnReminderActionListener(new ReminderInlineAdapter.OnReminderActionListener() {
            @Override
            public void onReminderEnabledChanged(ReminderEntity reminder, boolean enabled) {
                reminder.enabled = enabled;
            }
            
            @Override
            public void onReminderEdit(ReminderEntity reminder, int position) {
                showEditReminderDialog(reminder, position);
            }
            
            @Override
            public void onReminderDelete(ReminderEntity reminder, int position) {
                reminderAdapter.removeReminder(position);
            }
        });
        
        recyclerViewReminders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReminders.setAdapter(reminderAdapter);
        
        // Check if editing existing habit
        if (getIntent().hasExtra("habitId")) {
            habitId = getIntent().getLongExtra("habitId", -1);
            isEditMode = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Habit");
            }
            loadHabitData();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Habit");
            }
            switchHabitEnabled.setChecked(true);
        }
        
        buttonAddReminder.setOnClickListener(v -> showAddReminderDialog());
        
        buttonSave.setOnClickListener(v -> saveHabit());
        
        // Animate views on load (after all views are initialized)
        animateViews();
    }
    
    private void loadHabitData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            com.trackapp.habity.database.HabitDao habitDao = db.habitDao();
            HabitEntity habit = habitDao.getHabitById(habitId);
            
            if (habit == null) {
                runOnUiThread(() -> finish());
                return;
            }
            
            ReminderDao reminderDao = db.reminderDao();
            List<ReminderEntity> reminders = reminderDao.getRemindersForHabit(habitId);
            
            runOnUiThread(() -> {
                editTextTitle.setText(habit.title);
                switchHabitEnabled.setChecked(habit.enabled);
                
                originalReminders = new ArrayList<>(reminders);
                reminderAdapter.setReminders(reminders);
            });
        });
    }
    
    private void showAddReminderDialog() {
        AddReminderDialog dialog = AddReminderDialog.newInstance((hour, minute, daysMask) -> {
            ReminderEntity reminder = new ReminderEntity();
            reminder.hour = hour;
            reminder.minute = minute;
            reminder.daysMask = daysMask;
            reminder.enabled = true;
            reminderAdapter.addReminder(reminder);
        });
        
        dialog.show(getSupportFragmentManager(), "AddReminderDialog");
    }
    
    private void showEditReminderDialog(ReminderEntity reminder, int position) {
        // Create a new dialog that allows editing time and days with pre-filled values
        AddReminderDialog dialog = AddReminderDialog.newInstanceForEdit(
            reminder.hour, 
            reminder.minute, 
            reminder.daysMask,
            (hour, minute, daysMask) -> {
                reminder.hour = hour;
                reminder.minute = minute;
                reminder.daysMask = daysMask;
                reminderAdapter.updateReminder(position, reminder);
            }
        );
        
        dialog.show(getSupportFragmentManager(), "EditReminderDialog");
    }
    
    private void saveHabit() {
        String title = editTextTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a habit title", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean habitEnabled = switchHabitEnabled.isChecked();
        List<ReminderEntity> currentReminders = reminderAdapter.getReminders();
        
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            com.trackapp.habity.database.HabitDao habitDao = db.habitDao();
            ReminderDao reminderDao = db.reminderDao();
            
            if (isEditMode) {
                // Update existing habit
                HabitEntity habit = habitDao.getHabitById(habitId);
                if (habit != null) {
                    habit.title = title;
                    habit.enabled = habitEnabled;
                    habitDao.update(habit);
                    
                    // Cancel all existing alarms for this habit
                    ReminderScheduler.cancelAllForHabit(this, habitId);
                    
                    // Delete old reminders
                    for (ReminderEntity oldReminder : originalReminders) {
                        reminderDao.delete(oldReminder);
                    }
                    
                    // Insert new reminders
                    for (ReminderEntity reminder : currentReminders) {
                        reminder.habitId = habitId;
                        long reminderId = reminderDao.insert(reminder);
                        reminder.id = reminderId;
                    }
                    
                    // Schedule alarms for enabled reminders if habit is enabled
                    if (habitEnabled) {
                        for (ReminderEntity reminder : currentReminders) {
                            if (reminder.enabled) {
                                ReminderScheduler.scheduleReminder(this, reminder.id);
                            }
                        }
                    }
                }
            } else {
                // Insert new habit
                HabitEntity habit = new HabitEntity(title, habitEnabled);
                long newHabitId = habitDao.insert(habit);
                
                // Insert reminders
                for (ReminderEntity reminder : currentReminders) {
                    reminder.habitId = newHabitId;
                    long reminderId = reminderDao.insert(reminder);
                    reminder.id = reminderId;
                }
                
                // Schedule alarms for enabled reminders if habit is enabled
                if (habitEnabled) {
                    for (ReminderEntity reminder : currentReminders) {
                        if (reminder.enabled) {
                            ReminderScheduler.scheduleReminder(this, reminder.id);
                        }
                    }
                }
            }
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Habit saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_in_right);
        return true;
    }
    
    private void animateViews() {
        editTextTitle.setAlpha(0f);
        editTextTitle.setTranslationY(20f);
        editTextTitle.animate().alpha(1f).translationY(0f).setDuration(400).start();
        
        switchHabitEnabled.setAlpha(0f);
        switchHabitEnabled.animate().alpha(1f).setDuration(400).setStartDelay(100).start();
        
        recyclerViewReminders.setAlpha(0f);
        recyclerViewReminders.animate().alpha(1f).setDuration(400).setStartDelay(200).start();
        
        buttonAddReminder.setAlpha(0f);
        buttonAddReminder.animate().alpha(1f).setDuration(400).setStartDelay(300).start();
        
        buttonSave.setAlpha(0f);
        buttonSave.setScaleX(0.9f);
        buttonSave.setScaleY(0.9f);
        buttonSave.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setStartDelay(400)
            .start();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
