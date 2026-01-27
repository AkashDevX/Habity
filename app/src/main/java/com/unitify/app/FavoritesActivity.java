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
import com.unitify.app.model.FavoriteConversion;
import com.unitify.app.repository.FavoritesRepository;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {
    private RecyclerView favoritesRecyclerView;
    private FavoritesAdapter adapter;
    private FavoritesRepository favoritesRepository;
    private TextView emptyStateTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);
        
        favoritesRepository = new FavoritesRepository(this);
        
        setupToolbar();
        setupRecyclerView();
        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void setupToolbar() {
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        favoritesRecyclerView = findViewById(R.id.favoritesRecyclerView);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        
        adapter = new FavoritesAdapter(new ArrayList<>());
        favoritesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        favoritesRecyclerView.setAdapter(adapter);
    }

    private void loadFavorites() {
        try {
            List<FavoriteConversion> favorites = favoritesRepository.getFavorites();
            if (favorites == null) {
                favorites = new ArrayList<>();
            }
            adapter.updateFavorites(favorites);
            
            if (favorites.isEmpty()) {
                if (emptyStateTextView != null) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                }
                if (favoritesRecyclerView != null) {
                    favoritesRecyclerView.setVisibility(View.GONE);
                }
            } else {
                if (emptyStateTextView != null) {
                    emptyStateTextView.setVisibility(View.GONE);
                }
                if (favoritesRecyclerView != null) {
                    favoritesRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (emptyStateTextView != null) {
                emptyStateTextView.setVisibility(View.VISIBLE);
            }
            if (favoritesRecyclerView != null) {
                favoritesRecyclerView.setVisibility(View.GONE);
            }
        }
    }

    private class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.ViewHolder> {
        private List<FavoriteConversion> favorites;

        FavoritesAdapter(List<FavoriteConversion> favorites) {
            this.favorites = new ArrayList<>(favorites);
        }

        void updateFavorites(List<FavoriteConversion> newFavorites) {
            this.favorites = new ArrayList<>(newFavorites);
            notifyDataSetChanged();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_favorite, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            FavoriteConversion favorite = favorites.get(position);
            holder.bind(favorite);
        }

        @Override
        public int getItemCount() {
            return favorites.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView categoryTextView;
            private final TextView conversionTextView;
            private final TextView removeButton;

            ViewHolder(View itemView) {
                super(itemView);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                conversionTextView = itemView.findViewById(R.id.conversionTextView);
                removeButton = itemView.findViewById(R.id.removeButton);
            }

            void bind(FavoriteConversion favorite) {
                categoryTextView.setText(favorite.getCategory().getDisplayName());
                conversionTextView.setText(favorite.getFromUnit() + " → " + favorite.getToUnit());
                
                itemView.setOnClickListener(v -> {
                    Intent intent = new Intent(FavoritesActivity.this, ConverterActivity.class);
                    intent.putExtra("category", favorite.getCategory().name());
                    intent.putExtra("fromUnit", favorite.getFromUnit());
                    intent.putExtra("toUnit", favorite.getToUnit());
                    startActivity(intent);
                });
                
                removeButton.setOnClickListener(v -> {
                    favoritesRepository.removeFavorite(favorite);
                    loadFavorites();
                });
            }
        }
    }
}

