package com.carmaintenance.tracker.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.carmaintenance.tracker.database.AppDatabase;
import com.carmaintenance.tracker.database.FuelDao;
import com.carmaintenance.tracker.database.FuelEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FuelViewModel extends AndroidViewModel {
    private FuelDao fuelDao;
    private ExecutorService executor;
    private LiveData<List<FuelEntity>> allFuelLogs;
    
    public FuelViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabase.getDatabase(application);
        fuelDao = db.fuelDao();
        executor = Executors.newSingleThreadExecutor();
        allFuelLogs = fuelDao.getAllFuelLogs();
    }
    
    public LiveData<List<FuelEntity>> getAllFuelLogs() {
        return allFuelLogs;
    }
    
    public void insertFuel(FuelEntity fuel) {
        executor.execute(() -> {
            fuelDao.insert(fuel);
        });
    }
    
    public void updateFuel(FuelEntity fuel) {
        executor.execute(() -> {
            fuelDao.update(fuel);
        });
    }
    
    public void deleteFuel(FuelEntity fuel) {
        executor.execute(() -> {
            fuelDao.delete(fuel);
        });
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}

