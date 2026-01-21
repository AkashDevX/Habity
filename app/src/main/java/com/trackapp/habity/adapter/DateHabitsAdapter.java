package com.trackapp.habity.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.trackapp.habity.R;

import java.util.List;

public class DateHabitsAdapter extends RecyclerView.Adapter<DateHabitsAdapter.DateHabitsViewHolder> {
    private List<DateHabitsItem> items;
    
    public DateHabitsAdapter() {
    }
    
    public void setItems(List<DateHabitsItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public DateHabitsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_date_habits, parent, false);
        return new DateHabitsViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull DateHabitsViewHolder holder, int position) {
        DateHabitsItem item = items.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }
    
    class DateHabitsViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDate;
        private RecyclerView recyclerViewReminders;
        
        public DateHabitsViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            recyclerViewReminders = itemView.findViewById(R.id.recyclerViewReminders);
        }
        
        public void bind(DateHabitsItem item) {
            textViewDate.setText(item.date);
            
            // Setup nested RecyclerView for reminders
            ReminderListAdapter reminderAdapter = new ReminderListAdapter();
            reminderAdapter.setReminders(item.reminders);
            recyclerViewReminders.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(itemView.getContext()));
            recyclerViewReminders.setAdapter(reminderAdapter);
        }
    }
    
    private static class ReminderListAdapter extends RecyclerView.Adapter<ReminderViewHolder> {
        private List<ReminderItem> reminders;
        
        public void setReminders(List<ReminderItem> reminders) {
            this.reminders = reminders;
            notifyDataSetChanged();
        }
        
        @NonNull
        @Override
        public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reminder_date, parent, false);
            return new ReminderViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
            ReminderItem reminder = reminders.get(position);
            holder.bind(reminder);
        }
        
        @Override
        public int getItemCount() {
            return reminders == null ? 0 : reminders.size();
        }
    }
    
    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewStatus;
        private TextView textViewHabitTitle;
        private TextView textViewReminderTime;
        private CardView cardView;
        
        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewStatus = itemView.findViewById(R.id.textViewStatus);
            textViewHabitTitle = itemView.findViewById(R.id.textViewHabitTitle);
            textViewReminderTime = itemView.findViewById(R.id.textViewReminderTime);
            cardView = itemView.findViewById(R.id.cardViewReminder);
        }
        
        public void bind(ReminderItem reminder) {
            textViewHabitTitle.setText(reminder.habitTitle);
            textViewReminderTime.setText(reminder.reminderTime);
            
            if (reminder.completed) {
                textViewStatus.setText("✓");
                cardView.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
                textViewHabitTitle.setTextColor(Color.WHITE);
                textViewReminderTime.setTextColor(Color.WHITE);
                textViewStatus.setTextColor(Color.WHITE);
            } else {
                textViewStatus.setText("○");
                cardView.setCardBackgroundColor(Color.WHITE);
                textViewHabitTitle.setTextColor(Color.BLACK);
                textViewReminderTime.setTextColor(Color.GRAY);
                textViewStatus.setTextColor(Color.BLACK);
            }
        }
    }
    
    public static class DateHabitsItem {
        String date;
        List<ReminderItem> reminders;
        
        public DateHabitsItem(String date, List<ReminderItem> reminders) {
            this.date = date;
            this.reminders = reminders;
        }
    }
    
    public static class ReminderItem {
        long reminderId;
        long habitId;
        String habitTitle;
        String reminderTime;
        boolean completed;
        
        public ReminderItem(long reminderId, long habitId, String habitTitle, String reminderTime, boolean completed) {
            this.reminderId = reminderId;
            this.habitId = habitId;
            this.habitTitle = habitTitle;
            this.reminderTime = reminderTime;
            this.completed = completed;
        }
    }
}

