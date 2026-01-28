package com.passwordmanager.app.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PinManager {
    private static final String PIN_PREF_NAME = "pin_preferences";
    private static final String PIN_KEY = "pin_hash";
    private static final String PIN_ENABLED_KEY = "pin_enabled";
    
    public static boolean isPinSet(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PIN_PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PIN_ENABLED_KEY, false) && prefs.contains(PIN_KEY);
    }
    
    public static void setPin(String pin, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PIN_PREF_NAME, Context.MODE_PRIVATE);
        String pinHash = String.valueOf(pin.hashCode()); // Simple hash for demo
        prefs.edit()
            .putString(PIN_KEY, pinHash)
            .putBoolean(PIN_ENABLED_KEY, true)
            .apply();
    }
    
    public static boolean verifyPin(String pin, Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PIN_PREF_NAME, Context.MODE_PRIVATE);
        String storedHash = prefs.getString(PIN_KEY, null);
        if (storedHash == null) return false;
        
        String inputHash = String.valueOf(pin.hashCode());
        return storedHash.equals(inputHash);
    }
    
    public static void disablePin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PIN_PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit()
            .remove(PIN_KEY)
            .putBoolean(PIN_ENABLED_KEY, false)
            .apply();
    }
}
