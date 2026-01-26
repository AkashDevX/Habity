package com.lifetracker.habits;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lifetracker.habits.adapter.ReminderAdapter;
import com.lifetracker.habits.database.AppDatabase;
import com.lifetracker.habits.database.HabitDao;
import com.lifetracker.habits.database.HabitEntity;
import com.lifetracker.habits.viewmodel.ReminderViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitRemindersActivity extends AppCompatActivity {
    private TextView textViewHabitTitle;
    private RecyclerView recyclerViewReminders;
    private ReminderAdapter reminderAdapter;
    private ReminderViewModel reminderViewModel;
    private long habitId;
    private ExecutorService executor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_habit_reminders);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reminders");
        }
        
        habitId = getIntent().getLongExtra("habitId", -1);
        if (habitId == -1) {
            finish();
            return;
        }
        
        executor = Executors.newSingleThreadExecutor();
        
        textViewHabitTitle = findViewById(R.id.textViewHabitTitle);
        recyclerViewReminders = findViewById(R.id.recyclerViewReminders);
        
        reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);
        reminderAdapter = new ReminderAdapter(reminderViewModel, this);
        reminderAdapter.setOnReminderDeletedListener(() -> loadReminders());
        
        recyclerViewReminders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReminders.setAdapter(reminderAdapter);
        
        // Load habit title
        loadHabitTitle();
        
        // Load reminders
        loadReminders();
        
        // Setup FAB
        FloatingActionButton fabAddReminder = findViewById(R.id.fabAddReminder);
        fabAddReminder.setOnClickListener(v -> showAddReminderDialog());
    }
    
    private void loadHabitTitle() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            HabitDao habitDao = db.habitDao();
            HabitEntity habit = habitDao.getHabitById(habitId);
            
            runOnUiThread(() -> {
                if (habit != null) {
                    textViewHabitTitle.setText(habit.title);
                }
            });
        });
    }
    
    private void loadReminders() {
        reminderViewModel.getRemindersForHabit(habitId, reminders -> {
            reminderAdapter.setReminders(reminders);
        });
    }
    
    private void showAddReminderDialog() {
        AddReminderDialog dialog = AddReminderDialog.newInstance((hour, minute, daysMask) -> {
            // Set date to today's date
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            String today = dateFormat.format(new java.util.Date());
            com.lifetracker.habits.database.ReminderEntity reminder = 
                new com.lifetracker.habits.database.ReminderEntity(habitId, today, hour, minute, daysMask, true);
            reminderViewModel.insertReminder(reminder, insertedReminder -> {
                // Schedule the reminder after insertion
                com.lifetracker.habits.reminders.ReminderScheduler.scheduleReminder(
                    this, insertedReminder.id);
                // Reload reminders list
                loadReminders();
            });
        });
        
        dialog.show(getSupportFragmentManager(), "AddReminderDialog");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
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

