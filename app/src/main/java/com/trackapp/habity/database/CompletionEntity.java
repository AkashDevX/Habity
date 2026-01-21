package com.trackapp.habity.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
    tableName = "completions",
    foreignKeys = @ForeignKey(
        entity = HabitEntity.class,
        parentColumns = "id",
        childColumns = "habitId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {@Index("habitId")},
    primaryKeys = {"date", "habitId"}
)
public class CompletionEntity {
    // Date format: "yyyy-MM-dd"
    @NonNull
    public String date;
    
    public long habitId;
    
    public boolean done;
    
    public CompletionEntity() {}
    
    public CompletionEntity(String date, long habitId, boolean done) {
        this.date = date;
        this.habitId = habitId;
        this.done = done;
    }
}

