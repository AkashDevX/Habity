package com.lifetracker.habits;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifetracker.habits.viewmodel.HabitViewModel;
import com.trackapp.habity.R;

public class ProgressActivity extends AppCompatActivity {
    private HabitViewModel habitViewModel;
    private TextView textViewTotalDays;
    private TextView textViewCurrentStreak;
    private TextView textViewLongestStreak;
    private TextView textViewCompletionRate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
        
        // Initialize ViewModel
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        
        // Initialize views
        textViewTotalDays = findViewById(R.id.textViewTotalDays);
        textViewCurrentStreak = findViewById(R.id.textViewCurrentStreak);
        textViewLongestStreak = findViewById(R.id.textViewLongestStreak);
        textViewCompletionRate = findViewById(R.id.textViewCompletionRate);
        
        // Observe habits and calculate progress
        habitViewModel.getAllHabits().observe(this, habits -> {
            if (habits != null && !habits.isEmpty()) {
                updateProgressStats(habits.size());
            } else {
                textViewTotalDays.setText("0");
                textViewCurrentStreak.setText("0");
                textViewLongestStreak.setText("0");
                textViewCompletionRate.setText("0%");
            }
        });
    }
    
    private void updateProgressStats(int habitCount) {
        // Calculate and display progress statistics
        // This is a placeholder - you can enhance this with actual calculations
        textViewTotalDays.setText(String.valueOf(habitCount * 7)); // Example calculation
        textViewCurrentStreak.setText("0"); // TODO: Calculate from completion data
        textViewLongestStreak.setText("0"); // TODO: Calculate from completion data
        textViewCompletionRate.setText("0%"); // TODO: Calculate from completion data
    }
}

