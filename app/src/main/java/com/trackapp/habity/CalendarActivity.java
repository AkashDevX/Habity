package com.trackapp.habity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.trackapp.habity.adapter.CalendarAdapter;
import com.trackapp.habity.adapter.DateHabitsAdapter;
import com.trackapp.habity.database.AppDatabase;
import com.trackapp.habity.database.CompletionEntity;
import com.trackapp.habity.database.HabitEntity;
import com.trackapp.habity.database.ReminderEntity;
import com.trackapp.habity.view.PieChartView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CalendarActivity extends AppCompatActivity {
    private TextView textViewMonthYear;
    private TextView textViewSelectedDate;
    private RecyclerView recyclerViewCalendar;
    private RecyclerView recyclerViewDayHabits;
    private ImageButton buttonPrevMonth;
    private ImageButton buttonNextMonth;
    private Calendar currentMonth;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    private SimpleDateFormat displayDateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.getDefault());
    private CalendarAdapter calendarAdapter;
    private DateHabitsAdapter dateHabitsAdapter;
    private AppDatabase database;
    private ExecutorService executor;
    private Set<String> completedDates = new HashSet<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Calendar");
        }
        
        database = AppDatabase.getDatabase(this);
        executor = Executors.newSingleThreadExecutor();
        
        textViewMonthYear = findViewById(R.id.textViewMonthYear);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        recyclerViewCalendar = findViewById(R.id.recyclerViewCalendar);
        recyclerViewDayHabits = findViewById(R.id.recyclerViewDayHabits);
        buttonPrevMonth = findViewById(R.id.buttonPrevMonth);
        buttonNextMonth = findViewById(R.id.buttonNextMonth);
        
        currentMonth = Calendar.getInstance();
        
        // Setup calendar grid
        calendarAdapter = new CalendarAdapter();
        calendarAdapter.setOnDayClickListener(new CalendarAdapter.OnDayClickListener() {
            @Override
            public void onDayClick(String date) {
                loadHabitsForDate(date);
            }
            
            @Override
            public void onDayLongClick(String date) {
                showPieChartDialog(date);
            }
        });
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 7);
        recyclerViewCalendar.setLayoutManager(gridLayoutManager);
        recyclerViewCalendar.setAdapter(calendarAdapter);
        recyclerViewCalendar.setHasFixedSize(true);
        
        // Setup day habits list
        dateHabitsAdapter = new DateHabitsAdapter();
        recyclerViewDayHabits.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDayHabits.setAdapter(dateHabitsAdapter);
        
        // Setup navigation buttons
        buttonPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateCalendar();
        });
        
        buttonNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateCalendar();
        });
        
        updateCalendar();
    }
    
    private void updateCalendar() {
        updateMonthDisplay();
        // Show calendar immediately, then update with completion data
        updateCalendarGrid();
        loadCompletionsForMonth();
        // Clear the habits list when changing months
        dateHabitsAdapter.setItems(new ArrayList<>());
        textViewSelectedDate.setText("Selected: None");
    }
    
    private void updateMonthDisplay() {
        textViewMonthYear.setText(monthYearFormat.format(currentMonth.getTime()));
    }
    
    private void loadCompletionsForMonth() {
        executor.execute(() -> {
            // Calculate start and end dates for the month
            Calendar cal = (Calendar) currentMonth.clone();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            String startDate = dateFormat.format(cal.getTime());
            
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String endDate = dateFormat.format(cal.getTime());
            
            // Load all completions for this month (both done and not done)
            List<CompletionEntity> allCompletions = database.completionDao().getAllCompletionsInRange(startDate, endDate);
            
            // Extract unique dates that have at least one completed habit (done == 1)
            Set<String> completedDatesSet = new HashSet<>();
            for (CompletionEntity completion : allCompletions) {
                if (completion.done) {
                    completedDatesSet.add(completion.date);
                }
            }
            
            completedDates = completedDatesSet;
            
            // Update UI on main thread
            runOnUiThread(() -> updateCalendarGrid());
        });
    }
    
    private void loadHabitsForDate(String dateStr) {
        executor.execute(() -> {
            // Load reminders for the selected date
            List<ReminderEntity> reminders = database.reminderDao().getRemindersForDate(dateStr);
            
            // Load all completions for the selected date
            List<CompletionEntity> completions = database.completionDao().getCompletionsForDate(dateStr);
            
            // Create a map of reminderId -> completion (to check if reminder is completed)
            Map<Long, CompletionEntity> completionByReminderMap = new HashMap<>();
            for (CompletionEntity completion : completions) {
                if (completion.reminderId != null) {
                    completionByReminderMap.put(completion.reminderId, completion);
                }
            }
            
            // Load habits to get habit titles
            Map<Long, HabitEntity> habitMap = new HashMap<>();
            List<HabitEntity> allHabits = database.habitDao().getAllHabitsSync();
            for (HabitEntity habit : allHabits) {
                habitMap.put(habit.id, habit);
            }
            
            // Build reminder list for the selected date
            List<DateHabitsAdapter.ReminderItem> remindersForDate = new ArrayList<>();
            
            for (ReminderEntity reminder : reminders) {
                if (!reminder.enabled) continue;
                
                // Get habit title
                String habitTitle = "Unknown Habit";
                if (habitMap.containsKey(reminder.habitId)) {
                    HabitEntity habit = habitMap.get(reminder.habitId);
                    if (habit.enabled) {
                        habitTitle = habit.title;
                    } else {
                        continue; // Skip if habit is disabled
                    }
                }
                
                // Check if this reminder is completed
                boolean completed = false;
                if (completionByReminderMap.containsKey(reminder.id)) {
                    CompletionEntity completion = completionByReminderMap.get(reminder.id);
                    completed = completion.done;
                }
                
                String reminderTime = String.format("%02d:%02d", reminder.hour, reminder.minute);
                
                remindersForDate.add(new DateHabitsAdapter.ReminderItem(
                    reminder.id, reminder.habitId, habitTitle, reminderTime, completed));
            }
            
            // Format the date for display
            String displayDateValue;
            try {
                Date date = dateFormat.parse(dateStr);
                displayDateValue = displayDateFormat.format(date);
            } catch (Exception e) {
                displayDateValue = dateStr;
            }
            
            // Create final references for use in lambda
            final String displayDate = displayDateValue;
            final List<DateHabitsAdapter.ReminderItem> finalRemindersForDate = new ArrayList<>(remindersForDate);
            
            // Update UI on main thread
            runOnUiThread(() -> {
                textViewSelectedDate.setText("Selected: " + displayDate);
                List<DateHabitsAdapter.DateHabitsItem> dateHabitsList = new ArrayList<>();
                dateHabitsList.add(new DateHabitsAdapter.DateHabitsItem(displayDate, finalRemindersForDate));
                dateHabitsAdapter.setItems(dateHabitsList);
            });
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
        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(cal.getTime());
            boolean isToday = dateFormat.format(new Date()).equals(dateStr);
            boolean hasCompleted = completedDates.contains(dateStr);
            days.add(new CalendarAdapter.CalendarDay(String.valueOf(i), dateStr, isToday, hasCompleted));
        }
        
        calendarAdapter.setDays(days);
    }

    private void showPieChartDialog(String dateStr) {
        executor.execute(() -> {
            // Load reminders for the selected date
            List<ReminderEntity> reminders = database.reminderDao().getRemindersForDate(dateStr);

            // Check if there are any reminders
            if (reminders == null || reminders.isEmpty()) {
                // Format the date for display
                String displayDate;
                try {
                    Date date = dateFormat.parse(dateStr);
                    displayDate = displayDateFormat.format(date);
                } catch (Exception e) {
                    displayDate = dateStr;
                }

                final String finalDisplayDate = displayDate;

                // Show message dialog on main thread
                runOnUiThread(() -> {
                    new AlertDialog.Builder(this)
                            .setTitle("No Reminders")
                            .setMessage("No reminders for " + finalDisplayDate)
                            .setPositiveButton("OK", null)
                            .show();
                });
                return;
            }

            // Load all completions for the selected date
            List<CompletionEntity> completions = database.completionDao().getCompletionsForDate(dateStr);

            // Create a map of reminderId -> completion (to check if reminder is completed)
            Set<Long> completedReminderIds = new HashSet<>();
            for (CompletionEntity completion : completions) {
                if (completion.reminderId != null && completion.done) {
                    completedReminderIds.add(completion.reminderId);
                }
            }

            // Count completed and total reminders
            int totalReminders = reminders.size();
            int completedReminders = 0;

            for (ReminderEntity reminder : reminders) {
                if (reminder.enabled && completedReminderIds.contains(reminder.id)) {
                    completedReminders++;
                }
            }

            // Calculate percentages
            float completedPercentage = totalReminders > 0 ? (completedReminders * 100f / totalReminders) : 0f;
            float notCompletedPercentage = totalReminders > 0 ? ((totalReminders - completedReminders) * 100f / totalReminders) : 0f;

            // Format the date for display
            String displayDate;
            try {
                Date date = dateFormat.parse(dateStr);
                displayDate = displayDateFormat.format(date);
            } catch (Exception e) {
                displayDate = dateStr;
            }

            final int finalCompleted = completedReminders;
            final int finalTotal = totalReminders;
            final float finalCompletedPct = completedPercentage;
            final float finalNotCompletedPct = notCompletedPercentage;
            final String finalDisplayDate = displayDate;

            // Show dialog on main thread
            runOnUiThread(() -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_pie_chart, null);

                TextView textViewDate = dialogView.findViewById(R.id.textViewDate);
                PieChartView pieChartView = dialogView.findViewById(R.id.pieChartView);
                TextView textViewStats = dialogView.findViewById(R.id.textViewStats);

                textViewDate.setText("Date: " + finalDisplayDate);
                pieChartView.setData(finalCompletedPct, finalNotCompletedPct);
                textViewStats.setText("Completed: " + finalCompleted + " / Total: " + finalTotal);

                builder.setView(dialogView);
                builder.setPositiveButton("OK", null);
                builder.show();
            });
        });
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
