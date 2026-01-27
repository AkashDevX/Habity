package com.carmaintenance.tracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "fuel_logs")
public class FuelEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String date; // Date of fuel fill (format: "yyyy-MM-dd")
    public double amount; // Amount of fuel in liters/gallons
    public double price; // Price per unit
    public double totalCost; // Total cost
    public int mileage; // Odometer reading at time of fill
    public String fuelType; // Type of fuel (e.g., "Regular", "Premium", "Diesel")
    public String station; // Gas station name (optional)
    public String notes; // Additional notes (optional)
    
    public FuelEntity() {
    }
    
    public FuelEntity(String date, double amount, double price, int mileage, String fuelType) {
        this.date = date;
        this.amount = amount;
        this.price = price;
        this.totalCost = amount * price;
        this.mileage = mileage;
        this.fuelType = fuelType != null ? fuelType : "Regular";
    }
}

