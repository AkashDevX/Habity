package com.inventorymanager.app.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Locale;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.inventorymanager.app.R;
import com.inventorymanager.app.database.ItemEntity;
import java.io.File;

public class ItemAdapter extends ListAdapter<ItemEntity, ItemAdapter.ItemViewHolder> {
    private OnItemClickListener listener;

    public ItemAdapter() {
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
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemEntity item = getItem(position);
        holder.bind(item);
    }

    public ItemEntity getItemAt(int position) {
        return getItem(position);
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivPhoto;
        private TextView tvName;
        private TextView tvCategory;
        private TextView tvQuantity;
        private TextView tvPrice;

        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
            tvName = itemView.findViewById(R.id.tvName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        void bind(ItemEntity item) {
            tvName.setText(item.getName());
            tvCategory.setText(item.getCategory());
            tvQuantity.setText("Qty: " + item.getQuantity());
            
            // Show price if available
            if (item.getPrice() != null && item.getPrice() > 0) {
                tvPrice.setText(String.format(Locale.getDefault(), "$%.2f", item.getPrice()));
                tvPrice.setVisibility(View.VISIBLE);
            } else {
                tvPrice.setVisibility(View.GONE);
            }

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
