package com.carmaintenance.tracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.carmaintenance.tracker.R;
import com.carmaintenance.tracker.adapter.ServiceAdapter;
import com.carmaintenance.tracker.database.ServiceEntity;
import com.carmaintenance.tracker.viewmodel.ServiceViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class ServiceHistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerViewServices;
    private ServiceAdapter serviceAdapter;
    private ServiceViewModel serviceViewModel;
    private TextView textViewEmpty;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service_history);
        
        // Header is now in XML, no toolbar needed
        
        recyclerViewServices = findViewById(R.id.recyclerViewServices);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        com.google.android.material.floatingactionbutton.FloatingActionButton fabAddService = findViewById(R.id.fabAddService);
        
        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);
        serviceAdapter = new ServiceAdapter(serviceViewModel, this);
        
        recyclerViewServices.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewServices.setAdapter(serviceAdapter);
        
        // Setup click listeners
        serviceAdapter.setOnRemindersClickListener(serviceId -> {
            Intent intent = new Intent(this, RemindersActivity.class);
            intent.putExtra("serviceId", serviceId);
            startActivity(intent);
        });
        
        serviceAdapter.setOnServiceClickListener(serviceId -> {
            Intent intent = new Intent(this, AddEditServiceActivity.class);
            intent.putExtra("serviceId", serviceId);
            startActivity(intent);
        });
        
        // FAB - Add Service
        fabAddService.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditServiceActivity.class);
            startActivity(intent);
        });
        
        // Observe services
        serviceViewModel.getAllServices().observe(this, services -> {
            if (services != null) {
                serviceAdapter.setServices(services);
                if (services.isEmpty()) {
                    textViewEmpty.setVisibility(View.VISIBLE);
                    recyclerViewServices.setVisibility(View.GONE);
                } else {
                    textViewEmpty.setVisibility(View.GONE);
                    recyclerViewServices.setVisibility(View.VISIBLE);
                }
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_service_history, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
