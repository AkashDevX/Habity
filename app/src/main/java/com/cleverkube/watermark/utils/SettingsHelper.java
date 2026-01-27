package com.cleverkube.watermark.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsHelper {
    private static final String PREFS_NAME = "watermark_prefs";
    private static final String KEY_DAILY_GOAL = "daily_goal_ml";
    private static final String KEY_UNIT = "unit"; // "ml" or "oz"
    private static final int DEFAULT_GOAL_ML = 2000;
    
    private SharedPreferences prefs;
    
    public SettingsHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public int getDailyGoalMl() {
        return prefs.getInt(KEY_DAILY_GOAL, DEFAULT_GOAL_ML);
    }
    
    public void setDailyGoalMl(int goalMl) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goalMl).apply();
    }
    
    public String getUnit() {
        return prefs.getString(KEY_UNIT, "ml");
    }
    
    public void setUnit(String unit) {
        prefs.edit().putString(KEY_UNIT, unit).apply();
    }
    
    public static int convertMlToOz(int ml) {
        return (int) Math.round(ml / 29.5735);
    }
    
    public static int convertOzToMl(int oz) {
        return (int) Math.round(oz * 29.5735);
    }
    
    public String formatAmount(Context context, int ml) {
        String unit = getUnit();
        if ("oz".equals(unit)) {
            int oz = convertMlToOz(ml);
            return oz + " oz";
        } else {
            return ml + " ml";
        }
    }
}


