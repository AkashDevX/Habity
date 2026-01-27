package com.carmaintenance.tracker.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.carmaintenance.tracker.R;
import com.carmaintenance.tracker.database.FuelEntity;

import java.util.List;
import java.util.Locale;

public class FuelAdapter extends RecyclerView.Adapter<FuelAdapter.FuelViewHolder> {
    private List<FuelEntity> fuelLogs;
    private OnFuelClickListener onFuelClickListener;
    private OnFuelDeleteListener onFuelDeleteListener;
    
    public interface OnFuelClickListener {
        void onFuelClick(long fuelId);
    }
    
    public interface OnFuelDeleteListener {
        void onFuelDelete(long fuelId);
    }
    
    public void setFuelLogs(List<FuelEntity> fuelLogs) {
        this.fuelLogs = fuelLogs;
        notifyDataSetChanged();
    }
    
    public void setOnFuelClickListener(OnFuelClickListener listener) {
        this.onFuelClickListener = listener;
    }
    
    public void setOnFuelDeleteListener(OnFuelDeleteListener listener) {
        this.onFuelDeleteListener = listener;
    }
    
    @NonNull
    @Override
    public FuelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_fuel, parent, false);
        return new FuelViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FuelViewHolder holder, int position) {
        FuelEntity fuel = fuelLogs.get(position);
        holder.bind(fuel);
    }
    
    @Override
    public int getItemCount() {
        return fuelLogs == null ? 0 : fuelLogs.size();
    }
    
    class FuelViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDate;
        private TextView textViewAmount;
        private TextView textViewPrice;
        private TextView textViewTotal;
        private TextView textViewMileage;
        private TextView textViewFuelType;
        private ImageButton buttonDelete;
        
        public FuelViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewPrice = itemView.findViewById(R.id.textViewPrice);
            textViewTotal = itemView.findViewById(R.id.textViewTotal);
            textViewMileage = itemView.findViewById(R.id.textViewMileage);
            textViewFuelType = itemView.findViewById(R.id.textViewFuelType);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            
            itemView.setOnClickListener(v -> {
                if (onFuelClickListener != null && fuelLogs != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onFuelClickListener.onFuelClick(fuelLogs.get(pos).id);
                    }
                }
            });
            
            if (buttonDelete != null) {
                buttonDelete.setOnClickListener(v -> {
                    if (onFuelDeleteListener != null && fuelLogs != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            onFuelDeleteListener.onFuelDelete(fuelLogs.get(pos).id);
                        }
                    }
                });
            }
        }
        
        public void bind(FuelEntity fuel) {
            textViewDate.setText(fuel.date != null ? fuel.date : "");
            textViewAmount.setText(String.format(Locale.getDefault(), "%.2f L", fuel.amount));
            textViewPrice.setText(String.format(Locale.getDefault(), "$%.2f/L", fuel.price));
            textViewTotal.setText(String.format(Locale.getDefault(), "$%.2f", fuel.totalCost));
            textViewMileage.setText(String.format(Locale.getDefault(), "%d km", fuel.mileage));
            textViewFuelType.setText(fuel.fuelType != null ? fuel.fuelType : "Regular");
        }
    }
}

