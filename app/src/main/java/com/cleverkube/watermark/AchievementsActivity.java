package com.cleverkube.watermark;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cleverkube.watermark.database.Achievement;
import com.cleverkube.watermark.database.AppDatabase;
import com.cleverkube.watermark.database.AchievementDao;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {
    private AchievementDao achievementDao;
    private AchievementsAdapter adapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        
        overridePendingTransition(R.anim.slide_in_right, android.R.anim.fade_out);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Achievements");
        }
        
        AppDatabase db = AppDatabase.getDatabase(this);
        achievementDao = db.achievementDao();
        
        setupRecyclerView();
        loadAchievements();
    }
    
    private void setupRecyclerView() {
        adapter = new AchievementsAdapter();
        RecyclerView recyclerView = findViewById(R.id.recyclerViewAchievements);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadAchievements() {
        new Thread(() -> {
            try {
                List<Achievement> achievements = achievementDao.getAllAchievementsSync();
                int unlocked = achievementDao.getUnlockedCountSync();
                
                final List<Achievement> finalAchievements = achievements;
                final int finalUnlocked = unlocked;
                
                runOnUiThread(() -> {
                    try {
                        adapter.setAchievements(finalAchievements);
                        
                        // Update unlocked count
                        TextView textViewCount = findViewById(R.id.textViewUnlockedCount);
                        if (textViewCount != null) {
                            textViewCount.setText(finalUnlocked + " / " + finalAchievements.size() + " Unlocked");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, R.anim.slide_in_right);
        return true;
    }
    
    private static class AchievementsAdapter extends RecyclerView.Adapter<AchievementsAdapter.ViewHolder> {
        private List<Achievement> achievements;
        
        void setAchievements(List<Achievement> achievements) {
            this.achievements = achievements;
            notifyDataSetChanged();
        }
        
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_achievement, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Achievement achievement = achievements.get(position);
            holder.bind(achievement);
        }
        
        @Override
        public int getItemCount() {
            return achievements != null ? achievements.size() : 0;
        }
        
        class ViewHolder extends RecyclerView.ViewHolder {
            TextView textViewIcon;
            TextView textViewTitle;
            TextView textViewDescription;
            View cardView;
            
            ViewHolder(View itemView) {
                super(itemView);
                textViewIcon = itemView.findViewById(R.id.textViewIcon);
                textViewTitle = itemView.findViewById(R.id.textViewTitle);
                textViewDescription = itemView.findViewById(R.id.textViewDescription);
                cardView = itemView.findViewById(R.id.cardView);
            }
            
            void bind(Achievement achievement) {
                textViewIcon.setText(achievement.icon);
                textViewTitle.setText(achievement.title);
                textViewDescription.setText(achievement.description);
                
                if (achievement.unlocked) {
                    cardView.setAlpha(1.0f);
                    textViewIcon.setAlpha(1.0f);
                } else {
                    cardView.setAlpha(0.5f);
                    textViewIcon.setAlpha(0.3f);
                }
            }
        }
    }
}

