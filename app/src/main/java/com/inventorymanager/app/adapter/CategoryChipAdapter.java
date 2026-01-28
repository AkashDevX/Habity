package com.inventorymanager.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.inventorymanager.app.R;
import java.util.List;

public class CategoryChipAdapter extends RecyclerView.Adapter<CategoryChipAdapter.CategoryChipViewHolder> {
    private List<String> categories;
    private OnCategoryChipClickListener listener;
    private int selectedPosition = -1;

    public CategoryChipAdapter(List<String> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryChipViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_chip, parent, false);
        return new CategoryChipViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryChipViewHolder holder, int position) {
        String category = categories.get(position);
        holder.bind(category, position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return categories != null ? categories.size() : 0;
    }

    public void setSelectedPosition(int position) {
        int oldPosition = selectedPosition;
        selectedPosition = position;
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition);
        }
        if (selectedPosition != -1) {
            notifyItemChanged(selectedPosition);
        }
    }

    public String getSelectedCategory() {
        if (selectedPosition >= 0 && selectedPosition < categories.size()) {
            return categories.get(selectedPosition);
        }
        return null;
    }

    class CategoryChipViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryChip;

        CategoryChipViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryChip = itemView.findViewById(R.id.tvCategoryChip);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position);
                    listener.onCategoryChipClick(categories.get(position), position);
                }
            });
        }

        void bind(String category, boolean isSelected) {
            tvCategoryChip.setText(category);
            if (isSelected) {
                tvCategoryChip.setBackgroundResource(R.drawable.category_chip_selected);
                tvCategoryChip.setTextColor(android.graphics.Color.WHITE);
            } else {
                tvCategoryChip.setBackgroundResource(R.drawable.category_chip);
                tvCategoryChip.setTextColor(itemView.getContext().getResources().getColor(R.color.text_primary));
            }
        }
    }

    public interface OnCategoryChipClickListener {
        void onCategoryChipClick(String category, int position);
    }

    public void setOnCategoryChipClickListener(OnCategoryChipClickListener listener) {
        this.listener = listener;
    }
}
