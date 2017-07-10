package com.example.thorsten.myfirstapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by thorsten on 28.05.17.
 */

public class pinchZoomableGridLayout extends View {
    static final int VISIBLE = 1;
    static final int INVISIBLE = 2;

    static final int X = 0;
    static final int Y = 1;

    private final int paintColor = Color.WHITE;
    private Paint drawPaint;
    private int SCALE_FACTOR = (int) this.getResources().getDisplayMetrics().density;

    private int viewWidth;
    private int viewHeight;

    private int minimumCircleR = SCALE_FACTOR * 5;
    private int maximumCircleR = SCALE_FACTOR * 200;
    private int circleR = minimumCircleR;
    private int padding = (int) (circleR / 3);

    PicFromPics_map map = null;

    Matrix matrix; // matrix for transformations from touch zoom and dragging
    float updated_locations[];
    /*
     * Definition of the required PicFromPics_map, this might go to a separate file
     */

    public class PicFromPics_map {
        public PicPoint[][] points;
        boolean isInitialized = false;
        int rows, columns;

        private class PicPoint {
            private float screenLocation[] = new float[2]; //must be set, updated and maintained by view
            private int color;
            private Rebus rebus;

            private PicPoint(int color, Rebus rebus) {
                this.color = color;
                this.rebus = rebus;
            }

            void set_color(int color) {
                this.color = color;
            }
            int get_color() {
                return color;
            }

            void set_rebus (Rebus rebus) {
                this.rebus = rebus;
            }
            Rebus get_Rebus() {
                return rebus;
            }

            void set_screenLocation (int screen_x, int screen_y) {
                screenLocation[X] = (float) screen_x;
                screenLocation[Y] = (float) screen_y;
            }
            float[] get_screenLocation () {
                return screenLocation;
            }

            public boolean isInsideView() {
                if(screenLocation[X] > 0 && screenLocation[X] < viewWidth && screenLocation[Y] > 0 && screenLocation[Y] < viewHeight) {
                    return true;
                }
                else {
                    return false;
                }
            }
        }

        public PicFromPics_map(String dataset[], Bitmap image, int width, int height) {
            int x = 0;
            int y = 0;
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(image, width, height, false);

            // do something to reduce color depth here

            this.points = new PicPoint[width][height];
            this.rows = height;
            this.columns = width;

            // make Dataset match color palatte

           while(y < rows) {
               while(x < columns) {
                   points[x][y].color = scaledBitmap.getPixel(x,y);
                   // bind PicPoint to Rebus according to Color

                   x++;
               }
               y++;
           }
        }



        public float[] get_locations() {
            float locations[] = new float[rows * columns * 2];

            int x = 0;
            int y = 0;
            int n = 0;

            while(y < this.rows) {
                while(x < this.columns) {
                    locations[n] = points[x][y].screenLocation[X];
                    n++;
                    locations[n] = points[x][y].screenLocation[Y];
                    n++;
                    x++;
                }
                y++;
            }

            return locations;
        }
        public void set_locations(float[] new_locations) {
            int x = 0;
            int y = 0;
            int n = 0;

            if(new_locations.length == this.rows * this.columns * 2) {
                while(y < this.rows) {
                    while(x < this.columns) {
                        points[x][y].screenLocation[X] = new_locations[n];
                        n++;
                        points[x][y].screenLocation[Y] = new_locations[n];
                        n++;
                        x++;
                    }
                    y++;
                }
            }
        }


    }

    /*
     * Definition of the View itself NOTHING DONE JET
     */
    //private Rebus getRebusByColor(colorpalette, dataset, color) {}

    //private PicFromPics_map updateMapRebuses(PicfromPics_map, dataset) {}

    public pinchZoomableGridLayout(Context context, AttributeSet attrs) {
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

  /*  @Override
    public boolean onTouchEvent(MotionEvent event) {
        PickerView.mPoint newP = new PickerView.mPoint(event.getX(),event.getY());
        PickerView.mPoint mVect = new PickerView.mPoint(0,0);

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
                for (PickerView.TextField field : values) {
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
    }*/

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int stepX = 0;
        int stepY = 0;
        int tempLocation[] = new int[] {circleR,circleR};



        if(map != null) {

            //initialize map with standart size and padding if not already done
            if(!map.isInitialized){
                while(stepY < map.rows) {
                    while(stepX < map.columns) {
                        map.points[stepX][stepY].set_screenLocation(tempLocation[X], tempLocation[Y]);

                        tempLocation[X] += padding + (2*circleR);
                        stepX++;
                    }
                    tempLocation[Y] += padding + (2*circleR);
                    stepY++;
                    stepX = 0;
                }
                updated_locations = new float[map.get_locations().length];
                map.isInitialized = true;
            }
            //otherwise draw points
            else {
                // apply transformations from touch gestures MOVE THIS TO ONTOCUHGESTURE
                matrix.mapVectors(map.get_locations(),updated_locations);
                map.set_locations(updated_locations);

                while(stepY < map.rows) {
                    while(stepX < map.columns) {
                        // only draw visible items to canvas
                        if(map.points[stepX][stepY].isInsideView()) {
                            canvas.drawCircle(map.points[stepX][stepY].get_screenLocation()[X], map.points[stepX][stepY].get_screenLocation()[X], circleR, drawPaint);
                        }
                        stepX++;
                    }
                    stepY++;
                    stepX = 0;
                }
            }
        }
    }

    public void setMap(PicFromPics_map new_map) {
        this.map = new_map;
    }
    public PicFromPics_map getMap() {
        return map;
    }

    @Override
    public void onSizeChanged(int w, int h, int old_w, int old_h) {
        super.onSizeChanged(w, h, old_w, old_h);
        viewWidth = w;
        viewHeight = h;
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
