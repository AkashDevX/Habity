package com.carmaintenance.tracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carmaintenance.tracker.R;
import com.carmaintenance.tracker.database.ServiceEntity;
import com.carmaintenance.tracker.reminders.ReminderScheduler;
import com.carmaintenance.tracker.viewmodel.ServiceViewModel;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder> {
    private List<ServiceEntity> services;
    private ServiceViewModel viewModel;
    private OnRemindersClickListener onRemindersClickListener;
    private OnServiceClickListener onServiceClickListener;
    private android.content.Context context;
    
    public interface OnRemindersClickListener {
        void onRemindersClick(long serviceId);
    }
    
    public interface OnServiceClickListener {
        void onServiceClick(long serviceId);
    }
    
    public ServiceAdapter(ServiceViewModel viewModel, android.content.Context context) {
        this.viewModel = viewModel;
        this.context = context;
    }
    
    public void setServices(List<ServiceEntity> services) {
        this.services = services;
        notifyDataSetChanged();
    }
    
    public void setOnRemindersClickListener(OnRemindersClickListener listener) {
        this.onRemindersClickListener = listener;
    }
    
    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.onServiceClickListener = listener;
    }
    
    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_service, parent, false);
        return new ServiceViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        ServiceEntity service = services.get(position);
        holder.bind(service);
    }
    
    @Override
    public int getItemCount() {
        return services == null ? 0 : services.size();
    }
    
    class ServiceViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private TextView textViewServiceType;
        private TextView textViewDate;
        private TextView textViewCost;
        private Switch switchEnabled;
        
        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewServiceTitle);
            textViewServiceType = itemView.findViewById(R.id.textViewServiceType);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewCost = itemView.findViewById(R.id.textViewCost);
            switchEnabled = itemView.findViewById(R.id.switchServiceEnabled);
            
            itemView.findViewById(R.id.buttonReminders).setOnClickListener(v -> {
                if (onRemindersClickListener != null && services != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onRemindersClickListener.onRemindersClick(services.get(pos).id);
                    }
                }
            });
            
            // Click on item to edit
            itemView.setOnClickListener(v -> {
                if (onServiceClickListener != null && services != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onServiceClickListener.onServiceClick(services.get(pos).id);
                    }
                }
            });
        }
        
        public void bind(ServiceEntity service) {
            textViewTitle.setText(service.title);
            textViewServiceType.setText(service.serviceType != null ? service.serviceType : "");
            textViewDate.setText(service.date != null ? service.date : "");
            if (service.cost != null) {
                textViewCost.setText(String.format("$%.2f", service.cost));
            } else {
                textViewCost.setText("");
            }
            switchEnabled.setChecked(service.enabled);
            
            // Remove previous listeners to avoid triggering while setting state
            switchEnabled.setOnCheckedChangeListener(null);
            
            // Set up enabled switch listener
            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                service.enabled = isChecked;
                viewModel.updateService(service);
                
                if (isChecked) {
                    // Reschedule all reminders for this service
                    ReminderScheduler.scheduleAllEnabled(context);
                } else {
                    // Cancel all reminders for this service
                    ReminderScheduler.cancelAllForService(context, service.id);
                }
            });
        }
    }
}
