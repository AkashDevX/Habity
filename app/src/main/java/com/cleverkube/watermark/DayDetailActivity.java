package com.cleverkube.watermark;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cleverkube.watermark.adapter.WaterEntryAdapter;
import com.cleverkube.watermark.database.WaterEntry;
import com.cleverkube.watermark.utils.SettingsHelper;
import com.cleverkube.watermark.viewmodel.WaterViewModel;

import java.util.Calendar;
import java.util.List;

public class DayDetailActivity extends AppCompatActivity {
    private WaterViewModel waterViewModel;
    private SettingsHelper settingsHelper;
    private WaterEntryAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day_detail);
        
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        long dayTimestamp = getIntent().getLongExtra("dayTimestamp", System.currentTimeMillis());
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Day Details");
        }
        
        settingsHelper = new SettingsHelper(this);
        waterViewModel = new ViewModelProvider(this).get(WaterViewModel.class);
        
        setupRecyclerView();
        loadDayEntries(dayTimestamp);
    }
    
    private void setupRecyclerView() {
        adapter = new WaterEntryAdapter(settingsHelper);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewEntries);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadDayEntries(long dayTimestamp) {
        // Load entries on background thread
        new Thread(() -> {
            try {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(dayTimestamp);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                long startOfDay = cal.getTimeInMillis();
                
                cal.add(Calendar.DAY_OF_MONTH, 1);
                long endOfDay = cal.getTimeInMillis();
                
                List<WaterEntry> entries = waterViewModel.getEntriesForDaySync(startOfDay, endOfDay);
                
                // Update UI on main thread
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.setEntries(entries != null ? entries : new java.util.ArrayList<>());
                    }
                    
                    View emptyState = findViewById(R.id.emptyStateView);
                    if (entries == null || entries.isEmpty()) {
                        if (emptyState != null) {
                            emptyState.setVisibility(View.VISIBLE);
                        }
                    } else {
                        if (emptyState != null) {
                            emptyState.setVisibility(View.GONE);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (adapter != null) {
                        adapter.setEntries(new java.util.ArrayList<>());
                    }
                    View emptyState = findViewById(R.id.emptyStateView);
                    if (emptyState != null) {
                        emptyState.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).start();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_in_right);
        return true;
    }
}

