package com.lifetracker.habits.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trackapp.habity.R;
import com.lifetracker.habits.database.ReminderEntity;
import com.lifetracker.habits.reminders.ReminderScheduler;
import com.lifetracker.habits.viewmodel.ReminderViewModel;

import java.util.List;

public class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder> {
    private List<ReminderEntity> reminders;
    private ReminderViewModel viewModel;
    private Context context;
    private OnReminderDeletedListener onReminderDeletedListener;
    
    public interface OnReminderDeletedListener {
        void onDeleted();
    }
    
    public ReminderAdapter(ReminderViewModel viewModel, Context context) {
        this.viewModel = viewModel;
        this.context = context;
    }
    
    public void setReminders(List<ReminderEntity> reminders) {
        this.reminders = reminders;
        notifyDataSetChanged();
    }
    
    public void setOnReminderDeletedListener(OnReminderDeletedListener listener) {
        this.onReminderDeletedListener = listener;
    }
    
    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_reminder, parent, false);
        return new ReminderViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        ReminderEntity reminder = reminders.get(position);
        holder.bind(reminder);
    }
    
    @Override
    public int getItemCount() {
        return reminders == null ? 0 : reminders.size();
    }
    
    class ReminderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTime;
        private TextView textViewRepeatDays;
        private Switch switchEnabled;
        private ImageButton buttonDelete;
        
        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewRepeatDays = itemView.findViewById(R.id.textViewRepeatDays);
            switchEnabled = itemView.findViewById(R.id.switchReminderEnabled);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
        
        public void bind(ReminderEntity reminder) {
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
                viewModel.updateReminder(reminder);
                
                if (isChecked) {
                    ReminderScheduler.scheduleReminder(context, reminder.id);
                } else {
                    ReminderScheduler.cancelReminder(context, reminder.id);
                }
            });
            
            // Set up delete button
            buttonDelete.setOnClickListener(v -> {
                viewModel.deleteReminder(reminder);
                ReminderScheduler.cancelReminder(context, reminder.id);
                // Notify listener to reload
                if (onReminderDeletedListener != null) {
                    onReminderDeletedListener.onDeleted();
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
                if ((daysMask & dayBits[i]) != 0) {
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

