package com.carmaintenance.tracker;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.carmaintenance.tracker.adapter.ReminderAdapter;
import com.carmaintenance.tracker.database.AppDatabase;
import com.carmaintenance.tracker.database.ServiceDao;
import com.carmaintenance.tracker.database.ServiceEntity;
import com.carmaintenance.tracker.database.ReminderEntity;
import com.carmaintenance.tracker.reminders.ReminderScheduler;
import com.carmaintenance.tracker.viewmodel.ReminderViewModel;
import com.carmaintenance.tracker.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemindersActivity extends AppCompatActivity {
    private TextView textViewServiceTitle;
    private RecyclerView recyclerViewReminders;
    private ReminderAdapter reminderAdapter;
    private ReminderViewModel reminderViewModel;
    private long serviceId;
    private ExecutorService executor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminders);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reminders");
        }
        
        serviceId = getIntent().getLongExtra("serviceId", -1);
        
        executor = Executors.newSingleThreadExecutor();
        
        textViewServiceTitle = findViewById(R.id.textViewServiceTitle);
        recyclerViewReminders = findViewById(R.id.recyclerViewReminders);
        
        reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);
        reminderAdapter = new ReminderAdapter(reminderViewModel, this);
        reminderAdapter.setOnReminderDeletedListener(() -> loadReminders());
        
        recyclerViewReminders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReminders.setAdapter(reminderAdapter);
        
        if (serviceId == -1) {
            // No specific service - show message
            textViewServiceTitle.setText("All Reminders");
            loadAllReminders();
        } else {
            // Load service title
            loadServiceTitle();
            // Load reminders for specific service
            loadReminders();
        }
        
        // Setup FAB
        FloatingActionButton fabAddReminder = findViewById(R.id.fabAddReminder);
        if (serviceId != -1) {
            fabAddReminder.setOnClickListener(v -> showAddReminderDialog());
        } else {
            fabAddReminder.setVisibility(View.GONE);
        }
    }
    
    private void loadServiceTitle() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            ServiceDao serviceDao = db.serviceDao();
            ServiceEntity service = serviceDao.getServiceById(serviceId);
            
            runOnUiThread(() -> {
                if (service != null) {
                    textViewServiceTitle.setText(service.title);
                }
            });
        });
    }
    
    private void loadReminders() {
        if (serviceId == -1) {
            return;
        }
        reminderViewModel.getRemindersForService(serviceId, reminders -> {
            if (reminders != null) {
                reminderAdapter.setReminders(reminders);
            }
        });
    }
    
    private void loadAllReminders() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            com.carmaintenance.tracker.database.ReminderDao reminderDao = db.reminderDao();
            List<ReminderEntity> allReminders = reminderDao.getEnabledReminders();
            
            runOnUiThread(() -> {
                if (allReminders != null) {
                    reminderAdapter.setReminders(allReminders);
                }
            });
        });
    }
    
    private void showAddReminderDialog() {
        AddReminderDialog dialog = AddReminderDialog.newInstance((hour, minute, daysMask) -> {
            // Set date to today's date
            java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
            String today = dateFormat.format(new java.util.Date());
            ReminderEntity reminder = 
                new ReminderEntity(serviceId, today, hour, minute, daysMask, true);
            reminderViewModel.insertReminder(reminder, insertedReminder -> {
                // Schedule the reminder after insertion
                ReminderScheduler.scheduleReminder(
                    this, insertedReminder.id);
                // Reload reminders list
                loadReminders();
            });
        });
        
        dialog.show(getSupportFragmentManager(), "AddReminderDialog");
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadReminders();
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

