package com.unitify.app;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unitify.app.engine.ConverterEngine;
import com.unitify.app.model.ConversionCategory;
import com.unitify.app.model.FavoriteConversion;
import com.unitify.app.model.Unit;
import com.unitify.app.repository.FavoritesRepository;
import com.unitify.app.repository.HistoryRepository;
import com.unitify.app.repository.SettingsRepository;
import com.unitify.app.model.ConversionHistory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConverterActivity extends AppCompatActivity {
    private Spinner categorySpinner;
    private Spinner fromUnitSpinner;
    private Spinner toUnitSpinner;
    private EditText inputEditText;
    private TextView outputTextView;
    private Button swapButton;
    private ImageButton copyButton;
    private ImageButton favoriteButton;
    
    private ConversionCategory currentCategory = ConversionCategory.LENGTH;
    private String currentFromUnit;
    private String currentToUnit;
    private boolean isUpdating = false;
    
    private SettingsRepository settingsRepository;
    private FavoritesRepository favoritesRepository;
    private HistoryRepository historyRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_converter);
        
        settingsRepository = new SettingsRepository(this);
        favoritesRepository = new FavoritesRepository(this);
        historyRepository = new HistoryRepository(this);
        
        setupToolbar();
        setupCategorySpinner();
        setupUnitSpinners();
        setupInputOutput();
        setupButtons();
        
        // Load from intent if coming from favorites/history
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("category")) {
            String categoryName = intent.getStringExtra("category");
            String fromUnit = intent.getStringExtra("fromUnit");
            String toUnit = intent.getStringExtra("toUnit");
            double value = intent.getDoubleExtra("value", 0.0);
            
            if (categoryName != null) {
                try {
                    currentCategory = ConversionCategory.valueOf(categoryName);
                    selectCategory(currentCategory);
                    if (fromUnit != null && toUnit != null) {
                        setUnits(fromUnit, toUnit);
                        if (value != 0.0) {
                            inputEditText.setText(String.valueOf(value));
                        }
                    }
                } catch (IllegalArgumentException e) {
                    // Invalid category, use default
                }
            }
        } else {
            // Select first category by default (after everything is set up)
            if (categorySpinner != null && categorySpinner.getCount() > 0) {
                isUpdating = true;
                categorySpinner.setSelection(0);
                currentCategory = ConversionCategory.values()[0];
                updateUnitSpinners();
                isUpdating = false;
            }
        }
        
        updateFavoriteButton();
    }

    private void setupToolbar() {
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }
    }

    private void setupCategorySpinner() {
        categorySpinner = findViewById(R.id.categorySpinner);
        if (categorySpinner == null) return;
        
        // Create list of category display names
        List<String> categoryNames = new ArrayList<>();
        for (ConversionCategory category : ConversionCategory.values()) {
            categoryNames.add(category.getDisplayName());
        }
        
        // Create adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, categoryNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
        
        // Set selection listener
        categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUpdating) {
                    ConversionCategory selectedCategory = ConversionCategory.values()[position];
                    selectCategory(selectedCategory);
                }
            }
            
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void selectCategory(ConversionCategory category) {
        currentCategory = category;
        isUpdating = true;
        
        // Update spinner selection
        if (categorySpinner != null) {
            int index = category.ordinal();
            if (index < categorySpinner.getCount()) {
                categorySpinner.setSelection(index);
            }
        }
        
        // Update unit spinners
        updateUnitSpinners();
        isUpdating = false;
        
        // Recalculate if there's input
        performConversion();
    }

    private void setupUnitSpinners() {
        fromUnitSpinner = findViewById(R.id.fromUnitSpinner);
        toUnitSpinner = findViewById(R.id.toUnitSpinner);
        
        updateUnitSpinners();
        
        fromUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUpdating) {
                    Unit unit = (Unit) parent.getItemAtPosition(position);
                    currentFromUnit = unit.getName();
                    performConversion();
                    updateFavoriteButton();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        
        toUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!isUpdating) {
                    Unit unit = (Unit) parent.getItemAtPosition(position);
                    currentToUnit = unit.getName();
                    performConversion();
                    updateFavoriteButton();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void updateUnitSpinners() {
        if (fromUnitSpinner == null || toUnitSpinner == null) {
            return; // Spinners not initialized yet
        }
        
        List<Unit> units = ConverterEngine.getUnits(currentCategory);
        if (units == null || units.isEmpty()) {
            return;
        }
        
        ArrayAdapter<Unit> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, units);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        fromUnitSpinner.setAdapter(adapter);
        toUnitSpinner.setAdapter(adapter);
        
        currentFromUnit = units.get(0).getName();
        currentToUnit = units.size() > 1 ? units.get(1).getName() : units.get(0).getName();
    }

    private void setUnits(String fromUnit, String toUnit) {
        isUpdating = true;
        List<Unit> units = ConverterEngine.getUnits(currentCategory);
        
        int fromIndex = 0;
        int toIndex = 0;
        for (int i = 0; i < units.size(); i++) {
            if (units.get(i).getName().equals(fromUnit) || units.get(i).getSymbol().equals(fromUnit)) {
                fromIndex = i;
            }
            if (units.get(i).getName().equals(toUnit) || units.get(i).getSymbol().equals(toUnit)) {
                toIndex = i;
            }
        }
        
        fromUnitSpinner.setSelection(fromIndex);
        toUnitSpinner.setSelection(toIndex);
        currentFromUnit = units.get(fromIndex).getName();
        currentToUnit = units.get(toIndex).getName();
        isUpdating = false;
    }

    private void setupInputOutput() {
        inputEditText = findViewById(R.id.inputEditText);
        outputTextView = findViewById(R.id.outputTextView);
        
        inputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                performConversion();
            }
        });
    }

    private void setupButtons() {
        swapButton = findViewById(R.id.swapButton);
        copyButton = findViewById(R.id.copyButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        
        swapButton.setOnClickListener(v -> swapUnits());
        copyButton.setOnClickListener(v -> copyOutput());
        favoriteButton.setOnClickListener(v -> toggleFavorite());
    }

    private void swapUnits() {
        isUpdating = true;
        List<Unit> units = ConverterEngine.getUnits(currentCategory);
        
        int fromIndex = fromUnitSpinner.getSelectedItemPosition();
        int toIndex = toUnitSpinner.getSelectedItemPosition();
        
        fromUnitSpinner.setSelection(toIndex);
        toUnitSpinner.setSelection(fromIndex);
        
        String temp = currentFromUnit;
        currentFromUnit = currentToUnit;
        currentToUnit = temp;
        
        isUpdating = false;
        
        // Swap input and output values
        String inputText = inputEditText.getText().toString();
        String outputText = outputTextView.getText().toString();
        
        if (!inputText.isEmpty() && !outputText.isEmpty()) {
            try {
                double inputValue = Double.parseDouble(inputText);
                double outputValue = Double.parseDouble(outputText.replaceAll("[^0-9.E-]", ""));
                inputEditText.setText(formatNumber(outputValue));
            } catch (NumberFormatException e) {
                // Ignore
            }
        }
        
        performConversion();
    }

    private void copyOutput() {
        String output = outputTextView.getText().toString();
        if (!output.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Converted value", output);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleFavorite() {
        FavoriteConversion favorite = new FavoriteConversion(
            currentCategory, currentFromUnit, currentToUnit);
        
        List<FavoriteConversion> favorites = favoritesRepository.getFavorites();
        boolean isFavorite = favorites.contains(favorite);
        
        if (isFavorite) {
            favoritesRepository.removeFavorite(favorite);
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
        } else {
            favoritesRepository.addFavorite(favorite);
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
        }
        
        updateFavoriteButton();
    }

    private void updateFavoriteButton() {
        if (favoriteButton == null || currentFromUnit == null || currentToUnit == null) {
            return;
        }
        
        FavoriteConversion favorite = new FavoriteConversion(
            currentCategory, currentFromUnit, currentToUnit);
        
        List<FavoriteConversion> favorites = favoritesRepository.getFavorites();
        boolean isFavorite = favorites.contains(favorite);
        
        favoriteButton.setImageResource(isFavorite ? 
            android.R.drawable.star_big_on : android.R.drawable.star_big_off);
    }

    private void performConversion() {
        if (inputEditText == null || outputTextView == null || 
            currentFromUnit == null || currentToUnit == null) {
            return;
        }
        
        String inputText = inputEditText.getText().toString().trim();
        
        if (inputText.isEmpty()) {
            outputTextView.setText("");
            return;
        }
        
        try {
            double inputValue = Double.parseDouble(inputText);
            double result = ConverterEngine.convert(
                currentCategory, currentFromUnit, currentToUnit, inputValue);
            
            String formattedResult = formatNumber(result);
            outputTextView.setText(formattedResult);
            
            // Save to history (only if both values are non-zero)
            if (Math.abs(inputValue) > 0.0001 && Math.abs(result) > 0.0001) {
                ConversionHistory history = new ConversionHistory(
                    currentCategory, currentFromUnit, currentToUnit,
                    inputValue, result, System.currentTimeMillis());
                historyRepository.addHistory(history);
            }
        } catch (NumberFormatException e) {
            outputTextView.setText("");
        } catch (Exception e) {
            // Handle any other exceptions gracefully
            outputTextView.setText("");
        }
    }

    private String formatNumber(double value) {
        int precision = settingsRepository.getPrecision();
        boolean scientificNotation = settingsRepository.getScientificNotation();
        
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        
        if (scientificNotation) {
            DecimalFormat df = new DecimalFormat("0." + "0".repeat(precision) + "E0", symbols);
            return df.format(value);
        } else {
            DecimalFormat df = new DecimalFormat("0." + "0".repeat(precision), symbols);
            String formatted = df.format(value);
            // Remove trailing zeros
            if (formatted.contains(".")) {
                formatted = formatted.replaceAll("0*$", "").replaceAll("\\.$", "");
            }
            return formatted;
        }
    }
}

