package com.cleverkube.watermark.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.cleverkube.watermark.database.AppDatabase;
import com.cleverkube.watermark.database.WaterDao;
import com.cleverkube.watermark.database.WaterEntry;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WaterViewModel extends AndroidViewModel {
    private WaterDao waterDao;
    private ExecutorService executor;
    
    public WaterViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        waterDao = db.waterDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    public void insertWaterEntry(WaterEntry entry) {
        executor.execute(() -> {
            waterDao.insert(entry);
        });
    }
    
    public void updateWaterEntry(WaterEntry entry) {
        executor.execute(() -> {
            waterDao.update(entry);
        });
    }
    
    public void deleteWaterEntry(WaterEntry entry) {
        executor.execute(() -> {
            waterDao.delete(entry);
        });
    }
    
    public void deleteWaterEntryById(long id) {
        executor.execute(() -> {
            waterDao.deleteById(id);
        });
    }
    
    public LiveData<List<WaterEntry>> getTodayEntries() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = cal.getTimeInMillis();
        
        return waterDao.getEntriesForDay(startOfDay, endOfDay);
    }
    
    public LiveData<Integer> getTodayTotal() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startOfDay = cal.getTimeInMillis();
        
        cal.add(Calendar.DAY_OF_MONTH, 1);
        long endOfDay = cal.getTimeInMillis();
        
        return waterDao.getTotalForDay(startOfDay, endOfDay);
    }
    
    public List<WaterEntry> getEntriesForDaySync(long startOfDay, long endOfDay) {
        return waterDao.getEntriesForDaySync(startOfDay, endOfDay);
    }
    
    public Integer getTotalForDaySync(long startOfDay, long endOfDay) {
        Integer total = waterDao.getTotalForDaySync(startOfDay, endOfDay);
        return total != null ? total : 0;
    }
    
    public List<WaterEntry> getEntriesForRange(long startTimestamp, long endTimestamp) {
        return waterDao.getEntriesForRange(startTimestamp, endTimestamp);
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}


