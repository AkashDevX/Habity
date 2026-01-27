package com.carmaintenance.tracker;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.carmaintenance.tracker.R;
import com.carmaintenance.tracker.database.AppDatabase;
import com.carmaintenance.tracker.database.FuelDao;
import com.carmaintenance.tracker.database.FuelEntity;
import com.carmaintenance.tracker.viewmodel.FuelViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditFuelActivity extends AppCompatActivity {
    private TextInputEditText editTextDate;
    private TextInputEditText editTextAmount;
    private TextInputEditText editTextPrice;
    private TextInputEditText editTextMileage;
    private TextInputEditText editTextFuelType;
    private TextInputEditText editTextStation;
    private TextInputEditText editTextNotes;
    private MaterialButton buttonSave;
    
    private FuelViewModel fuelViewModel;
    private ExecutorService executor;
    private long fuelId = -1;
    private boolean isEditMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_fuel);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }
        
        executor = Executors.newSingleThreadExecutor();
        fuelViewModel = new ViewModelProvider(this).get(FuelViewModel.class);
        
        editTextDate = findViewById(R.id.editTextDate);
        editTextAmount = findViewById(R.id.editTextAmount);
        editTextPrice = findViewById(R.id.editTextPrice);
        editTextMileage = findViewById(R.id.editTextMileage);
        editTextFuelType = findViewById(R.id.editTextFuelType);
        editTextStation = findViewById(R.id.editTextStation);
        editTextNotes = findViewById(R.id.editTextNotes);
        buttonSave = findViewById(R.id.buttonSave);
        
        // Check if editing existing fuel log
        if (getIntent().hasExtra("fuelId")) {
            fuelId = getIntent().getLongExtra("fuelId", -1);
            isEditMode = true;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Edit Fuel Log");
            }
            loadFuelData();
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Add Fuel Log");
            }
            // Set default date to today
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            editTextDate.setText(dateFormat.format(new Date()));
            editTextFuelType.setText("Regular");
        }
        
        buttonSave.setOnClickListener(v -> saveFuel());
    }
    
    private void loadFuelData() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            FuelDao fuelDao = db.fuelDao();
            FuelEntity fuel = fuelDao.getFuelLogById(fuelId);
            
            if (fuel != null) {
                runOnUiThread(() -> {
                    editTextDate.setText(fuel.date != null ? fuel.date : "");
                    editTextAmount.setText(fuel.amount > 0 ? String.valueOf(fuel.amount) : "");
                    editTextPrice.setText(fuel.price > 0 ? String.valueOf(fuel.price) : "");
                    editTextMileage.setText(fuel.mileage > 0 ? String.valueOf(fuel.mileage) : "");
                    editTextFuelType.setText(fuel.fuelType != null ? fuel.fuelType : "Regular");
                    editTextStation.setText(fuel.station != null ? fuel.station : "");
                    editTextNotes.setText(fuel.notes != null ? fuel.notes : "");
                });
            }
        });
    }
    
    private void saveFuel() {
        String date = editTextDate.getText().toString().trim();
        String amountStr = editTextAmount.getText().toString().trim();
        String priceStr = editTextPrice.getText().toString().trim();
        String mileageStr = editTextMileage.getText().toString().trim();
        String fuelType = editTextFuelType.getText().toString().trim();
        String station = editTextStation.getText().toString().trim();
        String notes = editTextNotes.getText().toString().trim();
        
        if (date.isEmpty()) {
            Toast.makeText(this, "Please enter a date", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter fuel amount", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (priceStr.isEmpty()) {
            Toast.makeText(this, "Please enter price per liter", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (mileageStr.isEmpty()) {
            Toast.makeText(this, "Please enter mileage", Toast.LENGTH_SHORT).show();
            return;
        }
        
        final double amount;
        final double price;
        final int mileage;
        
        try {
            amount = Double.parseDouble(amountStr);
            price = Double.parseDouble(priceStr);
            mileage = Integer.parseInt(mileageStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (isEditMode) {
            executor.execute(() -> {
                AppDatabase db = AppDatabase.getDatabase(this);
                FuelDao fuelDao = db.fuelDao();
                FuelEntity fuel = fuelDao.getFuelLogById(fuelId);
                
                if (fuel != null) {
                    fuel.date = date;
                    fuel.amount = amount;
                    fuel.price = price;
                    fuel.totalCost = amount * price;
                    fuel.mileage = mileage;
                    fuel.fuelType = fuelType.isEmpty() ? "Regular" : fuelType;
                    fuel.station = station.isEmpty() ? null : station;
                    fuel.notes = notes.isEmpty() ? null : notes;
                    
                    fuelViewModel.updateFuel(fuel);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Fuel log updated", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                }
            });
        } else {
            FuelEntity fuel = new FuelEntity(date, amount, price, mileage, fuelType);
            fuel.station = station.isEmpty() ? null : station;
            fuel.notes = notes.isEmpty() ? null : notes;
            
            fuelViewModel.insertFuel(fuel);
            Toast.makeText(this, "Fuel log added", Toast.LENGTH_SHORT).show();
            finish();
        }
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

