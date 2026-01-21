package com.trackapp.habity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CalendarActivity extends AppCompatActivity {
    private TextView textViewMonthYear;
    private TextView textViewSelectedDate;
    private RecyclerView recyclerViewCalendar;
    private RecyclerView recyclerViewDayHabits;
    private Calendar currentMonth;
    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Calendar");
        }
        
        textViewMonthYear = findViewById(R.id.textViewMonthYear);
        textViewSelectedDate = findViewById(R.id.textViewSelectedDate);
        recyclerViewCalendar = findViewById(R.id.recyclerViewCalendar);
        recyclerViewDayHabits = findViewById(R.id.recyclerViewDayHabits);
        
        currentMonth = Calendar.getInstance();
        updateMonthDisplay();
        
        // Setup calendar grid
        recyclerViewCalendar.setLayoutManager(new GridLayoutManager(this, 7));
        updateCalendarGrid();
        
        // Setup day habits list
        recyclerViewDayHabits.setLayoutManager(new LinearLayoutManager(this));
    }
    
    private void updateMonthDisplay() {
        textViewMonthYear.setText(monthYearFormat.format(currentMonth.getTime()));
    }
    
    private void updateCalendarGrid() {
        List<CalendarDay> days = new ArrayList<>();
        
        Calendar cal = (Calendar) currentMonth.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        
        // Add empty cells for days before month starts
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int offset = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7;
        for (int i = 0; i < offset; i++) {
            days.add(new CalendarDay("", false, false));
        }
        
        // Add days of month
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        for (int i = 1; i <= daysInMonth; i++) {
            cal.set(Calendar.DAY_OF_MONTH, i);
            String dateStr = dateFormat.format(cal.getTime());
            boolean isToday = dateFormat.format(new Date()).equals(dateStr);
            // TODO: Check if all habits done for this date
            boolean allDone = false;
            days.add(new CalendarDay(String.valueOf(i), isToday, allDone));
        }
        
        // Simple adapter (you can enhance this)
        // For now, just show the grid
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    
    private static class CalendarDay {
        String day;
        boolean isToday;
        boolean allDone;
        
        CalendarDay(String day, boolean isToday, boolean allDone) {
            this.day = day;
            this.isToday = isToday;
            this.allDone = allDone;
        }
    }
}

