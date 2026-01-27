package com.cleverkube.watermark;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.cleverkube.watermark.database.WaterEntry;
import android.content.Intent;
import android.view.View;

import com.cleverkube.watermark.utils.SettingsHelper;
import com.cleverkube.watermark.view.HourlyPatternView;
import com.cleverkube.watermark.view.SimpleBarChartView;
import com.cleverkube.watermark.viewmodel.WaterViewModel;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class StatsActivity extends AppCompatActivity {
    private WaterViewModel waterViewModel;
    private SettingsHelper settingsHelper;
    private SimpleBarChartView barChartView;
    private HourlyPatternView hourlyPatternView;
    private TextView textViewAverage;
    private TextView textViewBestDay;
    private TextView textViewStreak;
    private TextView textViewWeeklyTotal;
    private TextView textViewConsistency;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.stats);
        }
        
        settingsHelper = new SettingsHelper(this);
        waterViewModel = new ViewModelProvider(this).get(WaterViewModel.class);
        
        initializeViews();
        setupClickListeners();
        loadStats();
    }
    
    private void setupClickListeners() {
        View cardAchievements = findViewById(R.id.cardAchievements);
        if (cardAchievements != null) {
            cardAchievements.setOnClickListener(v -> {
                Intent intent = new Intent(StatsActivity.this, AchievementsActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            });
        }
        
        View cardWeeklyReport = findViewById(R.id.cardWeeklyReport);
        if (cardWeeklyReport != null) {
            cardWeeklyReport.setOnClickListener(v -> {
                Intent intent = new Intent(StatsActivity.this, ReportsActivity.class);
                intent.putExtra("reportType", "weekly");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            });
        }
        
        View cardMonthlyReport = findViewById(R.id.cardMonthlyReport);
        if (cardMonthlyReport != null) {
            cardMonthlyReport.setOnClickListener(v -> {
                Intent intent = new Intent(StatsActivity.this, ReportsActivity.class);
                intent.putExtra("reportType", "monthly");
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
            });
        }
    }
    
    private void initializeViews() {
        barChartView = findViewById(R.id.barChartView);
        hourlyPatternView = findViewById(R.id.hourlyPatternView);
        textViewAverage = findViewById(R.id.textViewAverage);
        textViewBestDay = findViewById(R.id.textViewBestDay);
        textViewStreak = findViewById(R.id.textViewStreak);
        textViewWeeklyTotal = findViewById(R.id.textViewWeeklyTotal);
        textViewConsistency = findViewById(R.id.textViewConsistency);
    }
    
    private void loadStats() {
        // Load stats on background thread
        new Thread(() -> {
            try {
                Calendar cal = Calendar.getInstance();
                int[] dailyTotals = new int[7];
                int maxTotal = 0;
                int bestDayIndex = 0;
                int total = 0;
                
                // Calculate totals for last 7 days
                for (int i = 0; i < 7; i++) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long startOfDay = cal.getTimeInMillis();
                    
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    long endOfDay = cal.getTimeInMillis();
                    
                    int dayTotal = waterViewModel.getTotalForDaySync(startOfDay, endOfDay);
                    dailyTotals[6 - i] = dayTotal;
                    total += dayTotal;
                    
                    if (dayTotal > maxTotal) {
                        maxTotal = dayTotal;
                        bestDayIndex = 6 - i;
                    }
                    
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                }
                
                // Calculate streak
                int streak = calculateStreak();
                
                // Calculate hourly pattern for today
                int[] hourlyData = calculateHourlyPattern();
                
                // Calculate consistency (days meeting goal / 7)
                int goal = settingsHelper.getDailyGoalMl();
                int daysMetGoal = 0;
                for (int dayTotal : dailyTotals) {
                    if (dayTotal >= goal) daysMetGoal++;
                }
                int consistency = (daysMetGoal * 100) / 7;
                
                // Store values for UI update
                final int finalTotal = total;
                final int finalMaxTotal = maxTotal;
                final int finalStreak = streak;
                final int[] finalDailyTotals = dailyTotals;
                final int[] finalHourlyData = hourlyData;
                final int finalConsistency = consistency;
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    if (barChartView != null) {
                        barChartView.setData(finalDailyTotals);
                    }
                    if (hourlyPatternView != null) {
                        hourlyPatternView.setData(finalHourlyData);
                    }
                    if (textViewAverage != null) {
                        int average = finalTotal / 7;
                        textViewAverage.setText(settingsHelper.formatAmount(this, average));
                    }
                    if (textViewBestDay != null) {
                        textViewBestDay.setText(settingsHelper.formatAmount(this, finalMaxTotal));
                    }
                    if (textViewStreak != null) {
                        textViewStreak.setText(String.format(Locale.getDefault(), "%d %s", finalStreak, getString(R.string.days)));
                    }
                    if (textViewWeeklyTotal != null) {
                        textViewWeeklyTotal.setText(settingsHelper.formatAmount(this, finalTotal));
                    }
                    if (textViewConsistency != null) {
                        textViewConsistency.setText(String.format(Locale.getDefault(), "%d%%", finalConsistency));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Set default values on error
                    if (barChartView != null) {
                        barChartView.setData(new int[7]);
                    }
                    if (textViewAverage != null) {
                        textViewAverage.setText("0 ml");
                    }
                    if (textViewBestDay != null) {
                        textViewBestDay.setText("0 ml");
                    }
                    if (textViewStreak != null) {
                        textViewStreak.setText("0 days");
                    }
                });
            }
        }).start();
    }
    
    private int calculateStreak() {
        int goal = settingsHelper.getDailyGoalMl();
        Calendar cal = Calendar.getInstance();
        int streak = 0;
        
        while (true) {
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long startOfDay = cal.getTimeInMillis();
            
            cal.add(Calendar.DAY_OF_MONTH, 1);
            long endOfDay = cal.getTimeInMillis();
            
            int total = waterViewModel.getTotalForDaySync(startOfDay, endOfDay);
            
            if (total >= goal) {
                streak++;
                cal.add(Calendar.DAY_OF_MONTH, -1);
                cal.add(Calendar.DAY_OF_MONTH, -1);
            } else {
                break;
            }
            
            // Safety limit
            if (streak > 365) break;
        }
        
        return streak;
    }
    
    private int[] calculateHourlyPattern() {
        int[] hourlyData = new int[24];
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = cal.getTimeInMillis();
        
        List<WaterEntry> entries = waterViewModel.getEntriesForDaySync(startOfDay, endOfDay);
        
        for (WaterEntry entry : entries) {
            Calendar entryCal = Calendar.getInstance();
            entryCal.setTimeInMillis(entry.timestamp);
            int hour = entryCal.get(Calendar.HOUR_OF_DAY);
            hourlyData[hour] += entry.amountMl;
        }
        
        return hourlyData;
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_in_right);
        return true;
    }
}

