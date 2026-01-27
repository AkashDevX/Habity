package com.carmaintenance.tracker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.carmaintenance.tracker.adapter.ReminderInlineAdapter;
import com.carmaintenance.tracker.database.AppDatabase;
import com.carmaintenance.tracker.database.ServiceEntity;
import com.carmaintenance.tracker.database.ReminderDao;
import com.carmaintenance.tracker.database.ReminderEntity;
import com.carmaintenance.tracker.reminders.ReminderScheduler;
import com.carmaintenance.tracker.viewmodel.ServiceViewModel;
import com.carmaintenance.tracker.viewmodel.ReminderViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditServiceActivity extends AppCompatActivity {
    private TextInputEditText editTextTitle;
    private TextInputEditText editTextServiceType;
    private TextInputEditText editTextDate;
    private TextInputEditText editTextCost;
    private TextInputEditText editTextMileage;
    private TextInputEditText editTextNotes;
    private Switch switchServiceEnabled;
    private Button buttonAddReminder;
    private Button buttonSave;
    private RecyclerView recyclerViewReminders;
    
    private ServiceViewModel serviceViewModel;
    private ReminderViewModel reminderViewModel;
    private ReminderInlineAdapter reminderAdapter;
    private ExecutorService executor;
    
    private long serviceId = -1;
    private boolean isEditMode = false;
    private List<ReminderEntity> originalReminders = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_service);
        
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            }
        }
        
        executor = Executors.newSingleThreadExecutor();
        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);
        reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);
        
        editTextTitle = findViewById(R.id.editTextTitle);
        editTextServiceType = findViewById(R.id.editTextServiceType);
        editTextDate = findViewById(R.id.editTextDate);
        editTextCost = findViewById(R.id.editTextCost);
        editTextMileage = findViewById(R.id.editTextMileage);
        editTextNotes = findViewById(R.id.editTextNotes);
        switchServiceEnabled = findViewById(R.id.switchServiceEnabled);
        buttonAddReminder = findViewById(R.id.buttonAddReminder);
        buttonSave = findViewById(R.id.buttonSave);
        recyclerViewReminders = findViewById(R.id.recyclerViewReminders);
        
        // Setup RecyclerView for reminders
        reminderAdapter = new ReminderInlineAdapter();
        reminderAdapter.setOnReminderActionListener(new ReminderInlineAdapter.OnReminderActionListener() {
            @Override
            public void onReminderEnabledChanged(ReminderEntity reminder, boolean enabled) {
                reminder.enabled = enabled;
            }
            
            @Override
            public void onReminderEdit(ReminderEntity reminder, int position) {
                showEditReminderDialog(reminder, position);
            }
            
            @Override
            public void onReminderDelete(ReminderEntity reminder, int position) {
                reminderAdapter.removeReminder(position);
            }
        });
        
        recyclerViewReminders.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewReminders.setAdapter(reminderAdapter);
        
        // Check if editing existing service
        if (getIntent().hasExtra("serviceId")) {
            serviceId = getIntent().getLongExtra("serviceId", -1);
            isEditMode = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Service");
            }
            loadServiceData();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Service");
            }
            switchServiceEnabled.setChecked(true);
            // Set default date to today
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            editTextDate.setText(dateFormat.format(new Date()));
        }
        
        buttonAddReminder.setOnClickListener(v -> showAddReminderDialog());
        buttonSave.setOnClickListener(v -> saveService());
    }
    
    private void loadServiceData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            com.carmaintenance.tracker.database.ServiceDao serviceDao = db.serviceDao();
            ServiceEntity service = serviceDao.getServiceById(serviceId);
            
            if (service == null) {
                runOnUiThread(() -> finish());
                return;
            }
            
            ReminderDao reminderDao = db.reminderDao();
            List<ReminderEntity> reminders = reminderDao.getRemindersForService(serviceId);
            
            runOnUiThread(() -> {
                editTextTitle.setText(service.title);
                editTextServiceType.setText(service.serviceType);
                editTextDate.setText(service.date);
                if (service.cost != null) {
                    editTextCost.setText(String.valueOf(service.cost));
                }
                if (service.mileage != null) {
                    editTextMileage.setText(String.valueOf(service.mileage));
                }
                editTextNotes.setText(service.notes);
                switchServiceEnabled.setChecked(service.enabled);
                
                originalReminders = new ArrayList<>(reminders);
                reminderAdapter.setReminders(reminders);
            });
        });
    }
    
    private void showAddReminderDialog() {
        AddReminderDialog dialog = AddReminderDialog.newInstance((hour, minute, daysMask) -> {
            ReminderEntity reminder = new ReminderEntity();
            reminder.hour = hour;
            reminder.minute = minute;
            reminder.daysMask = daysMask;
            reminder.enabled = true;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            reminder.date = dateFormat.format(new Date());
            reminderAdapter.addReminder(reminder);
        });
        
        dialog.show(getSupportFragmentManager(), "AddReminderDialog");
    }
    
    private void showEditReminderDialog(ReminderEntity reminder, int position) {
        AddReminderDialog dialog = AddReminderDialog.newInstanceForEdit(
            reminder.hour, 
            reminder.minute, 
            reminder.daysMask,
            (hour, minute, daysMask) -> {
                reminder.hour = hour;
                reminder.minute = minute;
                reminder.daysMask = daysMask;
                reminderAdapter.updateReminder(position, reminder);
            }
        );
        
        dialog.show(getSupportFragmentManager(), "EditReminderDialog");
    }
    
    private void saveService() {
        String title = editTextTitle.getText().toString().trim();
        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a service title", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String serviceType = editTextServiceType.getText().toString().trim();
        String date = editTextDate.getText().toString().trim();
        String costStr = editTextCost.getText().toString().trim();
        String mileageStr = editTextMileage.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();
        boolean serviceEnabled = switchServiceEnabled.isChecked();
        
        // Parse cost
        final Double cost;
        if (!costStr.isEmpty()) {
            try {
                cost = Double.parseDouble(costStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid cost value", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            cost = null;
        }
        
        // Parse mileage
        final Long mileage;
        if (!mileageStr.isEmpty()) {
            try {
                mileage = Long.parseLong(mileageStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid mileage value", Toast.LENGTH_SHORT).show();
                return;
            }
        } else {
            mileage = null;
        }
        
        List<ReminderEntity> currentReminders = reminderAdapter.getReminders();
        
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            com.carmaintenance.tracker.database.ServiceDao serviceDao = db.serviceDao();
            ReminderDao reminderDao = db.reminderDao();
            
            if (isEditMode) {
                // Update existing service
                ServiceEntity service = serviceDao.getServiceById(serviceId);
                if (service != null) {
                    service.title = title;
                    service.serviceType = serviceType;
                    service.date = date;
                    service.cost = cost;
                    service.mileage = mileage;
                    service.notes = notes;
                    service.enabled = serviceEnabled;
                    serviceDao.update(service);
                    
                    // Cancel all existing alarms for this service
                    ReminderScheduler.cancelAllForService(this, serviceId);
                    
                    // Delete old reminders
                    for (ReminderEntity oldReminder : originalReminders) {
                        reminderDao.delete(oldReminder);
                    }
                    
                    // Insert new reminders
                    for (ReminderEntity reminder : currentReminders) {
                        reminder.serviceId = serviceId;
                        long reminderId = reminderDao.insert(reminder);
                        reminder.id = reminderId;
                    }
                    
                    // Schedule alarms for enabled reminders if service is enabled
                    if (serviceEnabled) {
                        for (ReminderEntity reminder : currentReminders) {
                            if (reminder.enabled) {
                                ReminderScheduler.scheduleReminder(this, reminder.id);
                            }
                        }
                    }
                }
            } else {
                // Insert new service
                ServiceEntity service = new ServiceEntity(title, serviceType);
                service.date = date;
                service.cost = cost;
                service.mileage = mileage;
                service.notes = notes;
                service.enabled = serviceEnabled;
                long newServiceId = serviceDao.insert(service);
                
                // Insert reminders
                for (ReminderEntity reminder : currentReminders) {
                    reminder.serviceId = newServiceId;
                    long reminderId = reminderDao.insert(reminder);
                    reminder.id = reminderId;
                }
                
                // Schedule alarms for enabled reminders if service is enabled
                if (serviceEnabled) {
                    for (ReminderEntity reminder : currentReminders) {
                        if (reminder.enabled) {
                            ReminderScheduler.scheduleReminder(this, reminder.id);
                        }
                    }
                }
            }
            
            runOnUiThread(() -> {
                Toast.makeText(this, "Service saved", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_in_right);
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
