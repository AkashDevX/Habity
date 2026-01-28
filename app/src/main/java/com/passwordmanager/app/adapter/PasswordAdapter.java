package com.passwordmanager.app.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.passwordmanager.app.R;
import com.passwordmanager.app.database.PasswordEntity;

import java.util.ArrayList;
import java.util.List;

public class PasswordAdapter extends RecyclerView.Adapter<PasswordAdapter.PasswordViewHolder> {
    private List<PasswordEntity> passwords;
    private Context context;
    private OnPasswordClickListener listener;
    
    public interface OnPasswordClickListener {
        void onPasswordClick(PasswordEntity password);
    }
    
    public PasswordAdapter(Context context, OnPasswordClickListener listener) {
        this.context = context;
        this.listener = listener;
        this.passwords = new ArrayList<>();
    }
    
    public void setPasswords(List<PasswordEntity> passwords) {
        this.passwords = passwords != null ? passwords : new ArrayList<>();
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public PasswordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_password, parent, false);
        return new PasswordViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull PasswordViewHolder holder, int position) {
        PasswordEntity password = passwords.get(position);
        holder.titleTextView.setText(password.title);
        holder.usernameTextView.setText(password.username != null ? password.username : "");
        if (password.website != null && !password.website.isEmpty()) {
            holder.websiteTextView.setText(password.website);
            holder.websiteTextView.setVisibility(View.VISIBLE);
        } else {
            holder.websiteTextView.setVisibility(View.GONE);
        }
        
        // Set initial letter in icon circle
        if (password.title != null && !password.title.isEmpty()) {
            String initial = password.title.substring(0, 1).toUpperCase();
            holder.iconTextView.setText(initial);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPasswordClick(password);
            }
        });
    }
    
    @Override
    public int getItemCount() {
        return passwords.size();
    }
    
    static class PasswordViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        TextView usernameTextView;
        TextView websiteTextView;
        TextView iconTextView;
        
        PasswordViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            websiteTextView = itemView.findViewById(R.id.websiteTextView);
            iconTextView = itemView.findViewById(R.id.iconTextView);
        }
    }
}
