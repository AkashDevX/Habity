package com.unitify.app.repository;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsRepository {
    private static final String PREFS_NAME = "unitify_settings";
    private static final String KEY_PRECISION = "precision";
    private static final String KEY_SCIENTIFIC_NOTATION = "scientific_notation";
    private static final String KEY_DARK_MODE = "dark_mode";

    private final SharedPreferences prefs;

    public SettingsRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getPrecision() {
        return prefs.getInt(KEY_PRECISION, 4);
    }

    public void setPrecision(int precision) {
        prefs.edit().putInt(KEY_PRECISION, precision).apply();
    }

    public boolean getScientificNotation() {
        return prefs.getBoolean(KEY_SCIENTIFIC_NOTATION, false);
    }

    public void setScientificNotation(boolean enabled) {
        prefs.edit().putBoolean(KEY_SCIENTIFIC_NOTATION, enabled).apply();
    }

    public String getDarkMode() {
        return prefs.getString(KEY_DARK_MODE, "system");
    }

    public void setDarkMode(String mode) {
        prefs.edit().putString(KEY_DARK_MODE, mode).apply();
    }
}
