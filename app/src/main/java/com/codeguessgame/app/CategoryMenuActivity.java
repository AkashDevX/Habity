package com.codeguessgame.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class CategoryMenuActivity extends AppCompatActivity {
    private GridView categoriesGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category_menu);

        categoriesGrid = findViewById(R.id.categoriesGrid);
        CategoryAdapter adapter = new CategoryAdapter(this, getCategories());
        categoriesGrid.setAdapter(adapter);

        categoriesGrid.setOnItemClickListener((parent, view, position, id) -> {
            GameCategory category = (GameCategory) adapter.getItem(position);
            startGame(category);
        });
        
        // Handle back button press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitConfirmationDialog();
            }
        });
    }
    
    private void showExitConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.exit_app_title));
        builder.setMessage(getString(R.string.exit_app_message));
        builder.setPositiveButton(getString(R.string.exit), (dialog, which) -> {
            finish();
        });
        builder.setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setCancelable(true);
        
        AlertDialog dialog = builder.create();
        dialog.show();
        
        // Style the buttons
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.error, null));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.text_primary, null));
    }

    private GameCategory[] getCategories() {
        return new GameCategory[]{
            new GameCategory("Easy", "3-digit code", R.drawable.category_easy_bg, 3, 8, GameCategory.DIFFICULTY_EASY),
            new GameCategory("Medium", "4-digit code", R.drawable.category_medium_bg, 4, 10, GameCategory.DIFFICULTY_MEDIUM),
            new GameCategory("Hard", "5-digit code", R.drawable.category_hard_bg, 5, 12, GameCategory.DIFFICULTY_HARD),
//            new GameCategory("Daily Challenge", "Same code for everyone", R.drawable.category_daily_bg, 4, 10, GameCategory.DIFFICULTY_DAILY),
//            new GameCategory("Time Attack", "Race against time", R.drawable.category_time_bg, 4, 8, GameCategory.DIFFICULTY_TIME),
            new GameCategory("Expert", "6-digit code", R.drawable.category_expert_bg, 6, 15, GameCategory.DIFFICULTY_EXPERT)
        };
    }

    private void startGame(GameCategory category) {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("category_name", category.getName());
        intent.putExtra("code_length", category.getCodeLength());
        intent.putExtra("max_attempts", category.getMaxAttempts());
        intent.putExtra("difficulty", category.getDifficulty());
        intent.putExtra("is_daily", category.getDifficulty() == GameCategory.DIFFICULTY_DAILY);
        startActivity(intent);
    }
}
