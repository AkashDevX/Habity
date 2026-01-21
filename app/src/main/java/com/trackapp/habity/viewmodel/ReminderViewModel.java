package com.trackapp.habity.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.trackapp.habity.database.AppDatabase;
import com.trackapp.habity.database.ReminderDao;
import com.trackapp.habity.database.ReminderEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReminderViewModel extends AndroidViewModel {
    private ReminderDao reminderDao;
    private ExecutorService executor;
    
    public ReminderViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        reminderDao = db.reminderDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<ReminderEntity>> getRemindersForHabit(long habitId) {
        // Room doesn't directly support LiveData for conditional queries in DAO like this
        // We'll need to load on background thread and update UI manually
        return null; // Will use executor pattern instead
    }
    
    public void getRemindersForHabit(long habitId, OnRemindersLoadedListener listener) {
        executor.execute(() -> {
            List<ReminderEntity> reminders = reminderDao.getRemindersForHabit(habitId);
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                listener.onLoaded(reminders);
            });
        });
    }
    
    public void insertReminder(ReminderEntity reminder, OnReminderInsertedListener listener) {
        executor.execute(() -> {
            long id = reminderDao.insert(reminder);
            reminder.id = id;
            if (listener != null) {
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    listener.onInserted(reminder);
                });
            }
        });
    }
    
    public interface OnReminderInsertedListener {
        void onInserted(ReminderEntity reminder);
    }
    
    public void updateReminder(ReminderEntity reminder) {
        executor.execute(() -> {
            reminderDao.update(reminder);
        });
    }
    
    public void deleteReminder(ReminderEntity reminder) {
        executor.execute(() -> {
            reminderDao.delete(reminder);
        });
    }
    
    public interface OnRemindersLoadedListener {
        void onLoaded(List<ReminderEntity> reminders);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}

