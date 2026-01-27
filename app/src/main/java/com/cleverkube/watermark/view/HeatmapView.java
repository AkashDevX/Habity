package com.cleverkube.watermark.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.cleverkube.watermark.R;

import java.util.Map;

public class HeatmapView extends View {
    private Map<String, Integer> dailyData; // Date -> amount
    private Paint[] paints;
    private int cellSize;
    private int padding;
    private int maxValue = 2000;
    
    public HeatmapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        paints = new Paint[5];
        int[] colors = {
            getContext().getResources().getColor(R.color.card_purple_light),
            getContext().getResources().getColor(R.color.primary_dark),
            getContext().getResources().getColor(R.color.primary),
            getContext().getResources().getColor(R.color.primary_light),
            getContext().getResources().getColor(R.color.accent)
        };
        
        for (int i = 0; i < paints.length; i++) {
            paints[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
            paints[i].setColor(colors[i]);
            paints[i].setStyle(Paint.Style.FILL);
        }
        
        cellSize = 40;
        padding = 4;
    }
    
    public void setData(Map<String, Integer> data, int max) {
        this.dailyData = data;
        this.maxValue = max > 0 ? max : 2000;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (dailyData == null || dailyData.isEmpty()) {
            return;
        }
        
        int cols = 7; // Days of week
        int rows = 4; // ~4 weeks visible
        
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * (cellSize + padding) + padding;
                int y = row * (cellSize + padding) + padding;
                
                // Get color based on data (simplified - you'd map dates properly)
                Paint paint = paints[0];
                if (row < rows && col < cols) {
                    int intensity = (row * cols + col) % 5;
                    paint = paints[intensity];
                }
                
                canvas.drawRect(x, y, x + cellSize, y + cellSize, paint);
            }
        }
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int cols = 7;
        int rows = 4;
        int width = cols * (cellSize + padding) + padding;
        int height = rows * (cellSize + padding) + padding;
        setMeasuredDimension(width, height);
    }
}


