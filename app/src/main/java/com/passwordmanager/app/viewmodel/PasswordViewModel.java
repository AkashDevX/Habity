package com.passwordmanager.app.viewmodel;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.passwordmanager.app.database.AppDatabase;
import com.passwordmanager.app.database.PasswordDao;
import com.passwordmanager.app.database.PasswordEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PasswordViewModel extends AndroidViewModel {
    private PasswordDao passwordDao;
    private LiveData<List<PasswordEntity>> allPasswords;
    private ExecutorService executor;
    
    public PasswordViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        passwordDao = database.passwordDao();
        allPasswords = passwordDao.getAll();
        executor = Executors.newSingleThreadExecutor();
    }
    
    public LiveData<List<PasswordEntity>> getAllPasswords() {
        return allPasswords;
    }
    
    public LiveData<PasswordEntity> getPasswordById(long id) {
        MutableLiveData<PasswordEntity> liveData = new MutableLiveData<>();
        executor.execute(() -> {
            PasswordEntity password = passwordDao.getById(id);
            liveData.postValue(password);
        });
        return liveData;
    }
    
    public void insert(PasswordEntity password) {
        executor.execute(() -> passwordDao.insert(password));
    }
    
    public void update(PasswordEntity password) {
        executor.execute(() -> {
            password.updatedAt = System.currentTimeMillis();
            passwordDao.update(password);
        });
    }
    
    public void delete(PasswordEntity password) {
        executor.execute(() -> passwordDao.delete(password));
    }
    
    public void refresh() {
        // LiveData automatically refreshes when observed
    }
    
    @Override
    protected void onCleared() {
        super.onCleared();
        if (executor != null) {
            executor.shutdown();
        }
    }
}
