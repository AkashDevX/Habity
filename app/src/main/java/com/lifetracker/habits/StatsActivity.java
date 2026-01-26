package com.lifetracker.habits;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifetracker.habits.database.AppDatabase;
import com.lifetracker.habits.database.CompletionDao;
import com.lifetracker.habits.database.CompletionEntity;
import com.lifetracker.habits.database.HabitDao;
import com.lifetracker.habits.database.HabitEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsActivity extends AppCompatActivity {
    private TextView textViewCurrentStreak;
    private TextView textViewLongestStreak;
    private TextView textView7DayCompletion;
    private TextView textViewTotalHabits;
    private TextView textViewMonthlyCompletion;
    private TextView textViewBestDay;
    private TextView textViewMonthlyTrend;
    private LinearLayout linearLayoutWeeklyProgress;
    private RecyclerView recyclerViewHabitStats;
    private ExecutorService executor;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat dayNameFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        
        // Override transition
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Stats");
        }
        
        initializeViews();
        
        recyclerViewHabitStats.setLayoutManager(new LinearLayoutManager(this));
        
        executor = Executors.newSingleThreadExecutor();
        loadStats();
        
        // Animate cards
        animateCards();
    }
    
    private void initializeViews() {
        textViewCurrentStreak = findViewById(R.id.textViewCurrentStreak);
        textViewLongestStreak = findViewById(R.id.textViewLongestStreak);
        textView7DayCompletion = findViewById(R.id.textView7DayCompletion);
        textViewTotalHabits = findViewById(R.id.textViewTotalHabits);
        textViewMonthlyCompletion = findViewById(R.id.textViewMonthlyCompletion);
        textViewBestDay = findViewById(R.id.textViewBestDay);
        textViewMonthlyTrend = findViewById(R.id.textViewMonthlyTrend);
        linearLayoutWeeklyProgress = findViewById(R.id.linearLayoutWeeklyProgress);
        recyclerViewHabitStats = findViewById(R.id.recyclerViewHabitStats);
    }
    
    private void animateCards() {
        View cardCurrentStreak = findViewById(R.id.cardCurrentStreak);
        View cardLongestStreak = findViewById(R.id.cardLongestStreak);
        View card7DayCompletion = findViewById(R.id.card7DayCompletion);
        View cardTotalHabits = findViewById(R.id.cardTotalHabits);
        View cardMonthlyCompletion = findViewById(R.id.cardMonthlyCompletion);
        View cardBestDay = findViewById(R.id.cardBestDay);
        
        animateCard(cardCurrentStreak, 0);
        animateCard(cardLongestStreak, 100);
        animateCard(card7DayCompletion, 200);
        animateCard(cardTotalHabits, 300);
        animateCard(cardMonthlyCompletion, 400);
        animateCard(cardBestDay, 500);
    }
    
    private void animateCard(View card, int delay) {
        if (card != null) {
            card.setAlpha(0f);
            card.setTranslationX(-50f);
            card.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(500)
                .setStartDelay(delay)
                .start();
        }
    }
    
    private void loadStats() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            HabitDao habitDao = db.habitDao();
            CompletionDao completionDao = db.completionDao();
            
            // Get habits directly from DAO (synchronous query for background thread)
            List<HabitEntity> habits = habitDao.getAllHabitsSync();
            
            // Calculate all stats
            int currentStreak = calculateCurrentStreak(completionDao, habits);
            int longestStreak = calculateLongestStreak(completionDao, habits);
            double sevenDayCompletion = calculate7DayCompletion(completionDao, habits);
            int totalHabits = habits.size();
            double monthlyCompletion = calculateMonthlyCompletion(completionDao, habits);
            String bestDay = calculateBestDay(completionDao, habits);
            List<WeeklyProgressData> weeklyProgress = calculateWeeklyProgress(completionDao, habits);
            List<HabitStatData> habitStats = calculateHabitStats(completionDao, habits);
            
            runOnUiThread(() -> {
                // Update main stats
                textViewCurrentStreak.setText(String.valueOf(currentStreak));
                textViewLongestStreak.setText(String.valueOf(longestStreak));
                textView7DayCompletion.setText(String.format(Locale.getDefault(), "%.0f%%", sevenDayCompletion));
                textViewTotalHabits.setText(String.valueOf(totalHabits));
                textViewMonthlyCompletion.setText(String.format(Locale.getDefault(), "%.0f%%", monthlyCompletion));
                textViewBestDay.setText(bestDay);
                textViewMonthlyTrend.setText(String.format(Locale.getDefault(), "This month: %.0f%% completion", monthlyCompletion));
                
                // Populate weekly progress
                populateWeeklyProgress(weeklyProgress);
                
                // Populate habit stats
                HabitStatsAdapter adapter = new HabitStatsAdapter(habitStats);
                recyclerViewHabitStats.setAdapter(adapter);
            });
        });
    }
    
    private void populateWeeklyProgress(List<WeeklyProgressData> weeklyProgress) {
        linearLayoutWeeklyProgress.removeAllViews();
        
        for (WeeklyProgressData data : weeklyProgress) {
            View progressView = LayoutInflater.from(this).inflate(R.layout.item_weekly_progress, linearLayoutWeeklyProgress, false);
            
            TextView dayName = progressView.findViewById(R.id.textViewDayName);
            ProgressBar progressBar = progressView.findViewById(R.id.progressBarDay);
            TextView percentage = progressView.findViewById(R.id.textViewPercentage);
            
            dayName.setText(data.dayName);
            int progress = (int) data.completionPercentage;
            progressBar.setProgress(progress);
            percentage.setText(String.format(Locale.getDefault(), "%.0f%%", data.completionPercentage));
            
            linearLayoutWeeklyProgress.addView(progressView);
        }
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
        if (habits.isEmpty()) return 0;
        
        // Get all completion dates
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -365); // Check last year
        
        int maxStreak = 0;
        int currentStreak = 0;
        Calendar checkCal = Calendar.getInstance();
        
        while (checkCal.after(cal) || checkCal.equals(cal)) {
            String dateStr = dateFormat.format(checkCal.getTime());
            boolean allDone = true;
            
            for (HabitEntity habit : habits) {
                CompletionEntity completion = completionDao.getCompletionForDate(dateStr, habit.id);
                if (completion == null || !completion.done) {
                    allDone = false;
                    break;
                }
            }
            
            if (allDone) {
                currentStreak++;
                maxStreak = Math.max(maxStreak, currentStreak);
            } else {
                currentStreak = 0;
            }
            
            checkCal.add(Calendar.DAY_OF_MONTH, -1);
        }
        
        return maxStreak;
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
    
    private double calculateMonthlyCompletion(CompletionDao completionDao, List<HabitEntity> habits) {
        if (habits.isEmpty()) return 0.0;
        
        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.get(Calendar.DAY_OF_MONTH);
        int completedDays = 0;
        
        for (int i = 0; i < daysInMonth; i++) {
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
        
        return daysInMonth > 0 ? (completedDays * 100.0) / daysInMonth : 0.0;
    }
    
    private String calculateBestDay(CompletionDao completionDao, List<HabitEntity> habits) {
        if (habits.isEmpty()) return "--";
        
        Map<String, Integer> dayCounts = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        
        // Check last 30 days
        for (int i = 0; i < 30; i++) {
            String dateStr = dateFormat.format(cal.getTime());
            String dayName = dayNameFormat.format(cal.getTime());
            
            boolean allDone = true;
            for (HabitEntity habit : habits) {
                CompletionEntity completion = completionDao.getCompletionForDate(dateStr, habit.id);
                if (completion == null || !completion.done) {
                    allDone = false;
                    break;
                }
            }
            
            if (allDone) {
                dayCounts.put(dayName, dayCounts.getOrDefault(dayName, 0) + 1);
            }
            
            cal.add(Calendar.DAY_OF_MONTH, -1);
        }
        
        if (dayCounts.isEmpty()) return "--";
        
        return Collections.max(dayCounts.entrySet(), Comparator.comparingInt(Map.Entry::getValue)).getKey();
    }
    
    private List<WeeklyProgressData> calculateWeeklyProgress(CompletionDao completionDao, List<HabitEntity> habits) {
        List<WeeklyProgressData> progress = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        
        // Get to the start of the current week (Sunday)
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysFromSunday = (dayOfWeek == Calendar.SUNDAY) ? 0 : dayOfWeek - Calendar.SUNDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysFromSunday);
        
        // Calculate for each day of the week (Sunday to Saturday)
        for (int i = 0; i < 7; i++) {
            String dateStr = dateFormat.format(cal.getTime());
            String dayName = getShortDayName(cal.get(Calendar.DAY_OF_WEEK));
            
            int completed = 0;
            int total = habits.size();
            
            for (HabitEntity habit : habits) {
                CompletionEntity completion = completionDao.getCompletionForDate(dateStr, habit.id);
                if (completion != null && completion.done) {
                    completed++;
                }
            }
            
            double percentage = total > 0 ? (completed * 100.0) / total : 0.0;
            progress.add(new WeeklyProgressData(dayName, percentage));
            cal.add(Calendar.DAY_OF_MONTH, 1); // Move to next day
        }
        
        return progress;
    }
    
    private String getShortDayName(int dayOfWeek) {
        switch (dayOfWeek) {
            case Calendar.SUNDAY: return "Sun";
            case Calendar.MONDAY: return "Mon";
            case Calendar.TUESDAY: return "Tue";
            case Calendar.WEDNESDAY: return "Wed";
            case Calendar.THURSDAY: return "Thu";
            case Calendar.FRIDAY: return "Fri";
            case Calendar.SATURDAY: return "Sat";
            default: return "";
        }
    }
    
    private List<HabitStatData> calculateHabitStats(CompletionDao completionDao, List<HabitEntity> habits) {
        List<HabitStatData> stats = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        
        for (HabitEntity habit : habits) {
            int completed = 0;
            int total = 30; // Last 30 days
            
            Calendar checkCal = Calendar.getInstance();
            for (int i = 0; i < 30; i++) {
                String dateStr = dateFormat.format(checkCal.getTime());
                CompletionEntity completion = completionDao.getCompletionForDate(dateStr, habit.id);
                if (completion != null && completion.done) {
                    completed++;
                }
                checkCal.add(Calendar.DAY_OF_MONTH, -1);
            }
            
            double percentage = (completed * 100.0) / total;
            stats.add(new HabitStatData(habit.title, percentage, completed, total));
        }
        
        // Sort by percentage descending
        stats.sort((a, b) -> Double.compare(b.percentage, a.percentage));
        
        return stats;
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
    
    // Data classes
    private static class WeeklyProgressData {
        String dayName;
        double completionPercentage;
        
        WeeklyProgressData(String dayName, double completionPercentage) {
            this.dayName = dayName;
            this.completionPercentage = completionPercentage;
        }
    }
    
    private static class HabitStatData {
        String habitName;
        double percentage;
        int completed;
        int total;
        
        HabitStatData(String habitName, double percentage, int completed, int total) {
            this.habitName = habitName;
            this.percentage = percentage;
            this.completed = completed;
            this.total = total;
        }
    }
    
    // Adapter for habit stats
    private class HabitStatsAdapter extends RecyclerView.Adapter<HabitStatsAdapter.ViewHolder> {
        private List<HabitStatData> habitStats;
        
        HabitStatsAdapter(List<HabitStatData> habitStats) {
            this.habitStats = habitStats;
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit_stat, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            HabitStatData data = habitStats.get(position);
            holder.habitName.setText(data.habitName);
            holder.percentage.setText(String.format(Locale.getDefault(), "%.0f%%", data.percentage));
            holder.progressBar.setProgress((int) data.percentage);
            holder.completedCount.setText(String.format(Locale.getDefault(), "%d/%d", data.completed, data.total));
        }
        
        @Override
        public int getItemCount() {
            return habitStats.size();
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView habitName;
            TextView percentage;
            ProgressBar progressBar;
            TextView completedCount;
            
            ViewHolder(View itemView) {
                super(itemView);
                habitName = itemView.findViewById(R.id.textViewHabitName);
                percentage = itemView.findViewById(R.id.textViewPercentage);
                progressBar = itemView.findViewById(R.id.progressBarHabit);
                completedCount = itemView.findViewById(R.id.textViewCompletedCount);
            }
        }
    }
}

