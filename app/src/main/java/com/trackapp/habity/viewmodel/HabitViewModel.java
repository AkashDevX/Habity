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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}

