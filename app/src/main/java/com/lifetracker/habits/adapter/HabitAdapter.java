package com.lifetracker.habits.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.trackapp.habity.R;
import com.lifetracker.habits.database.AppDatabase;
import com.lifetracker.habits.database.CategoryDao;
import com.lifetracker.habits.database.CategoryEntity;
import com.lifetracker.habits.database.HabitEntity;
import com.lifetracker.habits.reminders.ReminderScheduler;
import com.lifetracker.habits.viewmodel.HabitViewModel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private List<HabitEntity> habits;
    private HabitViewModel viewModel;
    private OnRemindersClickListener onRemindersClickListener;
    private android.content.Context context;
    
    public interface OnRemindersClickListener {
        void onRemindersClick(long habitId);
    }
    
    public HabitAdapter(HabitViewModel viewModel, android.content.Context context) {
        this.viewModel = viewModel;
        this.context = context;
    }
    
    public void setHabits(List<HabitEntity> habits) {
        this.habits = habits;
        notifyDataSetChanged();
    }
    
    public void setOnRemindersClickListener(OnRemindersClickListener listener) {
        this.onRemindersClickListener = listener;
    }
    
    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        HabitEntity habit = habits.get(position);
        holder.bind(habit);
    }
    
    @Override
    public int getItemCount() {
        return habits == null ? 0 : habits.size();
    }
    
    class HabitViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTitle;
        private Switch switchEnabled;
        private CheckBox checkBoxDoneToday;
        private View layoutCategoryBadge;
        private TextView textViewCategoryName;
        private TextView textViewCategoryIcon;
        private ExecutorService executor;
        
        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewHabitTitle);
            switchEnabled = itemView.findViewById(R.id.switchHabitEnabled);
            checkBoxDoneToday = itemView.findViewById(R.id.checkBoxDoneToday);
            layoutCategoryBadge = itemView.findViewById(R.id.layoutCategoryBadge);
            textViewCategoryName = itemView.findViewById(R.id.textViewCategoryName);
            textViewCategoryIcon = itemView.findViewById(R.id.textViewCategoryIcon);
            executor = Executors.newSingleThreadExecutor();
            
            itemView.findViewById(R.id.buttonReminders).setOnClickListener(v -> {
                if (onRemindersClickListener != null && habits != null) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        onRemindersClickListener.onRemindersClick(habits.get(pos).id);
                    }
                }
            });
        }
        
        public void bind(HabitEntity habit) {
            textViewTitle.setText(habit.title);
            switchEnabled.setChecked(habit.enabled);
            
            // Load and display category
            loadCategory(habit);
            
            // Remove previous listeners to avoid triggering while setting state
            switchEnabled.setOnCheckedChangeListener(null);
            checkBoxDoneToday.setOnCheckedChangeListener(null);
            
            // Check if done today
            viewModel.checkCompletionForToday(habit.id, done -> {
                checkBoxDoneToday.setChecked(done);
            });
            
            // Set up enabled switch listener
            switchEnabled.setOnCheckedChangeListener((buttonView, isChecked) -> {
                habit.enabled = isChecked;
                viewModel.updateHabit(habit);
                
                if (isChecked) {
                    // Reschedule all reminders for this habit
                    ReminderScheduler.scheduleAllEnabled(context);
                } else {
                    // Cancel all reminders for this habit
                    ReminderScheduler.cancelAllForHabit(context, habit.id);
                }
            });
            
            // Set up done checkbox listener
            checkBoxDoneToday.setOnCheckedChangeListener((buttonView, isChecked) -> {
                viewModel.toggleCompletion(habit.id, isChecked);
            });
        }
        
        private void loadCategory(HabitEntity habit) {
            if (habit.categoryId == null) {
                layoutCategoryBadge.setVisibility(View.GONE);
                return;
            }
            
            executor.execute(() -> {
                AppDatabase db = AppDatabase.getDatabase(context);
                CategoryDao categoryDao = db.categoryDao();
                CategoryEntity category = categoryDao.getCategoryById(habit.categoryId);
                
                if (category != null) {
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        layoutCategoryBadge.setVisibility(View.VISIBLE);
                        textViewCategoryName.setText(category.name);
                        textViewCategoryIcon.setText(category.icon != null ? category.icon : "⭐");
                        
                        // Set background color if available
                        if (category.color != null && !category.color.isEmpty()) {
                            try {
                                int color = android.graphics.Color.parseColor(category.color);
                                // Create a drawable with the category color
                                android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
                                drawable.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
                                drawable.setCornerRadius(16f);
                                drawable.setColor(color);
                                layoutCategoryBadge.setBackground(drawable);
                            } catch (Exception e) {
                                // Use default if color parsing fails
                            }
                        }
                    });
                } else {
                    android.os.Handler mainHandler = new android.os.Handler(android.os.Looper.getMainLooper());
                    mainHandler.post(() -> {
                        layoutCategoryBadge.setVisibility(View.GONE);
                    });
                }
            });
        }
    }
}





