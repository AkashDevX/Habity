package com.cleverkube.watermark.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.cleverkube.watermark.R;

public class HourlyPatternView extends View {
    private int[] hourlyData = new int[24]; // 0-23 hours
    private Paint barPaint;
    private Paint textPaint;
    private int maxValue = 500;
    
    public HourlyPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(getContext().getResources().getColor(R.color.primary));
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(getContext().getResources().getColor(R.color.text_white_secondary));
        textPaint.setTextSize(20);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    public void setData(int[] hourlyData) {
        this.hourlyData = hourlyData;
        maxValue = 0;
        for (int value : hourlyData) {
            if (value > maxValue) maxValue = value;
        }
        if (maxValue == 0) maxValue = 500;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int barWidth = width / 24;
        int chartHeight = height - 40;
        
        for (int i = 0; i < 24; i++) {
            int x = i * barWidth;
            int barHeight = maxValue > 0 ? (int) ((hourlyData[i] * chartHeight) / maxValue) : 0;
            int y = height - 40 - barHeight;
            
            canvas.drawRect(x + 2, y, x + barWidth - 2, height - 40, barPaint);
            
            if (i % 3 == 0) { // Show every 3 hours
                canvas.drawText(String.valueOf(i), x + barWidth / 2, height - 10, textPaint);
            }
        }
    }
}


