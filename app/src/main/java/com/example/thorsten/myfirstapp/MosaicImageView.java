package com.example.thorsten.myfirstapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

/**
 * Created by thorsten on 28.05.17.
 */

public class MosaicImageView extends View {
    static final int VISIBLE = 1;
    static final int INVISIBLE = 2;

    static final int X = 0;
    static final int Y = 1;

    private final int paintColor = Color.BLACK;
    private Paint drawPaint;
    private int SCALE_FACTOR = (int) this.getResources().getDisplayMetrics().density;

    private float minimumCircleR = SCALE_FACTOR * 3; // below this size take just pixels, graduately fade out outline of circle
    private float maximumCircleR = SCALE_FACTOR * 150;
    private float circleR = minimumCircleR;
    private int padding = (int) (circleR / 4);

    private int viewWidth;
    private int viewHeight;
    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    Matrix matrix = new Matrix(); // matrix for transformations from touch zoom and dragging
    private float touchStartPoint[] = new float[2];
    boolean isScaled = false;

    MosaicImageMap map = null;

    //private Rebus getRebusByColor(colorpalette, dataset, color) {}

    //private PicFromPics_map updateMapRebuses(PicfromPics_map, dataset) {}

    public MosaicImageView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(event);

        float newP[] = new float[2];
        float mVect[] = new float[2];

        newP[X] = event.getX();
        newP[Y] = event.getY();

        mVect[X] = 0;
        mVect[Y] = 0;

// Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartPoint = newP;
                break;
            case MotionEvent.ACTION_MOVE:
                mVect[X] = newP[X] - touchStartPoint[X];
                mVect[Y] = newP[Y] - touchStartPoint[Y];

                matrix.reset();
                matrix.setTranslate(mVect[X],mVect[Y]);
                map.update_locationsByMatrix(matrix);
                touchStartPoint = newP;
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                return false;
        }
        // Force view to draw again
        postInvalidate();
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float focus[] = new float[2];

            if(map != null && map.isInitialized) {
                mScaleFactor = detector.getScaleFactor();
                //mScaleFactor = Math.max(0.5f, Math.min(mScaleFactor, 3.0f));

                if(circleR * mScaleFactor > minimumCircleR && circleR * mScaleFactor < maximumCircleR) {
                    focus[X] = detector.getFocusX();
                    focus[Y] = detector.getFocusY();
                    matrix.reset();
                    matrix.setScale(mScaleFactor, mScaleFactor, focus[X], focus[Y]);

                    //update_matrixData calculates new positions and updates matrices of points
                    map.update_locationsByMatrix(matrix);
                    circleR *= mScaleFactor;

                    isScaled = true;
                    postInvalidate();
                }
            }

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if(isScaled) {
                map.updateTempImages(2 * (int) circleR, 2 * (int) circleR);
                isScaled = false;
                postInvalidate();
            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int stepX = 0;
        int stepY = 0;
        MosaicImageMap.tempImage image;

        if(map != null && map.isInitialized) {
            while(stepY < map.rows) {
                while(stepX < map.columns) {
                // only draw visible items to canvas?
                //if(map.points[stepX][stepY].isInsideView(viewWidth, viewHeight, Math.round(circleR))) {
                    image = map.get_image_by_color(map.points[stepX][stepY].get_color());
                    if(image != null) {
                        canvas.drawBitmap(
                                image.get_tempImage(2 * (int) circleR, 2 * (int) circleR),
                                map.points[stepX][stepY].get_screenLocation()[X] - circleR,
                                map.points[stepX][stepY].get_screenLocation()[Y] - circleR,
                                null);
                    }
           //     }
                    stepX++;
                }
                stepX = 0;
                stepY++;
            }
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int old_w, int old_h) {
        super.onSizeChanged(w, h, old_w, old_h);
        viewWidth = w;
        viewHeight = h;
    }


    public void setMap(MosaicImageMap new_map) {
        int tempLocation[] = new int[] {Math.round(circleR), Math.round(circleR)};
        int stepX = 0;
        int stepY = 0;

        this.map = new_map;

        //initialize map with standart size and padding if not already done

        if(!map.isInitialized){
            while(stepY < map.rows) {
                while(stepX < map.columns) {
                    map.points[stepX][stepY].set_screenLocation(tempLocation[X], tempLocation[Y]);

                    tempLocation[X] += padding + (2*circleR);
                    stepX++;
                }
                tempLocation[X] = Math.round(circleR);
                tempLocation[Y] += padding + (2*circleR);
                stepX = 0;
                stepY++;
            }

            map.updateTempImages(2 * (int )circleR, 2 * (int) circleR);
            map.isInitialized = true;
            postInvalidate();
        }
    }
    public MosaicImageMap getMap() {
        return map;
    }

    public void setMinimumCircleR(int num) {
        if (num > 1 && num < maximumCircleR) {
            minimumCircleR = num;
        }
        // force to draw view again
        postInvalidate();
    }

    public void setMaximumCircleSize(int num) {
        if (num > 1 && num > minimumCircleR) {
            maximumCircleR = num;
        }
        // force to draw view again
        postInvalidate();
    }
}
