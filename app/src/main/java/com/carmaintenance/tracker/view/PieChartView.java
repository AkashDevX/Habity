package com.carmaintenance.tracker.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class PieChartView extends View {
    private float completedPercentage = 0f;
    private float notCompletedPercentage = 0f;
    private Paint completedPaint;
    private Paint notCompletedPaint;
    private RectF rectF;
    
    public PieChartView(Context context) {
        super(context);
        init();
    }
    
    public PieChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public PieChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    
    private void init() {
        completedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        completedPaint.setColor(Color.parseColor("#4CAF50")); // Green
        
        notCompletedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        notCompletedPaint.setColor(Color.parseColor("#F44336")); // Red
        
        rectF = new RectF();
    }
    
    public void setData(float completed, float notCompleted) {
        this.completedPercentage = completed;
        this.notCompletedPercentage = notCompleted;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        if (completedPercentage == 0 && notCompletedPercentage == 0) {
            return;
        }
        
        float width = getWidth();
        float height = getHeight();
        float centerX = width / 2;
        float centerY = height / 2;
        float radius = Math.min(width, height) / 2 - 20;
        
        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        
        float startAngle = -90; // Start from top
        
        // Draw completed portion (green)
        if (completedPercentage > 0) {
            float sweepAngle = (completedPercentage / 100f) * 360f;
            canvas.drawArc(rectF, startAngle, sweepAngle, true, completedPaint);
            startAngle += sweepAngle;
        }
        
        // Draw not completed portion (red)
        if (notCompletedPercentage > 0) {
            float sweepAngle = (notCompletedPercentage / 100f) * 360f;
            canvas.drawArc(rectF, startAngle, sweepAngle, true, notCompletedPaint);
        }
    }
}

