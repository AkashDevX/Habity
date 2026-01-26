package com.lifetracker.habits;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.lifetracker.habits.adapter.CategoryAdapter;
import com.lifetracker.habits.database.AppDatabase;
import com.lifetracker.habits.database.CategoryEntity;
import com.trackapp.habity.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoriesActivity extends AppCompatActivity {
    private RecyclerView recyclerViewCategories;
    private CategoryAdapter categoryAdapter;
    private FloatingActionButton fabAddCategory;
    private ExecutorService executor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Categories");
        }
        
        executor = Executors.newSingleThreadExecutor();
        
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        fabAddCategory = findViewById(R.id.fabAddCategory);
        
        categoryAdapter = new CategoryAdapter();
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategories.setAdapter(categoryAdapter);
        
        loadCategories();
        
        fabAddCategory.setOnClickListener(v -> {
            // For now, show a toast - you can add a dialog to create categories
            Toast.makeText(this, "Category creation coming soon!", Toast.LENGTH_SHORT).show();
        });
    }
    
    private void loadCategories() {
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getDatabase(this);
            List<CategoryEntity> categories = db.categoryDao().getAllCategoriesSync();
            
            runOnUiThread(() -> {
                categoryAdapter.setCategories(categories);
            });
        });
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

