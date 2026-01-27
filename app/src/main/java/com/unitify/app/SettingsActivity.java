package com.unitify.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unitify.app.repository.FavoritesRepository;
import com.unitify.app.repository.HistoryRepository;
import com.unitify.app.repository.SettingsRepository;

public class SettingsActivity extends AppCompatActivity {
    private SettingsRepository settingsRepository;
    private FavoritesRepository favoritesRepository;
    private HistoryRepository historyRepository;
    
    private RadioGroup precisionRadioGroup;
    private Switch scientificNotationSwitch;
    private Button clearHistoryButton;
    private Button clearFavoritesButton;
    private boolean isInitializing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        try {
            setContentView(R.layout.activity_settings);
            
            settingsRepository = new SettingsRepository(this);
            favoritesRepository = new FavoritesRepository(this);
            historyRepository = new HistoryRepository(this);
            
            setupToolbar();
            setupPrecisionSelector();
            setupScientificNotation();
            setupClearButtons();
            loadSettings();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void setupToolbar() {
        try {
            ImageButton backButton = findViewById(R.id.backButton);
            if (backButton != null) {
                backButton.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupPrecisionSelector() {
        try {
            precisionRadioGroup = findViewById(R.id.precisionRadioGroup);
            if (precisionRadioGroup != null) {
                precisionRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                    try {
                        if (isInitializing) {
                            return;
                        }
                        if (settingsRepository == null) {
                            return;
                        }
                        int precision = 4; // default
                        if (checkedId == R.id.precision2) {
                            precision = 2;
                        } else if (checkedId == R.id.precision4) {
                            precision = 4;
                        } else if (checkedId == R.id.precision6) {
                            precision = 6;
                        }
                        settingsRepository.setPrecision(precision);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupScientificNotation() {
        try {
            scientificNotationSwitch = findViewById(R.id.scientificNotationSwitch);
            if (scientificNotationSwitch != null) {
                scientificNotationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    try {
                        if (isInitializing) {
                            return;
                        }
                        if (settingsRepository == null) {
                            return;
                        }
                        settingsRepository.setScientificNotation(isChecked);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClearButtons() {
        try {
            clearHistoryButton = findViewById(R.id.clearHistoryButton);
            clearFavoritesButton = findViewById(R.id.clearFavoritesButton);
            
            if (clearHistoryButton != null) {
                clearHistoryButton.setOnClickListener(v -> {
                    try {
                        if (historyRepository != null) {
                            historyRepository.clearHistory();
                            Toast.makeText(this, "History cleared", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            Toast.makeText(this, "Error clearing history", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
            
            if (clearFavoritesButton != null) {
                clearFavoritesButton.setOnClickListener(v -> {
                    try {
                        if (favoritesRepository != null) {
                            favoritesRepository.clearFavorites();
                            Toast.makeText(this, "Favorites cleared", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        try {
                            Toast.makeText(this, "Error clearing favorites", Toast.LENGTH_SHORT).show();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadSettings() {
        try {
            isInitializing = true;
            
            if (settingsRepository == null) {
                isInitializing = false;
                return;
            }
            
            // Load precision
            if (precisionRadioGroup != null) {
                try {
                    int precision = settingsRepository.getPrecision();
                    if (precision == 2) {
                        precisionRadioGroup.check(R.id.precision2);
                    } else if (precision == 4) {
                        precisionRadioGroup.check(R.id.precision4);
                    } else if (precision == 6) {
                        precisionRadioGroup.check(R.id.precision6);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // Load scientific notation
            if (scientificNotationSwitch != null) {
                try {
                    boolean scientificNotation = settingsRepository.getScientificNotation();
                    scientificNotationSwitch.setChecked(scientificNotation);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            isInitializing = false;
        } catch (Exception e) {
            e.printStackTrace();
            isInitializing = false;
        }
    }
}
