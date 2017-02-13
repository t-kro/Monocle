package com.example.thorsten.myfirstapp;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Thorsten on 01.02.2017.
 */
public class PickerView extends View {
    private int SCALE_FACTOR = (int) this.getResources().getDisplayMetrics().density;
    private int TEXT_SIZE = SCALE_FACTOR * 30;

    private Paint drawPaint;
    private final int paintColor = Color.BLACK;
    private mPoint touchStartPoint;
    List<TextField> values = new ArrayList<>();
    private String Focus = null;
    private String lastFocus;
    private ValueAnimator animator = ValueAnimator.ofFloat(0,0);

    int viewHeight;
    int viewWidth;
    mPoint viewCenter;
    int padding = SCALE_FACTOR * 50;

    // Listener to get user choice
    OnNewFocusListener mListener;

    public PickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setupPaint();
        viewWidth = this.getMeasuredWidth();
        viewHeight = this.getMeasuredHeight();
        viewCenter = new mPoint((viewWidth / 2),(viewHeight / 2));

        // setup value animator for adjusting text fields with vector

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                float vect = value - getFocusedField().getPosition();

                for (TextField field : values) {
                    field.move(vect);
                }
                invalidate();
            }
        });

    }

    class mPoint {
        float x;
        float y;

        public mPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    class TextField {
        private String text;
        private float position;
        private float width;

        public TextField(String text, float position) {
            this.text = text;
            this.position = position;
            width = drawPaint.measureText(text);
        }

        public void move(float v) {
            position = position + v;
        }

        public String getText() {
            return text;
        }

        public float getPosition() {
            return position;
        }

        public mPoint getLeftEdge() {
            return new mPoint(((position + viewCenter.x) - (width / 2)), (viewCenter.y + (drawPaint.getTextSize() / 2)));
        }

        public float getWidth() {
            return width;
        }
    }

    public interface OnNewFocusListener {
        void onEvent();
    }

    public void setOnNewFocusListener(OnNewFocusListener focusListener) {
        mListener = focusListener;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mPoint newP = new mPoint(event.getX(),event.getY());
        mPoint mVect = new mPoint(0,0);

// Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartPoint = newP;

                // stop animator in preparation for next animation

                animator.end();

                break;
            case MotionEvent.ACTION_MOVE:
                mVect.x = newP.x - touchStartPoint.x;
                mVect.y = newP.y - touchStartPoint.y;

                //limit the range within which the fields can be moved ("0" here behind the "?", because we used viewCenter.x before)
                if(values.size() > 1) {
                    mVect.x = ((values.get(0).position + mVect.x) > 0) ? (0 - values.get(0).position) : mVect.x;
                    mVect.x = ((values.get(values.size() - 1).position + mVect.x) < 0) ? (- (values.get(values.size() - 1).position - 0)) : mVect.x;
                }

                // move fields and update Focus if necessary
                for (TextField field : values) {
                    field.move(mVect.x);

                    // set new Focus to field closest to center of view
                    if(Math.abs(0 - field.getPosition()) < ((field.width + padding) / 2)) {
                        Focus = field.getText();
                    }
                }

                touchStartPoint = newP;

                break;
            case MotionEvent.ACTION_UP:

                //check if the focus is new and call listener if necessary
                if(!Focus.equals(lastFocus)) {
                    lastFocus = Focus;

                    if(mListener != null) {
                        mListener.onEvent();
                    }
                }

                // setup and start animation to adjust text fields to center
                animator.setFloatValues(getFocusedField().getPosition(),0f);
                animator.setDuration(500);
                animator.start();

                break;
            default:
                return false;
        }
// Force a view to draw again
        postInvalidate();
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(TextField field : values) {
            if(field.getText().equals(Focus)) {
                drawPaint.setColor(Color.WHITE);
            }
            else {
                drawPaint.setColor(Color.GRAY);
            }

            canvas.drawText(field.getText(), field.getLeftEdge().x, field.getLeftEdge().y, drawPaint);
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int old_w, int old_h){
        super.onSizeChanged(w, h, old_w, old_h);
        viewWidth = w;
        viewHeight = h;
        viewCenter.x = viewWidth / 2;
        viewCenter.y = viewHeight / 2;
    }

    private void setupPaint() {
// Setup paint with color and stroke styles
        drawPaint = new Paint();
        drawPaint.setColor(paintColor);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(2);
        drawPaint.setStyle(Paint.Style.FILL);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setTextSize(TEXT_SIZE);
    }

    // public functions below this:

    public void addTextField(String text) {
        int listLength = values.size();
        int vector;
        TextField lastField;
        TextField newField;

        if(listLength > 0) {
            lastField = values.get(listLength - 1);
            newField = new TextField(text, lastField.position);
            vector = (int) ( (lastField.getWidth() + newField.getWidth()) / 2 ) + padding;
            newField.move(vector);
        }
        else {
            newField = new TextField(text, 0);
            Focus = newField.getText();
        }

        values.add(newField);
        postInvalidate();
    }

    public TextField getFocusedField() {
        for (TextField field : values) {
            if(field.getText().equals(Focus)) {
                return field;
            }
        }
        return null;
    }

}