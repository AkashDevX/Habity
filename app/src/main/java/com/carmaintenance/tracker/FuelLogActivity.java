package com.carmaintenance.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carmaintenance.tracker.R;
import com.carmaintenance.tracker.adapter.FuelAdapter;
import com.carmaintenance.tracker.database.AppDatabase;
import com.carmaintenance.tracker.database.FuelEntity;
import com.carmaintenance.tracker.viewmodel.FuelViewModel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FuelLogActivity extends AppCompatActivity {
    private RecyclerView recyclerViewFuel;
    private FuelAdapter fuelAdapter;
    private FuelViewModel fuelViewModel;
    private TextView textViewEmpty;
    private View layoutEmpty;
    private ExecutorService executor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_log);
        
        // Header is now in XML, no toolbar needed
        
        executor = Executors.newSingleThreadExecutor();
        
        recyclerViewFuel = findViewById(R.id.recyclerViewFuel);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        com.google.android.material.button.MaterialButton buttonAddFuel = findViewById(R.id.buttonAddFuel);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabAddFuel = findViewById(R.id.fabAddFuel);
        
        fuelViewModel = new ViewModelProvider(this).get(FuelViewModel.class);
        fuelAdapter = new FuelAdapter();
        
        recyclerViewFuel.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFuel.setAdapter(fuelAdapter);
        
        // Setup click listener for editing
        fuelAdapter.setOnFuelClickListener(fuelId -> {
            Intent intent = new Intent(this, AddEditFuelActivity.class);
            intent.putExtra("fuelId", fuelId);
            startActivity(intent);
        });
        
        // Setup delete listener
        fuelAdapter.setOnFuelDeleteListener(fuelId -> {
            new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Delete Fuel Entry")
                .setMessage("Are you sure you want to delete this fuel entry?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    executor.execute(() -> {
                        AppDatabase db = AppDatabase.getDatabase(this);
                        com.carmaintenance.tracker.database.FuelDao fuelDao = db.fuelDao();
                        FuelEntity fuel = fuelDao.getFuelLogById(fuelId);
                        if (fuel != null) {
                            fuelViewModel.deleteFuel(fuel);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
        });
        
        // Add fuel button (empty state)
        buttonAddFuel.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditFuelActivity.class);
            startActivity(intent);
        });
        
        // FAB - always visible
        fabAddFuel.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditFuelActivity.class);
            startActivity(intent);
        });
        
        // Observe fuel logs
        fuelViewModel.getAllFuelLogs().observe(this, fuelLogs -> {
            if (fuelLogs != null) {
                fuelAdapter.setFuelLogs(fuelLogs);
                if (fuelLogs.isEmpty()) {
                    layoutEmpty.setVisibility(View.VISIBLE);
                    recyclerViewFuel.setVisibility(View.GONE);
                } else {
                    layoutEmpty.setVisibility(View.GONE);
                    recyclerViewFuel.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
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
