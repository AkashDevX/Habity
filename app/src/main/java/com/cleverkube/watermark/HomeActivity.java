package com.cleverkube.watermark;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cleverkube.watermark.adapter.WaterEntryAdapter;
import com.cleverkube.watermark.database.AppDatabase;
import com.cleverkube.watermark.database.WaterContainer;
import com.cleverkube.watermark.database.WaterContainerDao;
import com.cleverkube.watermark.database.WaterEntry;
import com.cleverkube.watermark.utils.AchievementChecker;
import com.cleverkube.watermark.utils.HydrationTips;
import com.cleverkube.watermark.utils.SettingsHelper;
import com.cleverkube.watermark.viewmodel.WaterViewModel;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity {
    private WaterViewModel waterViewModel;
    private SettingsHelper settingsHelper;
    private WaterEntryAdapter adapter;
    private RecyclerView recyclerViewEntries;
    private View emptyStateView;
    private TextView textViewProgress;
    private TextView textViewRemaining;
    private TextView textViewStatus;
    private ProgressBar progressBar;
    private LinearLayout quickAddLayout;
    private WaterEntry deletedEntry;
    private TextView textViewTip;
    private ExecutorService executor;
    private WaterContainerDao containerDao;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.home);
        }
        
        try {
            settingsHelper = new SettingsHelper(this);
            waterViewModel = new ViewModelProvider(this).get(WaterViewModel.class);
            executor = Executors.newSingleThreadExecutor();
            
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                if (db != null) {
                    containerDao = db.waterContainerDao();
                }
            } catch (Exception e) {
                e.printStackTrace();
                containerDao = null;
            }
            
            initializeViews();
            setupRecyclerView();
            setupQuickAddButtons();
            setupHydrationTip();
            observeData();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error initializing app: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    private void initializeViews() {
        try {
            textViewProgress = findViewById(R.id.textViewProgress);
            textViewRemaining = findViewById(R.id.textViewRemaining);
            textViewStatus = findViewById(R.id.textViewStatus);
            progressBar = findViewById(R.id.progressBar);
            quickAddLayout = findViewById(R.id.quickAddLayout);
            recyclerViewEntries = findViewById(R.id.recyclerViewEntries);
            emptyStateView = findViewById(R.id.emptyStateView);
            textViewTip = findViewById(R.id.textViewTip);
            
            if (textViewProgress == null || textViewRemaining == null || textViewStatus == null || 
                progressBar == null || quickAddLayout == null || recyclerViewEntries == null) {
                throw new IllegalStateException("Required views not found in layout");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading views: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupHydrationTip() {
        try {
            if (textViewTip != null) {
                textViewTip.setText("💡 " + HydrationTips.getTipOfTheDay());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupRecyclerView() {
        try {
            if (recyclerViewEntries == null) return;
            
            adapter = new WaterEntryAdapter(settingsHelper);
            adapter.setOnDeleteClickListener(entry -> {
                deletedEntry = entry;
                waterViewModel.deleteWaterEntry(entry);
                showUndoSnackbar();
            });
            
            recyclerViewEntries.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewEntries.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupQuickAddButtons() {
        executor.execute(() -> {
            try {
                List<WaterContainer> containers = containerDao != null ? containerDao.getAllContainersSync() : null;
                if (containers == null || containers.isEmpty()) {
                // Fallback to default amounts
                runOnUiThread(() -> {
                    int[] amounts = {100, 200, 250, 300, 500};
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    params.setMargins(4, 4, 4, 4);
                    
                    for (int amount : amounts) {
                        Button button = new Button(this);
                        button.setText(settingsHelper.formatAmount(this, amount));
                        button.setLayoutParams(params);
                        button.setTextColor(getResources().getColor(android.R.color.white));
                        button.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
                        button.setOnClickListener(v -> addWater(amount, ""));
                        quickAddLayout.addView(button);
                    }
                });
            } else {
                // Use containers
                runOnUiThread(() -> {
                    quickAddLayout.removeAllViews();
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                    params.setMargins(4, 4, 4, 4);
                    
                    for (WaterContainer container : containers) {
                        Button button = new Button(this);
                        button.setText(container.icon + "\n" + settingsHelper.formatAmount(this, container.amountMl));
                        button.setLayoutParams(params);
                        button.setTextColor(getResources().getColor(android.R.color.white));
                        button.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
                        button.setOnClickListener(v -> addWater(container.amountMl, container.name));
                        quickAddLayout.addView(button);
                    }
                });
            }
            
            runOnUiThread(() -> {
                try {
                    Button customButton = findViewById(R.id.buttonCustom);
                    if (customButton != null) {
                        customButton.setOnClickListener(v -> showCustomAmountDialog());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback to default amounts on error
                runOnUiThread(() -> {
                    try {
                        int[] amounts = {100, 200, 250, 300, 500};
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
                        params.setMargins(4, 4, 4, 4);
                        
                        for (int amount : amounts) {
                            Button button = new Button(this);
                            button.setText(settingsHelper.formatAmount(this, amount));
                            button.setLayoutParams(params);
                            button.setTextColor(getResources().getColor(android.R.color.white));
                            button.setBackgroundTintList(getResources().getColorStateList(R.color.primary));
                            button.setOnClickListener(v -> addWater(amount, ""));
                            quickAddLayout.addView(button);
                        }
                        
                        Button customButton = findViewById(R.id.buttonCustom);
                        if (customButton != null) {
                            customButton.setOnClickListener(v -> showCustomAmountDialog());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });
    }
    
    private void observeData() {
        try {
            waterViewModel.getTodayTotal().observe(this, total -> {
                try {
                    int goal = settingsHelper.getDailyGoalMl();
                    updateProgress(total != null ? total : 0, goal);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            
            waterViewModel.getTodayEntries().observe(this, entries -> {
                try {
                    if (recyclerViewEntries == null || emptyStateView == null || adapter == null) return;
                    
                    if (entries == null || entries.isEmpty()) {
                        recyclerViewEntries.setVisibility(View.GONE);
                        emptyStateView.setVisibility(View.VISIBLE);
                    } else {
                        recyclerViewEntries.setVisibility(View.VISIBLE);
                        emptyStateView.setVisibility(View.GONE);
                        adapter.setEntries(entries);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void updateProgress(int consumed, int goal) {
        try {
            if (progressBar == null || textViewProgress == null || textViewRemaining == null || textViewStatus == null) {
                return;
            }
            
            int percentage = goal > 0 ? (int) ((consumed * 100) / goal) : 0;
            int remaining = Math.max(0, goal - consumed);
            
            // Animate progress bar
            progressBar.setProgress(percentage);
            
            textViewProgress.setText(settingsHelper.formatAmount(this, consumed) + " / " + settingsHelper.formatAmount(this, goal));
            textViewRemaining.setText(getString(R.string.remaining) + ": " + settingsHelper.formatAmount(this, remaining));
            
            if (consumed >= goal) {
                textViewStatus.setText("🎉 " + getString(R.string.on_track));
                textViewStatus.setTextColor(getResources().getColor(R.color.accent));
                // Celebration animation
                textViewStatus.animate().scaleX(1.1f).scaleY(1.1f).setDuration(300)
                    .withEndAction(() -> textViewStatus.animate().scaleX(1f).scaleY(1f).setDuration(300).start())
                    .start();
            } else if (percentage >= 75) {
                textViewStatus.setText("Almost there! 💪");
                textViewStatus.setTextColor(getResources().getColor(R.color.primary_light));
            } else if (percentage >= 50) {
                textViewStatus.setText("Halfway there! 🌊");
                textViewStatus.setTextColor(getResources().getColor(R.color.primary));
            } else {
                textViewStatus.setText("Keep going! 💧");
                textViewStatus.setTextColor(getResources().getColor(R.color.text_white_secondary));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addWater(int amountMl, String note) {
        try {
            WaterEntry entry = new WaterEntry(amountMl, System.currentTimeMillis(), note);
            waterViewModel.insertWaterEntry(entry);
            
            // Check achievements
            try {
                int goal = settingsHelper.getDailyGoalMl();
                AchievementChecker.checkAchievements(this, goal);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // Animate entry addition
            if (recyclerViewEntries != null && recyclerViewEntries.getVisibility() == View.VISIBLE) {
                recyclerViewEntries.smoothScrollToPosition(0);
            }
            
            // Show success animation
            if (progressBar != null) {
                progressBar.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200)
                    .withEndAction(() -> {
                        if (progressBar != null) {
                            progressBar.animate().scaleX(1f).scaleY(1f).setDuration(200).start();
                        }
                    })
                    .start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error adding water: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showCustomAmountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.custom_amount);
        
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint(R.string.ml);
        builder.setView(input);
        
        builder.setPositiveButton("Add", (dialog, which) -> {
            try {
                int amount = Integer.parseInt(input.getText().toString());
                if (amount > 0) {
                    addWater(amount, "");
                } else {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showUndoSnackbar() {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), "Entry deleted", Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", v -> {
            if (deletedEntry != null) {
                waterViewModel.insertWaterEntry(deletedEntry);
                deletedEntry = null;
            }
        });
        snackbar.show();
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

