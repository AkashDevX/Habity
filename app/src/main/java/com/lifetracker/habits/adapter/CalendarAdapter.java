package com.lifetracker.habits.adapter;

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

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarDayViewHolder> {
    private List<CalendarDay> days;
    private OnDayClickListener onDayClickListener;
    
    public interface OnDayClickListener {
        void onDayClick(String date);
        void onDayLongClick(String date);
    }
    
    public CalendarAdapter() {
    }
    
    public void setDays(List<CalendarDay> days) {
        this.days = days;
        notifyDataSetChanged();
    }
    
    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }
    
    @NonNull
    @Override
    public CalendarDayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarDayViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CalendarDayViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        holder.bind(day);
    }
    
    @Override
    public int getItemCount() {
        return days == null ? 0 : days.size();
    }
    
    class CalendarDayViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewDay;
        private CardView cardView;
        
        public CalendarDayViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDay = itemView.findViewById(R.id.textViewDay);
            cardView = itemView.findViewById(R.id.cardView);
        }
        
        public void bind(CalendarDay day) {
            textViewDay.setText(day.day);
            
            if (day.day.isEmpty()) {
                // Empty cell
                textViewDay.setVisibility(View.INVISIBLE);
                cardView.setCardBackgroundColor(Color.TRANSPARENT);
                cardView.setCardElevation(0);
                itemView.setOnClickListener(null);
            } else {
                textViewDay.setVisibility(View.VISIBLE);
                
                // Set background color based on completion status
                if (day.hasCompleted) {
                    // Completed days (done == 1) - green color
                    cardView.setCardBackgroundColor(Color.parseColor("#4CAF50")); // Green
                    textViewDay.setTextColor(Color.WHITE);
                } else {
                    // Not completed (done == 0 or no entry) - yellow color
                    cardView.setCardBackgroundColor(Color.parseColor("#FFEB3B")); // Yellow
                    textViewDay.setTextColor(Color.BLACK);
                }
                
                cardView.setCardElevation(2);
                
                // Set click listener
                itemView.setOnClickListener(v -> {
                    if (onDayClickListener != null && day.date != null) {
                        onDayClickListener.onDayClick(day.date);
                    }
                });
                
                // Set long click listener
                itemView.setOnLongClickListener(v -> {
                    if (onDayClickListener != null && day.date != null) {
                        onDayClickListener.onDayLongClick(day.date);
                        return true;
                    }
                    return false;
                });
            }
        }
    }
    
    public static class CalendarDay {
        String day;
        String date;
        boolean isToday;
        boolean hasCompleted;
        
        public CalendarDay(String day, String date, boolean isToday, boolean hasCompleted) {
            this.day = day;
            this.date = date;
            this.isToday = isToday;
            this.hasCompleted = hasCompleted;
        }
    }
}

