package com.trackapp.habity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trackapp.habity.database.AppDatabase;
import com.trackapp.habity.database.CompletionDao;
import com.trackapp.habity.database.CompletionEntity;
import com.trackapp.habity.database.HabitDao;
import com.trackapp.habity.database.HabitEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsActivity extends AppCompatActivity {
    private TextView textViewCurrentStreak;
    private TextView textViewLongestStreak;
    private TextView textView7DayCompletion;
    private RecyclerView recyclerViewWeeklyProgress;
    private ExecutorService executor;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Stats");
        }
        
        textViewCurrentStreak = findViewById(R.id.textViewCurrentStreak);
        textViewLongestStreak = findViewById(R.id.textViewLongestStreak);
        textView7DayCompletion = findViewById(R.id.textView7DayCompletion);
        recyclerViewWeeklyProgress = findViewById(R.id.recyclerViewWeeklyProgress);
        
        recyclerViewWeeklyProgress.setLayoutManager(new LinearLayoutManager(this));
        
        executor = Executors.newSingleThreadExecutor();
        loadStats();
    }
    
    private void loadStats() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            HabitDao habitDao = db.habitDao();
            CompletionDao completionDao = db.completionDao();
            
            // Get habits directly from DAO (synchronous query for background thread)
            List<HabitEntity> habits = habitDao.getAllHabitsSync();
            
            // Calculate current streak
            int currentStreak = calculateCurrentStreak(completionDao, habits);
            
            // Calculate longest streak
            int longestStreak = calculateLongestStreak(completionDao, habits);
            
            // Calculate 7-day completion
            double sevenDayCompletion = calculate7DayCompletion(completionDao, habits);
            
            runOnUiThread(() -> {
                textViewCurrentStreak.setText(currentStreak + " days");
                textViewLongestStreak.setText(longestStreak + " days");
                textView7DayCompletion.setText(String.format(Locale.getDefault(), "%.0f%%", sevenDayCompletion));
            });
        });
    }
    
    private int calculateCurrentStreak(CompletionDao completionDao, List<HabitEntity> habits) {
        if (habits.isEmpty()) return 0;
        
        Calendar cal = Calendar.getInstance();
        int streak = 0;
        boolean continueStreak = true;
        
        while (continueStreak) {
            String dateStr = dateFormat.format(cal.getTime());
            boolean allDone = true;
            
            for (HabitEntity habit : habits) {
                CompletionEntity completion = completionDao.getCompletionForDate(dateStr, habit.id);
                if (completion == null || !completion.done) {
                    allDone = false;
                    break;
                }
            }
            
            if (allDone) {
                streak++;
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                continueStreak = false;
            }
        }
        
        return streak;
    }
    
    private int calculateLongestStreak(CompletionDao completionDao, List<HabitEntity> habits) {
        // Simplified - would need to scan all dates
        return 0; // TODO: Implement full streak calculation
    }
    
    private double calculate7DayCompletion(CompletionDao completionDao, List<HabitEntity> habits) {
        if (habits.isEmpty()) return 0.0;
        
        Calendar cal = Calendar.getInstance();
        int totalDays = 7;
        int completedDays = 0;
        
        for (int i = 0; i < 7; i++) {
            String dateStr = dateFormat.format(cal.getTime());
            boolean allDone = true;
            
            for (HabitEntity habit : habits) {
                CompletionEntity completion = completionDao.getCompletionForDate(dateStr, habit.id);
                if (completion == null || !completion.done) {
                    allDone = false;
                    break;
                }
            }
            
            if (allDone) {
                completedDays++;
            }
            
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        
        return (completedDays * 100.0) / totalDays;
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

