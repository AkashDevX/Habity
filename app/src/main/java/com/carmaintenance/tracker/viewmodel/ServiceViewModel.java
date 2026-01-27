package com.carmaintenance.tracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.carmaintenance.tracker.database.AppDatabase;
import com.carmaintenance.tracker.database.ServiceDao;
import com.carmaintenance.tracker.database.ServiceEntity;
import com.carmaintenance.tracker.database.ReminderDao;
import com.carmaintenance.tracker.database.ReminderEntity;
import com.carmaintenance.tracker.reminders.ReminderScheduler;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.lifecycle.MutableLiveData;
import java.text.SimpleDateFormat;

public class ServiceViewModel extends AndroidViewModel {
    private ServiceDao serviceDao;
    private ReminderDao reminderDao;
    private ExecutorService executor;
    private LiveData<List<ServiceEntity>> allServices;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    
    public ServiceViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        serviceDao = db.serviceDao();
        reminderDao = db.reminderDao();
        executor = Executors.newSingleThreadExecutor();
        allServices = serviceDao.getAllServices();
    }
    
    public LiveData<List<ServiceEntity>> getAllServices() {
        return allServices;
    }
    
    public void insertService(ServiceEntity service) {
        executor.execute(() -> {
            serviceDao.insert(service);
        });
    }
    
    public void updateService(ServiceEntity service) {
        executor.execute(() -> {
            serviceDao.update(service);
        });
    }
    
    public void deleteService(ServiceEntity service) {
        executor.execute(() -> {
            // Cancel all reminders for this service before deleting
            ReminderScheduler.cancelAllForService(
                getApplication(), service.id);
            serviceDao.delete(service);
        });
    }
    
    /**
     * Get services that have reminders scheduled for today.
     */
    public LiveData<List<ServiceEntity>> getServicesForToday() {
        MutableLiveData<List<ServiceEntity>> result = new MutableLiveData<>();
        
        executor.execute(() -> {
            String today = dateFormat.format(new Date());
            Calendar cal = Calendar.getInstance();
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int todayBit = dayOfWeekToBit(dayOfWeek);
            
            // Get all enabled services
            List<ServiceEntity> allServicesList = serviceDao.getAllServicesSync();
            List<ServiceEntity> todayServices = new ArrayList<>();
            
            for (ServiceEntity service : allServicesList) {
                if (!service.enabled) {
                    continue; // Skip disabled services
                }
                
                // Check if this service has any reminders for today
                List<ReminderEntity> reminders = reminderDao.getRemindersForService(service.id);
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
                    todayServices.add(service);
                }
            }
            
            // Update on main thread
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                result.setValue(todayServices);
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
