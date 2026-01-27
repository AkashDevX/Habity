package com.unitify.app.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unitify.app.model.ConversionCategory;
import com.unitify.app.model.ConversionHistory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class HistoryRepository {
    private static final String PREFS_NAME = "unitify_history";
    private static final String KEY_HISTORY = "history";
    private static final int MAX_HISTORY = 20;

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public HistoryRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<ConversionHistory> getHistory() {
        try {
            String json = prefs.getString(KEY_HISTORY, "[]");
            Type listType = new TypeToken<List<ConversionHistoryJson>>() {}.getType();
            List<ConversionHistoryJson> jsonList = gson.fromJson(json, listType);
            
            List<ConversionHistory> history = new ArrayList<>();
            for (ConversionHistoryJson item : jsonList) {
                history.add(new ConversionHistory(
                    ConversionCategory.valueOf(item.category),
                    item.fromUnit,
                    item.toUnit,
                    item.inputValue,
                    item.outputValue,
                    item.timestamp
                ));
            }
            // Sort by timestamp descending (newest first)
            history.sort((a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
            return history;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void addHistory(ConversionHistory history) {
        List<ConversionHistory> historyList = getHistory();
        historyList.add(0, history); // Add to beginning
        
        // Keep only last MAX_HISTORY items
        if (historyList.size() > MAX_HISTORY) {
            historyList = historyList.subList(0, MAX_HISTORY);
        }
        
        String json = gson.toJson(convertToJson(historyList));
        prefs.edit().putString(KEY_HISTORY, json).apply();
    }

    public void clearHistory() {
        prefs.edit().putString(KEY_HISTORY, "[]").apply();
    }

    private List<ConversionHistoryJson> convertToJson(List<ConversionHistory> history) {
        List<ConversionHistoryJson> jsonList = new ArrayList<>();
        for (ConversionHistory item : history) {
            jsonList.add(new ConversionHistoryJson(
                item.getCategory().name(),
                item.getFromUnit(),
                item.getToUnit(),
                item.getInputValue(),
                item.getOutputValue(),
                item.getTimestamp()
            ));
        }
        return jsonList;
    }

    private static class ConversionHistoryJson {
        String category;
        String fromUnit;
        String toUnit;
        double inputValue;
        double outputValue;
        long timestamp;

        ConversionHistoryJson(String category, String fromUnit, String toUnit,
                             double inputValue, double outputValue, long timestamp) {
            this.category = category;
            this.fromUnit = fromUnit;
            this.toUnit = toUnit;
            this.inputValue = inputValue;
            this.outputValue = outputValue;
            this.timestamp = timestamp;
        }
    }
}
