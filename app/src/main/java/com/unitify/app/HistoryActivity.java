package com.unitify.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.unitify.app.model.ConversionCategory;
import com.unitify.app.model.ConversionHistory;
import com.unitify.app.repository.HistoryRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView historyRecyclerView;
    private HistoryAdapter adapter;
    private HistoryRepository historyRepository;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        
        historyRepository = new HistoryRepository(this);
        
        setupToolbar();
        setupRecyclerView();
        loadHistory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory();
    }

    private void setupToolbar() {
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        historyRecyclerView = findViewById(R.id.historyRecyclerView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        
        adapter = new HistoryAdapter(new ArrayList<>());
        historyRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyRecyclerView.setAdapter(adapter);
    }

    private void loadHistory() {
        try {
            List<ConversionHistory> history = historyRepository.getHistory();
            if (history == null) {
                history = new ArrayList<>();
            }
            adapter.updateHistory(history);
            
            if (history.isEmpty()) {
                if (emptyStateTextView != null) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                }
                if (historyRecyclerView != null) {
                    historyRecyclerView.setVisibility(View.GONE);
                }
            } else {
                if (emptyStateTextView != null) {
                    emptyStateTextView.setVisibility(View.GONE);
                }
                if (historyRecyclerView != null) {
                    historyRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (emptyStateTextView != null) {
                emptyStateTextView.setVisibility(View.VISIBLE);
            }
            if (historyRecyclerView != null) {
                historyRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {
        private List<ConversionHistory> history;
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());

        HistoryAdapter(List<ConversionHistory> history) {
            this.history = new ArrayList<>(history);
        }

        void updateHistory(List<ConversionHistory> newHistory) {
            this.history = new ArrayList<>(newHistory);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            ConversionHistory historyItem = history.get(position);
            holder.bind(historyItem);
        }

        @Override
        public int getItemCount() {
            return history.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView categoryTextView;
            private final TextView conversionTextView;
            private final TextView timestampTextView;

            ViewHolder(View itemView) {
                super(itemView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                conversionTextView = itemView.findViewById(R.id.conversionTextView);
                timestampTextView = itemView.findViewById(R.id.timestampTextView);
            }

            void bind(ConversionHistory historyItem) {
                categoryTextView.setText(historyItem.getCategory().getDisplayName());
                
                String conversion = String.format(Locale.getDefault(), "%.4f %s = %.4f %s",
                    historyItem.getInputValue(), historyItem.getFromUnit(),
                    historyItem.getOutputValue(), historyItem.getToUnit());
                conversionTextView.setText(conversion);
                
                timestampTextView.setText(dateFormat.format(historyItem.getDate()));
                
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(HistoryActivity.this, ConverterActivity.class);
                    intent.putExtra("category", historyItem.getCategory().name());
                    intent.putExtra("fromUnit", historyItem.getFromUnit());
                    intent.putExtra("toUnit", historyItem.getToUnit());
                    intent.putExtra("value", historyItem.getInputValue());
                    startActivity(intent);
                });
            }
        }
    }
}

