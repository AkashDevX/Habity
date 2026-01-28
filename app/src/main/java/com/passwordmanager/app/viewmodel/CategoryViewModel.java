package com.passwordmanager.app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.passwordmanager.app.database.AppDatabase;
import com.passwordmanager.app.database.CategoryDao;
import com.passwordmanager.app.database.CategoryEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryViewModel extends AndroidViewModel {
    private CategoryDao categoryDao;
    private LiveData<List<CategoryEntity>> allCategories;
    private ExecutorService executor;
    
    public CategoryViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        categoryDao = database.categoryDao();
        allCategories = categoryDao.getAll();
        executor = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<CategoryEntity>> getAllCategories() {
        return allCategories;
    }
    
    public void insert(CategoryEntity category) {
        executor.execute(() -> categoryDao.insert(category));
    }
    
    public void update(CategoryEntity category) {
        executor.execute(() -> categoryDao.update(category));
    }
    
    public void delete(CategoryEntity category) {
        executor.execute(() -> categoryDao.delete(category));
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
