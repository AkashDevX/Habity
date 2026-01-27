package com.cleverkube.watermark.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.cleverkube.watermark.R;

import java.util.ArrayList;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarDayViewHolder> {
    private List<CalendarDay> days;
    private OnDayClickListener onDayClickListener;
    private OnDayLongClickListener onDayLongClickListener;
    private String selectedDate;
    
    public interface OnDayClickListener {
        void onDayClick(String date);
    }
    
    public interface OnDayLongClickListener {
        void onDayLongClick(String date);
    }
    
    public CalendarAdapter() {
        this.days = new ArrayList<>();
    }
    
    public void setDays(List<CalendarDay> days) {
        this.days = days;
        notifyDataSetChanged();
    }
    
    public void setOnDayClickListener(OnDayClickListener listener) {
        this.onDayClickListener = listener;
    }
    
    public void setOnDayLongClickListener(OnDayLongClickListener listener) {
        this.onDayLongClickListener = listener;
    }
    
    public void setSelectedDate(String date) {
        String oldSelected = selectedDate;
        selectedDate = date;
        if (oldSelected != null) {
            int oldPos = findPositionByDate(oldSelected);
            if (oldPos >= 0) notifyItemChanged(oldPos);
        }
        int newPos = findPositionByDate(date);
        if (newPos >= 0) notifyItemChanged(newPos);
    }
    
    private int findPositionByDate(String date) {
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i).date != null && days.get(i).date.equals(date)) {
                return i;
            }
        }
        return -1;
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
        return days.size();
    }
    
    class CalendarDayViewHolder extends RecyclerView.ViewHolder {
        TextView textViewDay;
        CardView cardView;
        
        CalendarDayViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDay = itemView.findViewById(R.id.textViewDay);
            cardView = itemView.findViewById(R.id.cardView);
        }
        
        void bind(CalendarDay day) {
            if (day.dayNumber.isEmpty()) {
                textViewDay.setText("");
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.background_purple));
                cardView.setClickable(false);
                return;
            }
            
            textViewDay.setText(day.dayNumber);
            cardView.setClickable(true);
            
            // Highlight selected date
            if (day.date != null && day.date.equals(selectedDate)) {
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.primary));
                textViewDay.setTextColor(itemView.getContext().getResources().getColor(R.color.text_white));
            } else if (day.isToday) {
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.primary_light));
                textViewDay.setTextColor(itemView.getContext().getResources().getColor(R.color.text_white));
            } else if (day.hasData) {
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.accent));
                textViewDay.setTextColor(itemView.getContext().getResources().getColor(R.color.text_white));
            } else {
                cardView.setCardBackgroundColor(itemView.getContext().getResources().getColor(R.color.card_purple));
                textViewDay.setTextColor(itemView.getContext().getResources().getColor(R.color.text_white));
            }
            
            itemView.setOnClickListener(v -> {
                if (day.date != null && onDayClickListener != null) {
                    onDayClickListener.onDayClick(day.date);
                }
            });
            
            itemView.setOnLongClickListener(v -> {
                if (day.date != null && onDayLongClickListener != null) {
                    onDayLongClickListener.onDayLongClick(day.date);
                    return true;
                }
                return false;
            });
        }
    }
    
    public static class CalendarDay {
        String dayNumber;
        String date; // Format: "yyyy-MM-dd"
        boolean isToday;
        boolean hasData;
        
        public CalendarDay(String dayNumber, String date, boolean isToday, boolean hasData) {
            this.dayNumber = dayNumber;
            this.date = date;
            this.isToday = isToday;
            this.hasData = hasData;
        }
    }
}


