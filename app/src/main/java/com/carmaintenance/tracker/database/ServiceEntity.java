package com.carmaintenance.tracker.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "services")
public class ServiceEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String title; // Service type/name (e.g., "Oil Change", "Tire Rotation", "Brake Inspection")
    public String serviceType; // Type of service (e.g., "Maintenance", "Repair", "Inspection", "Insurance")
    public String date; // Date when service was performed (format: "yyyy-MM-dd")
    public Double cost; // Cost of the service
    public Long mileage; // Mileage/odometer reading at time of service
    public String notes; // Additional notes about the service
    public String icon; // Optional emoji icon
    public String color; // Optional color for the service
    public boolean enabled; // Whether reminders are enabled for this service
    
    public ServiceEntity() {
        this.enabled = true;
    }
    
    public ServiceEntity(String title, String serviceType) {
        this.title = title;
        this.serviceType = serviceType;
        this.enabled = true;
    }
}





