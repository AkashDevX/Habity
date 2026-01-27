package com.unitify.app.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unitify.app.model.ConversionCategory;
import com.unitify.app.model.FavoriteConversion;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FavoritesRepository {
    private static final String PREFS_NAME = "unitify_favorites";
    private static final String KEY_FAVORITES = "favorites";

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public FavoritesRepository(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<FavoriteConversion> getFavorites() {
        try {
            String json = prefs.getString(KEY_FAVORITES, "[]");
            Type listType = new TypeToken<List<FavoriteConversionJson>>() {}.getType();
            List<FavoriteConversionJson> jsonList = gson.fromJson(json, listType);
            
            List<FavoriteConversion> favorites = new ArrayList<>();
            for (FavoriteConversionJson item : jsonList) {
                favorites.add(new FavoriteConversion(
                    ConversionCategory.valueOf(item.category),
                    item.fromUnit,
                    item.toUnit
                ));
            }
            return favorites;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void addFavorite(FavoriteConversion favorite) {
        List<FavoriteConversion> favorites = getFavorites();
        if (!favorites.contains(favorite)) {
            favorites.add(favorite);
        }
        String json = gson.toJson(convertToJson(favorites));
        prefs.edit().putString(KEY_FAVORITES, json).apply();
    }

    public void removeFavorite(FavoriteConversion favorite) {
        List<FavoriteConversion> favorites = getFavorites();
        favorites.remove(favorite);
        String json = gson.toJson(convertToJson(favorites));
        prefs.edit().putString(KEY_FAVORITES, json).apply();
    }

    public void clearFavorites() {
        prefs.edit().putString(KEY_FAVORITES, "[]").apply();
    }

    private List<FavoriteConversionJson> convertToJson(List<FavoriteConversion> favorites) {
        List<FavoriteConversionJson> jsonList = new ArrayList<>();
        for (FavoriteConversion fav : favorites) {
            jsonList.add(new FavoriteConversionJson(
                fav.getCategory().name(),
                fav.getFromUnit(),
                fav.getToUnit()
            ));
        }
        return jsonList;
    }

    private static class FavoriteConversionJson {
        String category;
        String fromUnit;
        String toUnit;

        FavoriteConversionJson(String category, String fromUnit, String toUnit) {
            this.category = category;
            this.fromUnit = fromUnit;
            this.toUnit = toUnit;
        }
    }
}
