package com.lifetracker.habits;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lifetracker.habits.adapter.HabitAdapter;
import com.lifetracker.habits.reminders.NotificationUtil;
import com.lifetracker.habits.reminders.ReminderScheduler;
import com.lifetracker.habits.viewmodel.HabitViewModel;
import com.trackapp.habity.R;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerViewHabits;
    private HabitAdapter habitAdapter;
    private HabitViewModel habitViewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Create notification channel
        NotificationUtil.createNotificationChannel(this);
        
        // Request notification permission for Android 13+
        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, 
                        "Notification permission denied. Notifications may not work.", 
                        Toast.LENGTH_SHORT).show();
                }
            }
        );
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
        
        // Initialize ViewModel
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        
        // Setup RecyclerView
        recyclerViewHabits = findViewById(R.id.recyclerViewHabits);
        habitAdapter = new HabitAdapter(habitViewModel, this);
        habitAdapter.setOnRemindersClickListener(habitId -> {
            Intent intent = new Intent(MainActivity.this, HabitRemindersActivity.class);
            intent.putExtra("habitId", habitId);
            startActivity(intent);
        });
        
        recyclerViewHabits.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHabits.setAdapter(habitAdapter);
        
        // Observe habits
        habitViewModel.getAllHabits().observe(this, habits -> {
            habitAdapter.setHabits(habits);
        });
        
        // Setup FAB
        FloatingActionButton fabAddHabit = findViewById(R.id.fabAddHabit);
        fabAddHabit.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddEditHabitActivity.class);
            startActivity(intent);
        });
        
        // Reschedule all enabled reminders on app start
        ReminderScheduler.scheduleAllEnabled(this);
    }
}

