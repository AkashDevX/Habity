package com.codeguessgame.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

public class CategoryAdapter extends BaseAdapter {
    private Context context;
    private GameCategory[] categories;

    public CategoryAdapter(Context context, GameCategory[] categories) {
        this.context = context;
        this.categories = categories;
    }

    @Override
    public int getCount() {
        return categories.length;
    }

    @Override
    public Object getItem(int position) {
        return categories[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        }

        GameCategory category = categories[position];
        CardView cardView = (CardView) convertView;
        View categoryCard = convertView.findViewById(R.id.categoryCard);
        TextView nameText = convertView.findViewById(R.id.categoryName);
        TextView descText = convertView.findViewById(R.id.categoryDescription);

        nameText.setText(category.getName());
        descText.setText(category.getDescription());

        // Set different background drawable based on category
        categoryCard.setBackgroundResource(category.getBackgroundRes());

        return convertView;
    }

    private int getCategoryColor(int difficulty) {
        switch (difficulty) {
            case GameCategory.DIFFICULTY_EASY:
                return context.getResources().getColor(R.color.category_easy, null);
            case GameCategory.DIFFICULTY_MEDIUM:
                return context.getResources().getColor(R.color.category_medium, null);
            case GameCategory.DIFFICULTY_HARD:
                return context.getResources().getColor(R.color.category_hard, null);
            case GameCategory.DIFFICULTY_DAILY:
                return context.getResources().getColor(R.color.category_daily, null);
            case GameCategory.DIFFICULTY_TIME:
                return context.getResources().getColor(R.color.category_time, null);
            case GameCategory.DIFFICULTY_EXPERT:
                return context.getResources().getColor(R.color.category_expert, null);
            default:
                return context.getResources().getColor(R.color.primary, null);
        }
    }
}
