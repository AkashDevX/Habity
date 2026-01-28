package com.inventorymanager.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventorymanager.app.R;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<String> categories;
    private OnCategoryClickListener listener;
    private String selectedCategory = "All";

    public CategoryAdapter(List<String> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category, category.equals(selectedCategory));
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
        notifyDataSetChanged();
    }

    public void setSelectedCategory(String category) {
        String oldSelected = selectedCategory;
        selectedCategory = category;
        notifyItemChanged(categories.indexOf(oldSelected));
        notifyItemChanged(categories.indexOf(category));
    }

    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategory;

        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onCategoryClick(categories.get(position));
                }
            });
        }

        void bind(String category, boolean isSelected) {
            tvCategory.setText(category);
            if (isSelected) {
                tvCategory.setBackgroundResource(R.drawable.category_chip_selected);
                tvCategory.setTextColor(android.graphics.Color.WHITE);
            } else {
                tvCategory.setBackgroundResource(R.drawable.category_chip);
                tvCategory.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }
        }
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(String category);
    }

    public void setOnCategoryClickListener(OnCategoryClickListener listener) {
        this.listener = listener;
    }
}
