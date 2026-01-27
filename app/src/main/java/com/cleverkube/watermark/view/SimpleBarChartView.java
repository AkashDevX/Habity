package com.cleverkube.watermark.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

public class SimpleBarChartView extends View {
    private int[] data = new int[7];
    private Paint barPaint;
    private Paint textPaint;
    private int maxValue = 2000;
    
    public SimpleBarChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        barPaint.setColor(0xFF00A8FF);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(0xFFFFFFFF);
        textPaint.setTextSize(24);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }
    
    public void setData(int[] data) {
        this.data = data;
        maxValue = 0;
        for (int value : data) {
            if (value > maxValue) maxValue = value;
        }
        if (maxValue == 0) maxValue = 2000;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int barWidth = width / 8;
        int chartHeight = height - 80;
        
        String[] labels = {"6d", "5d", "4d", "3d", "2d", "1d", "Today"};
        
        for (int i = 0; i < 7; i++) {
            int x = (i + 1) * barWidth;
            int barHeight = maxValue > 0 ? (int) ((data[i] * chartHeight) / maxValue) : 0;
            int y = height - 60 - barHeight;
            
            canvas.drawRect(x - barWidth / 2 + 10, y, x + barWidth / 2 - 10, height - 60, barPaint);
            canvas.drawText(labels[i], x, height - 20, textPaint);
        }
    }
}


