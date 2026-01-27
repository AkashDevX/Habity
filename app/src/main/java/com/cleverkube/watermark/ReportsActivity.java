package com.cleverkube.watermark;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.cleverkube.watermark.utils.SettingsHelper;
import com.cleverkube.watermark.viewmodel.WaterViewModel;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportsActivity extends AppCompatActivity {
    private WaterViewModel waterViewModel;
    private SettingsHelper settingsHelper;
    private ExecutorService executor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);
        
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        String reportType = getIntent().getStringExtra("reportType"); // "weekly" or "monthly"
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(reportType != null && reportType.equals("monthly") ? "Monthly Report" : "Weekly Report");
        }
        
        settingsHelper = new SettingsHelper(this);
        waterViewModel = new ViewModelProvider(this).get(WaterViewModel.class);
        executor = Executors.newSingleThreadExecutor();
        
        loadReport(reportType != null && reportType.equals("monthly"));
    }
    
    private void loadReport(boolean isMonthly) {
        executor.execute(() -> {
            try {
                Calendar cal = Calendar.getInstance();
                int days = isMonthly ? 30 : 7;
                int total = 0;
                int daysMetGoal = 0;
                int maxDay = 0;
                int minDay = Integer.MAX_VALUE;
                int goal = settingsHelper.getDailyGoalMl();
                
                for (int i = 0; i < days; i++) {
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    long startOfDay = cal.getTimeInMillis();
                    
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    long endOfDay = cal.getTimeInMillis();
                    
                    int dayTotal = waterViewModel.getTotalForDaySync(startOfDay, endOfDay);
                    total += dayTotal;
                    
                    if (dayTotal >= goal) daysMetGoal++;
                    if (dayTotal > maxDay) maxDay = dayTotal;
                    if (dayTotal < minDay && dayTotal > 0) minDay = dayTotal;
                    
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                    cal.add(Calendar.DAY_OF_MONTH, -1);
                }
                
                if (minDay == Integer.MAX_VALUE) minDay = 0;
                
                final int finalTotal = total;
                final int finalDaysMetGoal = daysMetGoal;
                final int finalMaxDay = maxDay;
                final int finalMinDay = minDay;
                final int finalDays = days;
                
                runOnUiThread(() -> {
                    TextView textViewTotal = findViewById(R.id.textViewTotal);
                    TextView textViewAverage = findViewById(R.id.textViewAverage);
                    TextView textViewDaysMet = findViewById(R.id.textViewDaysMet);
                    TextView textViewMax = findViewById(R.id.textViewMax);
                    TextView textViewMin = findViewById(R.id.textViewMin);
                    
                    if (textViewTotal != null) {
                        textViewTotal.setText(settingsHelper.formatAmount(this, finalTotal));
                    }
                    if (textViewAverage != null) {
                        int avg = finalDays > 0 ? finalTotal / finalDays : 0;
                        textViewAverage.setText(settingsHelper.formatAmount(this, avg));
                    }
                    if (textViewDaysMet != null) {
                        textViewDaysMet.setText(String.format(Locale.getDefault(), "%d / %d days", finalDaysMetGoal, finalDays));
                    }
                    if (textViewMax != null) {
                        textViewMax.setText(settingsHelper.formatAmount(this, finalMaxDay));
                    }
                    if (textViewMin != null) {
                        textViewMin.setText(settingsHelper.formatAmount(this, finalMinDay));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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


