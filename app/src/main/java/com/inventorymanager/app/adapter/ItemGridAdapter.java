package com.inventorymanager.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.inventorymanager.app.R;
import com.inventorymanager.app.database.ItemEntity;
import java.io.File;

public class ItemGridAdapter extends ListAdapter<ItemEntity, ItemGridAdapter.ItemGridViewHolder> {
    private OnItemClickListener listener;

    public ItemGridAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<ItemEntity> DIFF_CALLBACK = new DiffUtil.ItemCallback<ItemEntity>() {
        @Override
        public boolean areItemsTheSame(@NonNull ItemEntity oldItem, @NonNull ItemEntity newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull ItemEntity oldItem, @NonNull ItemEntity newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                   oldItem.getCategory().equals(newItem.getCategory()) &&
                   oldItem.getQuantity() == newItem.getQuantity();
        }
    };

    @NonNull
    @Override
    public ItemGridViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory_grid, parent, false);
        return new ItemGridViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemGridViewHolder holder, int position) {
        ItemEntity item = getItem(position);
        holder.bind(item);
    }

    class ItemGridViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private TextView tvName;
        private TextView tvQuantity;

        ItemGridViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvName = itemView.findViewById(R.id.tvName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        void bind(ItemEntity item) {
            tvName.setText(item.getName());
            tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Load image using Glide
            if (item.getPhotoPath() != null && !item.getPhotoPath().isEmpty()) {
                File imageFile = new File(item.getPhotoPath());
                if (imageFile.exists()) {
                    Glide.with(itemView.getContext())
                            .load(imageFile)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .into(ivPhoto);
                } else {
                    ivPhoto.setImageResource(R.drawable.ic_launcher_foreground);
                }
            } else {
                ivPhoto.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(ItemEntity item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
