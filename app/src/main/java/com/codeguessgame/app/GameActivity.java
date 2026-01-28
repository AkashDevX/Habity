package com.codeguessgame.app;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameActivity extends AppCompatActivity {
    private GameEngine gameEngine;
    private EditText guessInput;
    private Button submitButton;
    private Button newGameButton;
    private LinearLayout guessesContainer;
    private TextView attemptsRemainingText;
    private TextView gameStatusText;
    private CardView gameOverLayout;
    private View gameOverContent;
    private TextView secretCodeText;
    private TextView categoryTitle;
    private int codeLength;
    private int maxAttempts;
    private boolean isDailyChallenge = false;
    private SharedPreferences prefs;

    private static final String PREFS_NAME = "CodeGuessGame";
    private static final String KEY_LAST_DAILY_DATE = "last_daily_date";
    private static final String KEY_DAILY_COMPLETED = "daily_completed";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Get game parameters from intent
        codeLength = getIntent().getIntExtra("code_length", 4);
        maxAttempts = getIntent().getIntExtra("max_attempts", 10);
        isDailyChallenge = getIntent().getBooleanExtra("is_daily", false);
        String categoryName = getIntent().getStringExtra("category_name");

        initializeViews();
        
        if (categoryName != null) {
            categoryTitle.setText(categoryName);
        }
        
        // Update hint based on code length
        String hint = "0".repeat(codeLength);
        guessInput.setHint(hint);
        guessInput.setMaxEms(codeLength);

        startNewGame();

        guessInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String input = s.toString();
                if (input.length() > codeLength) {
                    guessInput.setText(input.substring(0, codeLength));
                    guessInput.setSelection(codeLength);
                }
                updateSubmitButton();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        submitButton.setOnClickListener(v -> submitGuess());
        newGameButton.setOnClickListener(v -> startNewGame());
        
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
        builder.setTitle(getString(R.string.exit_game_title));
        builder.setMessage(getString(R.string.exit_game_message));
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

    private void initializeViews() {
        guessInput = findViewById(R.id.guessInput);
        submitButton = findViewById(R.id.submitButton);
        newGameButton = findViewById(R.id.newGameButton);
        guessesContainer = findViewById(R.id.guessesContainer);
        attemptsRemainingText = findViewById(R.id.attemptsRemainingText);
        gameStatusText = findViewById(R.id.gameStatusText);
        gameOverLayout = findViewById(R.id.gameOverLayout);
        gameOverContent = findViewById(R.id.gameOverContent);
        secretCodeText = findViewById(R.id.secretCodeText);
        categoryTitle = findViewById(R.id.categoryTitle);
    }

    private void startNewGame() {
        gameEngine = new GameEngine(codeLength, maxAttempts, isDailyChallenge);
        guessInput.setText("");
        guessInput.setEnabled(true);
        submitButton.setEnabled(false);
        gameOverLayout.setVisibility(View.GONE);
        gameStatusText.setText("");
        clearGuesses();
        updateAttemptsRemaining();
        updateSubmitButton();
        
        if (isDailyChallenge) {
            checkDailyChallengeStatus();
        }
    }

    private void checkDailyChallengeStatus() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        String lastDate = prefs.getString(KEY_LAST_DAILY_DATE, "");
        
        if (!today.equals(lastDate)) {
            prefs.edit().putString(KEY_LAST_DAILY_DATE, today).putBoolean(KEY_DAILY_COMPLETED, false).apply();
        }
    }

    private void submitGuess() {
        String guess = guessInput.getText().toString().trim();
        
        if (guess.length() != codeLength) {
            Toast.makeText(this, getString(R.string.invalid_guess_length_custom, codeLength), Toast.LENGTH_SHORT).show();
            return;
        }

        GameEngine.GuessResult result = gameEngine.submitGuess(guess);
        
        if (result == null) {
            Toast.makeText(this, getString(R.string.invalid_guess_custom, codeLength), Toast.LENGTH_SHORT).show();
            return;
        }

        addGuessView(result);
        guessInput.setText("");
        updateAttemptsRemaining();

        if (gameEngine.isGameWon()) {
            handleGameWin();
        } else if (gameEngine.isGameOver()) {
            handleGameLoss();
        }
    }

    private void addGuessView(GameEngine.GuessResult result) {
        View guessView = LayoutInflater.from(this).inflate(R.layout.item_guess, guessesContainer, false);
        
        TextView guessText = guessView.findViewById(R.id.guessText);
        TextView feedbackText = guessView.findViewById(R.id.feedbackText);
        LinearLayout badgesLayout = guessView.findViewById(R.id.badgesLayout);
        
        guessText.setText(result.getGuess());
        
        badgesLayout.removeAllViews();
        
        StringBuilder feedback = new StringBuilder();
        boolean hasFeedback = false;
        
        if (result.getRightPlace() > 0) {
            feedback.append(result.getRightPlace()).append(" ").append(getString(R.string.right_place));
            hasFeedback = true;
            
            for (int i = 0; i < result.getRightPlace(); i++) {
                View badge = createBadge(ContextCompat.getColor(this, R.color.right_place));
                badgesLayout.addView(badge);
            }
        }
        
        if (result.getRightNumber() > 0) {
            if (feedback.length() > 0) {
                feedback.append(", ");
            }
            feedback.append(result.getRightNumber()).append(" ").append(getString(R.string.right_number));
            hasFeedback = true;
            
            for (int i = 0; i < result.getRightNumber(); i++) {
                View badge = createBadge(ContextCompat.getColor(this, R.color.right_number));
                badgesLayout.addView(badge);
            }
        }
        
        if (!hasFeedback) {
            feedback.append(getString(R.string.no_match));
        }
        
        feedbackText.setText(feedback.toString());
        
        guessesContainer.addView(guessView);
    }
    
    private View createBadge(int color) {
        View badge = new View(this);
        int size = (int) (24 * getResources().getDisplayMetrics().density);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        int margin = (int) (4 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, 0, margin, 0);
        badge.setLayoutParams(params);
        
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        badge.setBackground(drawable);
        return badge;
    }

    private void clearGuesses() {
        guessesContainer.removeAllViews();
    }

    private void handleGameWin() {
        gameStatusText.setText(getString(R.string.game_won));
        gameStatusText.setTextColor(getResources().getColor(R.color.text_white, null));
        gameOverContent.setBackgroundResource(R.drawable.game_over_success);
        gameOverLayout.setVisibility(View.VISIBLE);
        secretCodeText.setText(getString(R.string.secret_code_was, gameEngine.getSecretCode()));
        guessInput.setEnabled(false);
        submitButton.setEnabled(false);

        if (isDailyChallenge) {
            prefs.edit().putBoolean(KEY_DAILY_COMPLETED, true).apply();
        }
    }

    private void handleGameLoss() {
        gameStatusText.setText(getString(R.string.game_lost));
        gameStatusText.setTextColor(getResources().getColor(R.color.text_white, null));
        gameOverContent.setBackgroundResource(R.drawable.game_over_failure);
        gameOverLayout.setVisibility(View.VISIBLE);
        secretCodeText.setText(getString(R.string.secret_code_was, gameEngine.getSecretCode()));
        guessInput.setEnabled(false);
        submitButton.setEnabled(false);
    }

    private void updateAttemptsRemaining() {
        int remaining = gameEngine.getRemainingAttempts();
        attemptsRemainingText.setText(getString(R.string.attempts_remaining, remaining));
    }

    private void updateSubmitButton() {
        String input = guessInput.getText().toString().trim();
        submitButton.setEnabled(input.length() == codeLength && !gameEngine.isGameOver());
    }
}
