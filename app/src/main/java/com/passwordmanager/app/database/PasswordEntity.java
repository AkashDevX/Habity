package com.passwordmanager.app.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "passwords",
    foreignKeys = @ForeignKey(
        entity = CategoryEntity.class,
        parentColumns = "id",
        childColumns = "categoryId",
        onDelete = ForeignKey.SET_NULL
    ),
    indices = {@Index("categoryId")}
)
public class PasswordEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String title;
    public String username;
    public String encryptedPassword; // AES encrypted
    public String website;
    public String notes;
    public Long categoryId; // Nullable
    public long createdAt;
    public long updatedAt;
    
    public PasswordEntity(String title, String username, String encryptedPassword, 
                         String website, String notes, Long categoryId) {
        this.title = title;
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.website = website;
        this.notes = notes;
        this.categoryId = categoryId;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
    }
}
