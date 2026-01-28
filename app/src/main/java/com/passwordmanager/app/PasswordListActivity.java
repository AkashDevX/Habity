package com.passwordmanager.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.TextView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.passwordmanager.app.adapter.PasswordAdapter;
import com.passwordmanager.app.viewmodel.PasswordViewModel;

public class PasswordListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PasswordAdapter adapter;
    private PasswordViewModel viewModel;
    private ExtendedFloatingActionButton fab;
    private View emptyStateLayout;
    private TextView countTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_list);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.gradient_actionbar));
        }
        
        recyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        countTextView = findViewById(R.id.countTextView);
        
        adapter = new PasswordAdapter(this, password -> {
            Intent intent = new Intent(this, AddEditPasswordActivity.class);
            intent.putExtra("password_id", password.id);
            startActivity(intent);
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        viewModel = new ViewModelProvider(this).get(PasswordViewModel.class);
        viewModel.getAllPasswords().observe(this, passwords -> {
            adapter.setPasswords(passwords);
            int count = passwords != null ? passwords.size() : 0;
            countTextView.setText(count + (count == 1 ? " password saved" : " passwords saved"));
            
            if (passwords == null || passwords.isEmpty()) {
                emptyStateLayout.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                emptyStateLayout.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });
        
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditPasswordActivity.class);
            startActivity(intent);
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // Handle action bar back button - show exit confirmation
            showExitConfirmation();
            return true;
        }
//        if (item.getItemId() == R.id.action_categories) {
//            Intent intent = new Intent(this, CategoriesActivity.class);
//            startActivity(intent);
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }
    
    private void showExitConfirmation() {
        new AlertDialog.Builder(this, R.style.AlertDialogTheme)
            .setTitle("Exit App")
            .setMessage("Are you sure you want to exit the Password Manager?")
            .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finishAffinity();
                }
            })
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setCancelable(true)
            .show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }
    
    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }
}
