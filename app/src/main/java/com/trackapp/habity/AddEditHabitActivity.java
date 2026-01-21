package com.trackapp.habity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.trackapp.habity.database.HabitEntity;
import com.trackapp.habity.viewmodel.HabitViewModel;

public class AddEditHabitActivity extends AppCompatActivity {
    private TextInputEditText editTextTitle;
    private Button buttonSave;
    private HabitViewModel habitViewModel;
    private long habitId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_habit);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getIntent().hasExtra("habitId") ? "Edit Habit" : "Add Habit");
        }
        
        editTextTitle = findViewById(R.id.editTextTitle);
        buttonSave = findViewById(R.id.buttonSave);
        
        habitViewModel = new ViewModelProvider(this).get(HabitViewModel.class);
        
        // Check if editing existing habit
        if (getIntent().hasExtra("habitId")) {
            habitId = getIntent().getLongExtra("habitId", -1);
            // Load habit and populate fields (for now, we'll just create new habits)
        }
        
        buttonSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a habit title", Toast.LENGTH_SHORT).show();
                return;
            }
            
            HabitEntity habit = new HabitEntity(title, true);
            habitViewModel.insertHabit(habit);
            
            Toast.makeText(this, "Habit saved", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}

