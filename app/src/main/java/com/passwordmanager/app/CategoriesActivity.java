package com.passwordmanager.app;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.passwordmanager.app.adapter.CategoryAdapter;
import com.passwordmanager.app.viewmodel.CategoryViewModel;

public class CategoriesActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CategoryAdapter adapter;
    private CategoryViewModel viewModel;
    private FloatingActionButton fab;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
            getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.gradient_actionbar));
        }
        
        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        
        adapter = new CategoryAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        viewModel = new ViewModelProvider(this).get(CategoryViewModel.class);
        viewModel.getAllCategories().observe(this, categories -> {
            adapter.setCategories(categories);
        });
        
        fab.setOnClickListener(v -> {
            // TODO: Show dialog to add category
        });
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
