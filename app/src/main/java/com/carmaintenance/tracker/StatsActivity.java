package com.carmaintenance.tracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.carmaintenance.tracker.database.AppDatabase;
import com.carmaintenance.tracker.database.ServiceDao;
import com.carmaintenance.tracker.database.ServiceEntity;
import com.carmaintenance.tracker.viewmodel.ServiceViewModel;
import com.carmaintenance.tracker.R;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatsActivity extends AppCompatActivity {
    private TextView textViewTotalServices;
    private TextView textViewTotalCost;
    private TextView textViewAverageCost;
    private TextView textViewLastServiceDate;
    private TextView textViewThisMonth;
    private TextView textViewThisYear;
    private LinearLayout linearLayoutServiceTypes;
    private ExecutorService executor;
    private ServiceViewModel serviceViewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);
        
        // Header is now in XML, no toolbar needed
        
        executor = Executors.newSingleThreadExecutor();
        serviceViewModel = new ViewModelProvider(this).get(ServiceViewModel.class);
        
        textViewTotalServices = findViewById(R.id.textViewTotalServices);
        textViewTotalCost = findViewById(R.id.textViewTotalCost);
        textViewAverageCost = findViewById(R.id.textViewAverageCost);
        textViewLastServiceDate = findViewById(R.id.textViewLastServiceDate);
        textViewThisMonth = findViewById(R.id.textViewThisMonth);
        textViewThisYear = findViewById(R.id.textViewThisYear);
        linearLayoutServiceTypes = findViewById(R.id.linearLayoutServiceTypes);
        
        loadStatistics();
    }
    
    private void loadStatistics() {
        executor.execute(() -> {
            try {
                AppDatabase db = AppDatabase.getDatabase(this);
                ServiceDao serviceDao = db.serviceDao();
                
                List<ServiceEntity> services = serviceDao.getAllServicesSync();
                if (services == null) {
                    services = new java.util.ArrayList<>();
                }
                int totalServices = services.size();
            
            double totalCost = 0.0;
            String lastServiceDate = "N/A";
            Calendar calendar = Calendar.getInstance();
            int currentMonth = calendar.get(Calendar.MONTH);
            int currentYear = calendar.get(Calendar.YEAR);
            int servicesThisMonth = 0;
            int servicesThisYear = 0;
            
            Map<String, Integer> servicesByType = new HashMap<>();
            Map<String, Double> costByType = new HashMap<>();
            
            for (ServiceEntity service : services) {
                // Calculate total cost
                if (service.cost != null) {
                    totalCost += service.cost;
                }
                
                // Find last service date
                if (service.date != null && !service.date.isEmpty()) {
                    if (lastServiceDate.equals("N/A") || service.date.compareTo(lastServiceDate) > 0) {
                        lastServiceDate = service.date;
                    }
                    
                    // Parse date to check month/year
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        Date serviceDate = sdf.parse(service.date);
                        if (serviceDate != null) {
                            Calendar serviceCalendar = Calendar.getInstance();
                            serviceCalendar.setTime(serviceDate);
                            if (serviceCalendar.get(Calendar.YEAR) == currentYear) {
                                servicesThisYear++;
                                if (serviceCalendar.get(Calendar.MONTH) == currentMonth) {
                                    servicesThisMonth++;
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Ignore parsing errors
                    }
                }
                
                // Count by type
                String type = (service.serviceType != null && !service.serviceType.isEmpty()) 
                    ? service.serviceType : "Other";
                servicesByType.put(type, servicesByType.getOrDefault(type, 0) + 1);
                if (service.cost != null) {
                    costByType.put(type, costByType.getOrDefault(type, 0.0) + service.cost);
                }
            }
            
            double averageCost = totalServices > 0 ? totalCost / totalServices : 0.0;
            
            final int finalTotalServices = totalServices;
            final double finalTotalCost = totalCost;
            final double finalAverageCost = averageCost;
            final String finalLastServiceDate = lastServiceDate;
            final int finalServicesThisMonth = servicesThisMonth;
            final int finalServicesThisYear = servicesThisYear;
            final Map<String, Integer> finalServicesByType = servicesByType;
            final Map<String, Double> finalCostByType = costByType;
            
                runOnUiThread(() -> {
                    try {
                        textViewTotalServices.setText(String.valueOf(finalTotalServices));
                        textViewTotalCost.setText(String.format(Locale.getDefault(), "$%.2f", finalTotalCost));
                        textViewAverageCost.setText(String.format(Locale.getDefault(), "$%.2f", finalAverageCost));
                        textViewLastServiceDate.setText(finalLastServiceDate);
                        textViewThisMonth.setText(String.valueOf(finalServicesThisMonth));
                        textViewThisYear.setText(String.valueOf(finalServicesThisYear));
                        
                        // Populate service types
                        linearLayoutServiceTypes.removeAllViews();
                        for (Map.Entry<String, Integer> entry : finalServicesByType.entrySet()) {
                            View typeView = createServiceTypeView(entry.getKey(), entry.getValue(), 
                                finalCostByType.getOrDefault(entry.getKey(), 0.0));
                            linearLayoutServiceTypes.addView(typeView);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    textViewTotalServices.setText("0");
                    textViewTotalCost.setText("$0.00");
                    textViewAverageCost.setText("$0.00");
                    textViewLastServiceDate.setText("N/A");
                    textViewThisMonth.setText("0");
                    textViewThisYear.setText("0");
                });
            }
        });
    }
    
    private View createServiceTypeView(String type, int count, double cost) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.item_service_type_stat, linearLayoutServiceTypes, false);
        
        TextView textViewType = view.findViewById(R.id.textViewType);
        TextView textViewCount = view.findViewById(R.id.textViewCount);
        TextView textViewCost = view.findViewById(R.id.textViewCost);
        
        textViewType.setText(type);
        textViewCount.setText(String.format(Locale.getDefault(), "%d services", count));
        textViewCost.setText(String.format(Locale.getDefault(), "$%.2f", cost));
        
        return view;
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
