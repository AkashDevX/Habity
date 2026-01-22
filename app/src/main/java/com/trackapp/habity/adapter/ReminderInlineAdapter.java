package com.trackapp.habity.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trackapp.habity.R;
import com.trackapp.habity.database.ReminderEntity;

import java.util.ArrayList;
import java.util.List;

public class ReminderInlineAdapter extends RecyclerView.Adapter<ReminderInlineAdapter.ReminderViewHolder> {
    private List<ReminderEntity> reminders;
    private OnReminderActionListener listener;
    
    public interface OnReminderActionListener {
        void onReminderEnabledChanged(ReminderEntity reminder, boolean enabled);
        void onReminderEdit(ReminderEntity reminder, int position);
        void onReminderDelete(ReminderEntity reminder, int position);
    }
    
    public ReminderInlineAdapter() {
        this.reminders = new ArrayList<>();
    }
    
    public void setReminders(List<ReminderEntity> reminders) {
        this.reminders = reminders != null ? reminders : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    public List<ReminderEntity> getReminders() {
        return reminders;
    }
    
    public void addReminder(ReminderEntity reminder) {
        reminders.add(reminder);
        notifyItemInserted(reminders.size() - 1);
    }
    
    public void updateReminder(int position, ReminderEntity reminder) {
        if (position >= 0 && position < reminders.size()) {
            reminders.set(position, reminder);
            notifyItemChanged(position);
        }
    }
    
    public void removeReminder(int position) {
        if (position >= 0 && position < reminders.size()) {
            reminders.remove(position);
            notifyItemRemoved(position);
        }
    }
    
    public void setOnReminderActionListener(OnReminderActionListener listener) {
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_reminder_inline, parent, false);
        return new ReminderViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderEntity reminder = reminders.get(position);
        holder.bind(reminder, position);
    }
    
    @Override
    public int getItemCount() {
        return reminders.size();
    }
    
    class ReminderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTime;
        private TextView textViewRepeatDays;
        private Switch switchEnabled;
        private ImageButton buttonEdit;
        private ImageButton buttonDelete;
        
        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewRepeatDays = itemView.findViewById(R.id.textViewRepeatDays);
            switchEnabled = itemView.findViewById(R.id.switchReminderEnabled);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
        
        public void bind(ReminderEntity reminder, int position) {
            // Format time
            String timeStr = String.format("%02d:%02d", reminder.hour, reminder.minute);
            textViewTime.setText(timeStr);
            
            // Format repeat days
            String repeatStr = formatDaysMask(reminder.daysMask);
            textViewRepeatDays.setText(repeatStr);
            
            // Remove previous listeners
            switchEnabled.setOnCheckedChangeListener(null);
            
            switchEnabled.setChecked(reminder.enabled);
            
            // Set up enabled switch listener
            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                reminder.enabled = isChecked;
                if (listener != null) {
                    listener.onReminderEnabledChanged(reminder, isChecked);
                }
            });
            
            // Set up edit button
            buttonEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReminderEdit(reminder, position);
                }
            });
            
            // Set up delete button
            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onReminderDelete(reminder, position);
                }
            });
        }
        
        private String formatDaysMask(int daysMask) {
            if (daysMask == 127) {
                return "Every day";
            }
            
            // Show days that ARE selected (where bit is 1)
            String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
            int[] dayBits = {1, 2, 4, 8, 16, 32, 64};
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < dayBits.length; i++) {
                // Check if day IS in the selection (bit is 1)
                if ((daysMask & dayBits[i]) == 0) {
                    if (sb.length() > 0) {
                        sb.append(", ");
                    }
                    sb.append(dayNames[i]);
                }
            }
            
            return sb.toString();
        }
    }
}


