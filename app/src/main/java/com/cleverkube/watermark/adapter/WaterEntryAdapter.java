package com.cleverkube.watermark.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cleverkube.watermark.R;
import com.cleverkube.watermark.database.WaterEntry;
import com.cleverkube.watermark.utils.SettingsHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WaterEntryAdapter extends RecyclerView.Adapter<WaterEntryAdapter.WaterEntryViewHolder> {
    private List<WaterEntry> entries;
    private OnDeleteClickListener onDeleteClickListener;
    private SettingsHelper settingsHelper;
    
    public interface OnDeleteClickListener {
        void onDeleteClick(WaterEntry entry);
    }
    
    public WaterEntryAdapter(SettingsHelper settingsHelper) {
        this.settingsHelper = settingsHelper;
    }
    
    public void setEntries(List<WaterEntry> entries) {
        this.entries = entries;
        notifyDataSetChanged();
    }
    
    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        this.onDeleteClickListener = listener;
    }
    
    @NonNull
    @Override
    public WaterEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_water_entry, parent, false);
        return new WaterEntryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WaterEntryViewHolder holder, int position) {
        WaterEntry entry = entries.get(position);
        holder.bind(entry);
    }
    
    @Override
    public int getItemCount() {
        return entries == null ? 0 : entries.size();
    }
    
    class WaterEntryViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewAmount;
        private TextView textViewTime;
        private TextView textViewNote;
        private View deleteButton;
        
        public WaterEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewAmount = itemView.findViewById(R.id.textViewAmount);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewNote = itemView.findViewById(R.id.textViewNote);
            deleteButton = itemView.findViewById(R.id.buttonDelete);
        }
        
        public void bind(WaterEntry entry) {
            String amount = settingsHelper.formatAmount(itemView.getContext(), entry.amountMl);
            textViewAmount.setText(amount);
            
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            textViewTime.setText(timeFormat.format(new Date(entry.timestamp)));
            
            if (entry.note != null && !entry.note.trim().isEmpty()) {
                textViewNote.setVisibility(View.VISIBLE);
                textViewNote.setText(entry.note);
            } else {
                textViewNote.setVisibility(View.GONE);
            }
            
            deleteButton.setOnClickListener(v -> {
                if (onDeleteClickListener != null) {
                    onDeleteClickListener.onDeleteClick(entry);
                }
            });
        }
    }
}


