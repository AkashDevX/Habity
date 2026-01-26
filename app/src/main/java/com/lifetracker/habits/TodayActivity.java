package com.lifetracker.habits;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifetracker.habits.adapter.HabitAdapter;
import com.lifetracker.habits.viewmodel.HabitViewModel;

public class TodayActivity extends AppCompatActivity {
    private RecyclerView recyclerViewHabits;
    private HabitAdapter habitAdapter;
    private HabitViewModel habitViewModel;
    private View emptyStateView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_today);
        
        // Override transition
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Today");
        }
        
        // Initialize ViewModel
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        
        // Setup RecyclerView with animation
        recyclerViewHabits = findViewById(R.id.recyclerViewHabits);
        emptyStateView = findViewById(R.id.emptyStateView);
        habitAdapter = new HabitAdapter(habitViewModel, this);
        habitAdapter.setOnRemindersClickListener(habitId -> {
            // Open edit habit screen (which includes inline reminders)
            Intent intent = new Intent(TodayActivity.this, AddEditHabitActivity.class);
            intent.putExtra("habitId", habitId);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        });
        
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewHabits.setLayoutManager(layoutManager);
        recyclerViewHabits.setAdapter(habitAdapter);
        
        // Observe habits for today only
        habitViewModel.getHabitsForToday().observe(this, habits -> {
            if (habits == null || habits.isEmpty()) {
                // Show empty state
                recyclerViewHabits.setVisibility(View.GONE);
                emptyStateView.setVisibility(View.VISIBLE);
            } else {
                // Show habits list
                recyclerViewHabits.setVisibility(View.VISIBLE);
                emptyStateView.setVisibility(View.GONE);
                habitAdapter.setHabits(habits);
                // Animate items
                animateRecyclerViewItems();
            }
        });
    }
    
    private void animateRecyclerViewItems() {
        for (int i = 0; i < recyclerViewHabits.getChildCount(); i++) {
            View child = recyclerViewHabits.getChildAt(i);
            child.setAlpha(0f);
            child.setTranslationY(30f);
            child.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(400)
                .setStartDelay(i * 100)
                .start();
        }
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_in_right);
        return true;
    }
}

