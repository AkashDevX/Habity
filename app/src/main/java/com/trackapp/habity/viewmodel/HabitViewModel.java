package com.trackapp.habity.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.trackapp.habity.database.AppDatabase;
import com.trackapp.habity.database.HabitDao;
import com.trackapp.habity.database.HabitEntity;
import com.trackapp.habity.database.CompletionDao;
import com.trackapp.habity.database.CompletionEntity;
import com.trackapp.habity.database.ReminderDao;
import com.trackapp.habity.database.ReminderEntity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.MutableLiveData;

public class HabitViewModel extends AndroidViewModel {
    private HabitDao habitDao;
    private CompletionDao completionDao;
    private ReminderDao reminderDao;
    private ExecutorService executor;
    private LiveData<List<HabitEntity>> allHabits;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public HabitViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        habitDao = db.habitDao();
        completionDao = db.completionDao();
        reminderDao = db.reminderDao();
        executor = Executors.newSingleThreadExecutor();
        allHabits = habitDao.getAllHabits();
    }
    
    public LiveData<List<HabitEntity>> getAllHabits() {
        return allHabits;
    }
    
    public void insertHabit(HabitEntity habit) {
        executor.execute(() -> {
            habitDao.insert(habit);
        });
    }
    
    public void updateHabit(HabitEntity habit) {
        executor.execute(() -> {
            habitDao.update(habit);
        });
    }
    
    public void deleteHabit(HabitEntity habit) {
        executor.execute(() -> {
            // Cancel all reminders for this habit before deleting
            com.trackapp.habity.reminders.ReminderScheduler.cancelAllForHabit(
                getApplication(), habit.id);
            habitDao.delete(habit);
        });
    }
    
    public void toggleCompletion(long habitId, boolean done) {
        executor.execute(() -> {
            String today = dateFormat.format(new Date());
            
            // Get the first enabled reminder for this habit (if any)
            Long reminderId = null;
            List<ReminderEntity> reminders = reminderDao.getRemindersForHabit(habitId);
            for (ReminderEntity reminder : reminders) {
                if (reminder.enabled) {
                    reminderId = reminder.id;
                    break; // Use the first enabled reminder
                }
            }
            
            CompletionEntity completion = new CompletionEntity(today, habitId, reminderId, done);
            completionDao.upsertCompletion(completion);
        });
    }
    
    public void checkCompletionForToday(long habitId, OnCompletionCheckedListener listener) {
        executor.execute(() -> {
            String today = dateFormat.format(new Date());
            CompletionEntity completion = completionDao.getCompletionForDate(today, habitId);
            boolean done = completion != null && completion.done;
            // Run on main thread
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                listener.onChecked(done);
            });
        });
    }
    
    public interface OnCompletionCheckedListener {
        void onChecked(boolean done);
    }
    
    /**
     * Get habits that have reminders scheduled for today.
     * A habit is included if it has at least one enabled reminder that:
     * - Has a date matching today, OR
     * - Has a daysMask that includes today's day of week
     */
    public LiveData<List<HabitEntity>> getHabitsForToday() {
        MutableLiveData<List<HabitEntity>> result = new MutableLiveData<>();
        
        executor.execute(() -> {
            String today = dateFormat.format(new Date());
            Calendar cal = Calendar.getInstance();
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int todayBit = dayOfWeekToBit(dayOfWeek);
            
            // Get all enabled habits
            List<HabitEntity> allHabits = habitDao.getAllHabitsSync();
            List<HabitEntity> todayHabits = new ArrayList<>();
            
            for (HabitEntity habit : allHabits) {
                if (!habit.enabled) {
                    continue; // Skip disabled habits
                }
                
                // Check if this habit has any reminders for today
                List<ReminderEntity> reminders = reminderDao.getRemindersForHabit(habit.id);
                boolean hasReminderForToday = false;
                
                for (ReminderEntity reminder : reminders) {
                    if (!reminder.enabled) {
                        continue; // Skip disabled reminders
                    }
                    
                    // Check if reminder matches today by date
                    if (today.equals(reminder.date)) {
                        hasReminderForToday = true;
                        break;
                    }
                    
                    // Check if reminder matches today by daysMask
                    if ((reminder.daysMask & todayBit) != 0) {
                        hasReminderForToday = true;
                        break;
                    }
                }
                
                if (hasReminderForToday) {
                    todayHabits.add(habit);
                }
            }
            
            // Update on main thread
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                result.setValue(todayHabits);
            });
        });
        
        return result;
    }
    
    /**
     * Convert Calendar day of week to daysMask bit.
     * Sun=1, Mon=2, Tue=4, Wed=8, Thu=16, Fri=32, Sat=64
     */
    private int dayOfWeekToBit(int calendarDayOfWeek) {
        switch (calendarDayOfWeek) {
            case Calendar.SUNDAY: return 1;
            case Calendar.MONDAY: return 2;
            case Calendar.TUESDAY: return 4;
            case Calendar.WEDNESDAY: return 8;
            case Calendar.THURSDAY: return 16;
            case Calendar.FRIDAY: return 32;
            case Calendar.SATURDAY: return 64;
            default: return 1;
        }
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}

