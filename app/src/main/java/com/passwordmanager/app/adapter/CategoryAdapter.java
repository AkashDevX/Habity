package com.passwordmanager.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.passwordmanager.app.R;
import com.passwordmanager.app.database.CategoryEntity;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryEntity> categories;
    private Context context;
    
    public CategoryAdapter(Context context) {
        this.context = context;
        this.categories = new ArrayList<>();
    }
    
    public void setCategories(List<CategoryEntity> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryEntity category = categories.get(position);
        holder.nameTextView.setText(category.name);
        if (category.color != null && !category.color.isEmpty()) {
            try {
                int color = android.graphics.Color.parseColor(category.color);
                holder.colorView.setBackgroundColor(color);
            } catch (Exception e) {
                holder.colorView.setBackgroundColor(0xFF6200EE);
            }
        }
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        View colorView;
        
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            colorView = itemView.findViewById(R.id.colorView);
        }
    }
}
