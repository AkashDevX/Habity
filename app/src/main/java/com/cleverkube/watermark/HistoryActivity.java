package com.cleverkube.watermark;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cleverkube.watermark.adapter.CalendarAdapter;
import com.cleverkube.watermark.adapter.WaterEntryAdapter;
import com.cleverkube.watermark.database.WaterEntry;
import com.cleverkube.watermark.utils.SettingsHelper;
import com.cleverkube.watermark.view.PieChartView;
import com.cleverkube.watermark.viewmodel.WaterViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryActivity extends AppCompatActivity {
    private WaterViewModel waterViewModel;
    private SettingsHelper settingsHelper;
    private CalendarAdapter calendarAdapter;
    private WaterEntryAdapter entriesAdapter;
    private RecyclerView recyclerViewCalendar;
    private RecyclerView recyclerViewEntries;
    private TextView textViewMonthYear;
    private TextView textViewSelectedDate;
    private ImageButton buttonPrevMonth;
    private ImageButton buttonNextMonth;
    private Calendar currentMonth;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
    private Set<String> datesWithData = new HashSet<>();
    private ExecutorService executor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.history);
        }
        
        settingsHelper = new SettingsHelper(this);
        waterViewModel = new ViewModelProvider(this).get(WaterViewModel.class);
        executor = Executors.newSingleThreadExecutor();
        
        initializeViews();
        setupCalendar();
        setupEntriesList();
        currentMonth = Calendar.getInstance();
        updateCalendar();
    }
    
    private void initializeViews() {
        textViewMonthYear = findViewById(R.id.textViewMonthYear);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        recyclerViewCalendar = findViewById(R.id.recyclerViewCalendar);
        recyclerViewEntries = findViewById(R.id.recyclerViewEntries);
        buttonPrevMonth = findViewById(R.id.buttonPrevMonth);
        buttonNextMonth = findViewById(R.id.buttonNextMonth);
        
        buttonPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateCalendar();
        });
        
        buttonNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateCalendar();
        });
    }
    
    private void setupCalendar() {
        calendarAdapter = new CalendarAdapter();
        calendarAdapter.setOnDayClickListener(date -> {
            calendarAdapter.setSelectedDate(date);
            loadEntriesForDate(date);
        });
        calendarAdapter.setOnDayLongClickListener(date -> {
            showPieChartDialog(date);
        });
        
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 7);
        recyclerViewCalendar.setLayoutManager(gridLayoutManager);
        recyclerViewCalendar.setAdapter(calendarAdapter);
    }
    
    private void setupEntriesList() {
        entriesAdapter = new WaterEntryAdapter(settingsHelper);
        recyclerViewEntries.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewEntries.setAdapter(entriesAdapter);
    }
    
    private void updateCalendar() {
        updateMonthDisplay();
        loadDatesWithData();
        updateCalendarGrid();
    }
    
    private void updateMonthDisplay() {
        textViewMonthYear.setText(monthYearFormat.format(currentMonth.getTime()));
    }
    
    private void loadDatesWithData() {
        executor.execute(() -> {
            Calendar cal = (Calendar) currentMonth.clone();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            long startOfMonth = cal.getTimeInMillis();
            
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            long endOfMonth = cal.getTimeInMillis();
            
            List<WaterEntry> entries = waterViewModel.getEntriesForRange(startOfMonth, endOfMonth);
            Set<String> dates = new HashSet<>();
            
            for (WaterEntry entry : entries) {
                Calendar entryCal = Calendar.getInstance();
                entryCal.setTimeInMillis(entry.timestamp);
                entryCal.set(Calendar.HOUR_OF_DAY, 0);
                entryCal.set(Calendar.MINUTE, 0);
                entryCal.set(Calendar.SECOND, 0);
                entryCal.set(Calendar.MILLISECOND, 0);
                dates.add(dateFormat.format(new Date(entryCal.getTimeInMillis())));
            }
            
            datesWithData = dates;
            runOnUiThread(this::updateCalendarGrid);
        });
    }
    
    private void updateCalendarGrid() {
        List<CalendarAdapter.CalendarDay> days = new ArrayList<>();
        
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Add empty cells for days before month starts
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7;
        for (int i = 0; i < offset; i++) {
            days.add(new CalendarAdapter.CalendarDay("", null, false, false));
        }
        
        // Add days of month
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        String todayStr = dateFormat.format(new Date());
        
        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(cal.getTime());
            boolean isToday = todayStr.equals(dateStr);
            boolean hasData = datesWithData.contains(dateStr);
            days.add(new CalendarAdapter.CalendarDay(String.valueOf(i), dateStr, isToday, hasData));
        }
        
        calendarAdapter.setDays(days);
    }
    
    private void loadEntriesForDate(String dateStr) {
        executor.execute(() -> {
            try {
                Date date = dateFormat.parse(dateStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startOfDay = cal.getTimeInMillis();
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
                long endOfDay = cal.getTimeInMillis();
                
                List<WaterEntry> entries = waterViewModel.getEntriesForDaySync(startOfDay, endOfDay);
                
                String displayDate = displayDateFormat.format(date);
                
                runOnUiThread(() -> {
                    textViewSelectedDate.setText("Selected: " + displayDate);
                    entriesAdapter.setEntries(entries != null ? entries : new ArrayList<>());
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void showPieChartDialog(String dateStr) {
        executor.execute(() -> {
            try {
                Date date = dateFormat.parse(dateStr);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startOfDay = cal.getTimeInMillis();
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
                long endOfDay = cal.getTimeInMillis();
                
                int consumed = waterViewModel.getTotalForDaySync(startOfDay, endOfDay);
                int goal = settingsHelper.getDailyGoalMl();
                String displayDate = displayDateFormat.format(date);
                
                runOnUiThread(() -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pie_chart, null);
                    
                    TextView textViewDate = dialogView.findViewById(R.id.textViewDate);
                    PieChartView pieChartView = dialogView.findViewById(R.id.pieChartView);
                    TextView textViewStats = dialogView.findViewById(R.id.textViewStats);
                    
                    textViewDate.setText("Date: " + displayDate);
                    pieChartView.setData(consumed, goal);
                    textViewStats.setText(String.format(Locale.getDefault(), 
                        "Consumed: %s / Goal: %s", 
                        settingsHelper.formatAmount(this, consumed),
                        settingsHelper.formatAmount(this, goal)));
                    
                    builder.setView(dialogView);
                    builder.setPositiveButton("OK", null);
                    builder.show();
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
