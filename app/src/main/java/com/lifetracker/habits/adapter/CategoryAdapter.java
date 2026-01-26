package com.lifetracker.habits.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.lifetracker.habits.database.CategoryEntity;
import com.trackapp.habity.R;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
    private List<CategoryEntity> categories = new ArrayList<>();
    
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryEntity category = categories.get(position);
        holder.categoryName.setText(category.name);
        holder.categoryIcon.setText(category.icon != null ? category.icon : "⭐");
        
        // Set background color if available
        if (category.color != null && !category.color.isEmpty()) {
            try {
                int color = android.graphics.Color.parseColor(category.color);
                holder.iconLayout.setBackgroundColor(color);
            } catch (Exception e) {
                // Use default if color parsing fails
            }
        }
        
        // TODO: Calculate habit count for this category
        holder.habitCount.setText("0 habits");
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    public void setCategories(List<CategoryEntity> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        TextView categoryIcon;
        TextView habitCount;
        LinearLayout iconLayout;
        
        ViewHolder(View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.textViewCategoryName);
            categoryIcon = itemView.findViewById(R.id.textViewCategoryIcon);
            habitCount = itemView.findViewById(R.id.textViewHabitCount);
            iconLayout = itemView.findViewById(R.id.layoutCategoryIcon);
        }
    }
}

