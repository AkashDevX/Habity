package com.trackapp.habity.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "completions",
    foreignKeys = {
        @ForeignKey(
            entity = HabitEntity.class,
            parentColumns = "id",
            childColumns = "habitId",
            onDelete = ForeignKey.CASCADE
        ),
        @ForeignKey(
            entity = ReminderEntity.class,
            parentColumns = "id",
            childColumns = "reminderId",
            onDelete = ForeignKey.SET_NULL
        )
    },
    indices = {@Index("habitId"), @Index("reminderId")},
    primaryKeys = {"date", "habitId"}
)
public class CompletionEntity {
    // Date format: "yyyy-MM-dd"
    @NonNull
    public String date;
    
    public long habitId;
    
    public Long reminderId; // Nullable foreign key to reminders table
    
    public boolean done;
    
    public CompletionEntity() {}
    
    public CompletionEntity(String date, long habitId, boolean done) {
        this.date = date;
        this.habitId = habitId;
        this.done = done;
        this.reminderId = null;
    }
    
    public CompletionEntity(String date, long habitId, Long reminderId, boolean done) {
        this.date = date;
        this.habitId = habitId;
        this.reminderId = reminderId;
        this.done = done;
    }
}

