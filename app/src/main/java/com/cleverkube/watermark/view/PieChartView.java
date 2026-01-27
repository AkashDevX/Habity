package com.cleverkube.watermark.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.cleverkube.watermark.R;

public class PieChartView extends View {
    private Paint completedPaint;
    private Paint remainingPaint;
    private float completedPercentage = 0f;
    private float remainingPercentage = 0f;
    private int goalMl = 2000;
    private int consumedMl = 0;
    
    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        completedPaint.setColor(getContext().getResources().getColor(R.color.primary));
        completedPaint.setStyle(Paint.Style.FILL);
        
        remainingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        remainingPaint.setColor(getContext().getResources().getColor(R.color.card_purple_light));
        remainingPaint.setStyle(Paint.Style.FILL);
    }
    
    public void setData(int consumed, int goal) {
        this.consumedMl = consumed;
        this.goalMl = goal;
        if (goal > 0) {
            this.completedPercentage = (consumed * 360f) / goal;
            this.remainingPercentage = 360f - completedPercentage;
        } else {
            this.completedPercentage = 0f;
            this.remainingPercentage = 360f;
        }
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int padding = 20;
        int radius = (size - padding * 2) / 2;
        int centerX = width / 2;
        int centerY = height / 2;
        
        RectF rectF = new RectF(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        );
        
        float startAngle = -90f; // Start from top
        
        // Draw completed portion
        if (completedPercentage > 0) {
            canvas.drawArc(rectF, startAngle, completedPercentage, true, completedPaint);
        }
        
        // Draw remaining portion
        if (remainingPercentage > 0) {
            canvas.drawArc(rectF, startAngle + completedPercentage, remainingPercentage, true, remainingPaint);
        }
    }
}


