package com.trackapp.habity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trackapp.habity.adapter.HabitAdapter;
import com.trackapp.habity.viewmodel.HabitViewModel;

public class TodayActivity extends AppCompatActivity {
    private RecyclerView recyclerViewHabits;
    private HabitAdapter habitAdapter;
    private HabitViewModel habitViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Today");
        }
        
        // Initialize ViewModel
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        
        // Setup RecyclerView
        recyclerViewHabits = findViewById(R.id.recyclerViewHabits);
        habitAdapter = new HabitAdapter(habitViewModel, this);
        habitAdapter.setOnRemindersClickListener(habitId -> {
            // Open edit habit screen (which includes inline reminders)
            Intent intent = new Intent(TodayActivity.this, AddEditHabitActivity.class);
            intent.putExtra("habitId", habitId);
            startActivity(intent);
        });
        
        recyclerViewHabits.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHabits.setAdapter(habitAdapter);
        
        // Observe habits
        habitViewModel.getAllHabits().observe(this, habits -> {
            habitAdapter.setHabits(habits);
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

