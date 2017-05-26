package com.example.thorsten.myfirstapp;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Thorsten on 29.12.2016.
 */
public class StepperView extends View {
    static final int VISIBLE = 1;
    static final int INVISIBLE = 2;

    private final int paintColor = Color.WHITE;
    private Paint drawPaint;
    private int SCALE_FACTOR = (int) this.getResources().getDisplayMetrics().density;

    private int viewWidth;
    private int viewHeight;

    private int numberOfSteps = 3;
    private int stepProgress = 1;
    int circleR = SCALE_FACTOR * 7;
    int lineVisibility = INVISIBLE;

    public StepperView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupPaint();
    }

    private void setupPaint() {
// Setup paint with color and stroke styles
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(2);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int startStepX = circleR;
        int stepY = (viewHeight / 2);
        int stepSize = (viewWidth - (2 * circleR)) / (numberOfSteps - 1);

        int startLineX = startStepX + circleR;
        int lineLength = stepSize - (2 * circleR);

        for(int stepCount = 0; stepCount <= numberOfSteps; stepCount++) {
            if (stepCount < stepProgress) {
                drawPaint.setStyle(Paint.Style.FILL);
            }

            else {
                drawPaint.setStyle(Paint.Style.STROKE);
            }
            canvas.drawCircle(startStepX + (stepCount * stepSize), stepY, circleR, drawPaint);

            if(stepCount < numberOfSteps && lineVisibility == VISIBLE) {
                canvas.drawLine(startLineX + (stepCount * stepSize), stepY, startLineX + (stepCount * stepSize) + lineLength, stepY, drawPaint);
            }
        }
    }

    @Override
    public void onSizeChanged (int w, int h, int old_w, int old_h){
        super.onSizeChanged(w, h, old_w, old_h);
        viewWidth = w;
        viewHeight = h;
    }

    public void setNumberOfSteps (int num) {
        if(num > 1) {
            numberOfSteps = num;
        }
        // force to draw view again
        postInvalidate();
    }

    public void setStepProgress(int num) {
        if(num >= 0) {
            if(num <= numberOfSteps) {
                stepProgress = num;
            }
            else {
                stepProgress = numberOfSteps;
            }
            // force to draw view again
            postInvalidate();
        }
    }

    public void setCircleR(int R) {
        if(R <= (int) (0.5 * viewHeight)){
            circleR = R;
        }
        else {
            circleR = (int) (0.5 * viewHeight);
        }
        // force to draw view again
        postInvalidate();
    }

    public void setLineVisibility(int requestedVisibility) {
        lineVisibility = requestedVisibility;
        // force to draw view again
        postInvalidate();
    }
}
