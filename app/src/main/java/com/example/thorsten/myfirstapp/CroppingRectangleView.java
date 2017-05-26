package com.example.thorsten.myfirstapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Thorsten on 04.12.2016.
 */


public class CroppingRectangleView extends ImageView {
    static final int VISIBLE = 1;
    static final int INVISIBLE = 2;
    private int RectangleVisibility = INVISIBLE;

    private static final int BEFORE_START = 0;

    static final int POINT_ON_A = 1;
    static final int POINT_ON_B = 2;
    static final int POINT_ON_C = 3;
    static final int POINT_ON_D = 4;
    private static final int POINT_OUTSIDE_RECT = 5;
    private static final int POINT_INSIDE_RECT = 6;

    private int SCALE_FACTOR = (int) this.getResources().getDisplayMetrics().density;
    private final int MINIMUM_RECTANGLE_SIZE = SCALE_FACTOR * 100;

    private final int CROPPED_IMAGE_WIDTH_HEIGHT = 1024;

    private int viewWidth;
    private int viewHeight;
    private final int paintColor = Color.WHITE;
    private Paint drawPaint;

    private Rectangle mRect = new Rectangle(new mPoint(SCALE_FACTOR * 100,SCALE_FACTOR * 100), MINIMUM_RECTANGLE_SIZE, MINIMUM_RECTANGLE_SIZE, SCALE_FACTOR * 10);

    private int touchStartPoint_flag = BEFORE_START;
    private mPoint touchStartPoint;

    public CroppingRectangleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFocusable(true);
        setFocusableInTouchMode(true);
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

    class Rectangle {
        private Circle handleA;
        private Circle handleB;
        private Circle handleC;
        private Circle handleD;
        private Line A_to_D;
        private Line B_to_C;

        public Rectangle(mPoint a, int w, int h, int r) {
            this.handleA = new Circle(a,r);
            this.handleB = new Circle(new mPoint((a.x + w),a.y),r);
            this.handleC = new Circle(new mPoint(a.x,(a.y + h)),r);
            this.handleD = new Circle(new mPoint((a.x+w),(a.y+h)),r);

            this.A_to_D = new Line(handleA.c,handleD.c);
            this.B_to_C= new Line(handleB.c, handleC.c);
        }

        public void move(mPoint v, boolean stay_in_view) {
            if(stay_in_view) {
                v.x = ((handleA.c.x + v.x) < 0) ? 0 : v.x;
                v.x = ((handleD.c.x + v.x) > viewWidth) ? (viewWidth - handleD.c.x) : v.x;
                v.y = ((handleA.c.y + v.y) < 0) ? 0 : v.y;
                v.y = ((handleD.c.y + v.y) > viewHeight) ? (viewHeight - handleD.c.y) : v.y;
            }

            handleA.move(v);
            handleB.move(v);
            handleC.move(v);
            handleD.move(v);
        }

        public void setHandle(int handle, mPoint p, boolean keep_aspect_ratio) {
            if(keep_aspect_ratio) {
                if(handle == POINT_ON_A || handle == POINT_ON_D) {
                    p = ClosestPointToSegment(p, A_to_D, true);
                }
                else {
                    p = ClosestPointToSegment(p, B_to_C, true);
                }
            }

            switch (handle) {
                case POINT_ON_A:
                    if ((handleB.c.x - p.x) > MINIMUM_RECTANGLE_SIZE) {
                        handleA.c = p;
                        handleC.c.x = handleA.c.x;
                        handleB.c.y = handleA.c.y;
                    }
                    break;
                case POINT_ON_B:
                    if ((p.x - handleA.c.x) > MINIMUM_RECTANGLE_SIZE) {
                        handleB.c = p;
                        handleD.c.x = handleB.c.x;
                        handleA.c.y = handleB.c.y;
                    }
                    break;
                case POINT_ON_C:
                    if ((handleB.c.x - p.x) > MINIMUM_RECTANGLE_SIZE) {
                        handleC.c = p;
                        handleA.c.x = handleC.c.x;
                        handleD.c.y = handleC.c.y;
                    }
                    break;
                case POINT_ON_D:
                    if ((p.x - handleA.c.x) > MINIMUM_RECTANGLE_SIZE) {
                        handleD.c = p;
                        handleB.c.x = handleD.c.x;
                        handleC.c.y = handleD.c.y;
                    }
                    break;
                default:
                    break;
            }
        }

        private int getPointPosition(mPoint p) {
            if(handleA.contains(p)) { return POINT_ON_A ;}
            else if(handleB.contains(p)) { return POINT_ON_B ;}
            else if(handleC.contains(p)) { return POINT_ON_C ;}
            else if(handleD.contains(p)) { return POINT_ON_D ;}
            else if(this.contains(p)) { return POINT_INSIDE_RECT ;}
            else { return POINT_OUTSIDE_RECT;}
        }

        private boolean contains(mPoint p) {
            if (handleA.c.x < p.x && p.x < handleD.c.x) {
                if (handleA.c.y < p.y && p.y < handleD.c.y) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        public void updateDiagonals() {
            A_to_D = new Line(handleA.c, handleD.c);
            B_to_C = new Line(handleB.c, handleC.c);
        }
    }

    class Circle {
        mPoint c;
        float r;

        public Circle(mPoint c, float r) {
            this.c = c;
            this.r = r;
        }

        public boolean contains(mPoint p) {
            float distance = (float) Math.sqrt(Math.pow(p.x - c.x, 2) + Math.pow(p.y - c.y, 2));
            distance = (distance < 0) ? -distance : distance;

            if(distance < 2 * r) {
                return true;
            }
            else {
                return false;
            }
        }

        public void move(mPoint v) {
            c.x = c.x + v.x;
            c.y = c.y + v.y;

        }
    }

    class mPoint {
        float x;
        float y;

        public mPoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    class Line {
        mPoint a;
        mPoint b;

        public Line(mPoint a, mPoint b) {
            this.a = a;
            this.b = b;
        }
    }

    private mPoint ClosestPointToSegment(mPoint p, Line l, boolean stay_in_view) {
        mPoint a = l.a;
        mPoint b = l.b;
        mPoint result;

        float a_to_p_x, a_to_p_y;
        float a_to_b_x, a_to_b_y;
        a_to_p_x = p.x - a.x;
        a_to_p_y = p.y - a.y; //     # Storing vector A->P
        a_to_b_x = b.x - a.x;
        a_to_b_y = b.y - a.y; //     # Storing vector A->B

        float atb2 = a_to_b_x * a_to_b_x + a_to_b_y * a_to_b_y;
        float atp_dot_atb = a_to_p_x * a_to_b_x + a_to_p_y * a_to_b_y; // The dot product of a_to_p and a_to_b
        float t = atp_dot_atb / atb2;  //  # The normalized "distance" from a to the closest point

        result = new mPoint(a.x + a_to_b_x * t,a.y + a_to_b_y * t);

        if(stay_in_view) {
            if(result.x < 0) {
                result.x = 0;
                result.y = ((b.y - a.y) / (b.x - a.x)) * (0 - a.x) + a.y;
            }
            else if(result.x > viewWidth) {
                result.x = viewWidth;
                result.y = ((b.y - a.y) / (b.x - a.x)) * (viewWidth - a.x) + a.y;
            }

            if(result.y < 0) {
                result.x = (0 - a.y) / ((b.y - a.y)/(b.x - a.x)) + a.x;
                result.y = 0;
            }
            else if(result.y > viewHeight) {
                result.x = (viewHeight - a.y) / ((b.y - a.y)/(b.x - a.x)) + a.x;
                result.y = viewHeight;
            }
        }

        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mPoint newP = new mPoint(event.getX(),event.getY());
        mPoint mVect = new mPoint(0,0);

// Checks for the event that occurs
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartPoint_flag = mRect.getPointPosition(newP);
                touchStartPoint = newP;
                break;
            case MotionEvent.ACTION_MOVE:

                switch (touchStartPoint_flag) {
                    case POINT_ON_A:
                        mRect.setHandle(POINT_ON_A, newP, true);
                        break;
                    case POINT_ON_B:
                        mRect.setHandle(POINT_ON_B, newP, true);
                        break;
                    case POINT_ON_C:
                        mRect.setHandle(POINT_ON_C, newP, true);
                        break;
                    case POINT_ON_D:
                        mRect.setHandle(POINT_ON_D, newP, true);
                        break;
                    case POINT_INSIDE_RECT:
                        mVect.x = newP.x - touchStartPoint.x;
                        mVect.y = newP.y - touchStartPoint.y;
                        mRect.move(mVect,true);
                        touchStartPoint = newP;
                        break;
                    default:
/*                            circleD.c = newP;
                        circleB.c.x = circleD.c.x;
                        circleC.c.y = circleD.c.y; */
                        break;
                }
                break;
             case MotionEvent.ACTION_UP:
                 mRect.updateDiagonals();
            default:
                return false;
        }
        // Force view to draw again
        postInvalidate();
        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(RectangleVisibility == VISIBLE) {
            drawPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(mRect.handleA.c.x, mRect.handleA.c.y, mRect.handleD.c.x, mRect.handleD.c.y, drawPaint);
            drawPaint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(mRect.handleA.c.x, mRect.handleA.c.y, mRect.handleA.r, drawPaint);
            canvas.drawCircle(mRect.handleB.c.x, mRect.handleB.c.y, mRect.handleB.r, drawPaint);
            canvas.drawCircle(mRect.handleC.c.x, mRect.handleC.c.y, mRect.handleC.r, drawPaint);
            canvas.drawCircle(mRect.handleD.c.x, mRect.handleD.c.y, mRect.handleD.r, drawPaint);
        }
    }

    @Override
    public void onSizeChanged (int w, int h, int old_w, int old_h){
        super.onSizeChanged(w, h, old_w, old_h);
        viewWidth = w;
        viewHeight = h;
    }

    /*
     *  below this we just have functions to be called from outside this class
     */

    public mPoint getRectangleEdge(int handle) {
        mPoint result;

        switch(handle) {
            case POINT_ON_A:
                result = new mPoint(mRect.handleA.c.x + getPaddingLeft(),mRect.handleA.c.y + getPaddingTop());
                break;
            case POINT_ON_B:
                result = new mPoint(mRect.handleB.c.x + getPaddingLeft(),mRect.handleB.c.y + getPaddingTop());
                break;
            case POINT_ON_C:
                result = new mPoint(mRect.handleC.c.x + getPaddingLeft(),mRect.handleC.c.y + getPaddingTop());
                break;
            case POINT_ON_D:
                result = new mPoint(mRect.handleD.c.x + getPaddingLeft(),mRect.handleD.c.y + getPaddingTop());
                break;
            default:
                return null;
        }
        return result;
    }

    public void setRectangleVisibility(int requestedVisibility) {
        RectangleVisibility = requestedVisibility;
    }

    public int getRectangleWidth() {
        return (int) (mRect.handleB.c.x - mRect.handleA.c.x);
    }

    public int getRectangleHeight() {
        return (int) (mRect.handleC.c.y - mRect.handleA.c.y);
    }

    public String getCroppedImage(String picturePath) {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeFile(picturePath, bitmapOptions);
        int horizontalImageOffset = 0;
        int verticalImageOffset = 0;
        Matrix m =  new Matrix();

        // get dimensions and relative position of cropping rectangle
        float rX = getRectangleEdge(CroppingRectangleView.POINT_ON_A).x / getWidth();
        float rY = getRectangleEdge(CroppingRectangleView.POINT_ON_A).y / getHeight();
        float rWidth = (float) getRectangleWidth() / getWidth();
        float rHeight =  (float) getRectangleHeight() / getHeight();

        //handle image orientation
        try {
            ExifInterface exif = new ExifInterface(picturePath);
            float temp;

            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    verticalImageOffset = getHorizontalImageViewOffset(exif, bitmapOptions);
                    horizontalImageOffset = getVerticalImageViewOffset(exif, bitmapOptions);
                    m.postRotate(90);

                    temp = rX;
                    rX = rY;
                    rY = Math.abs(temp - 1) - rWidth;

                    temp = rWidth;
                    rWidth = rHeight;
                    rHeight = temp;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    horizontalImageOffset = getHorizontalImageViewOffset(exif, bitmapOptions);
                    verticalImageOffset = getVerticalImageViewOffset(exif, bitmapOptions);
                    m.postRotate(180);

                    rX = Math.abs(rX - 1) - rWidth;
                    rY = Math.abs(rY - 1) - rHeight;

                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    verticalImageOffset = getHorizontalImageViewOffset(exif, bitmapOptions);
                    horizontalImageOffset = getVerticalImageViewOffset(exif, bitmapOptions);
                    m.postRotate(270);

                    temp = rY;
                    rY = rX;
                    rX = Math.abs(temp -1 ) - rHeight;

                    temp = rWidth;
                    rWidth = rHeight;
                    rHeight = temp;

                    break;
                default:
                    horizontalImageOffset = getHorizontalImageViewOffset(exif, bitmapOptions);
                    verticalImageOffset = getVerticalImageViewOffset(exif, bitmapOptions);
                    break;
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        //get dimensions of visible area of the picture
        int visibleImageWidth = bitmapOptions.outWidth - (2 * horizontalImageOffset);
        int visibleImageHeight = bitmapOptions.outHeight - (2 * verticalImageOffset);

        // translate relative positions of rectangle to absolute positions on the picture
        int iX = (int) (rX * visibleImageWidth) + horizontalImageOffset;
        int iY = (int) (rY * visibleImageHeight) + verticalImageOffset;
        int iWidth = (int) (rWidth * visibleImageWidth);
        int iHeight = (int) (rHeight * visibleImageHeight);

        // get cropped image and scale to standardized size

        Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, iX, iY, iWidth, iHeight, m, false);
        croppedBitmap = Bitmap.createScaledBitmap(croppedBitmap, CROPPED_IMAGE_WIDTH_HEIGHT, CROPPED_IMAGE_WIDTH_HEIGHT,false);

        // save cropped image
        FileOutputStream fileOutStream = null;
        String newFileName = "croppedImage.jpg";
        try {
            fileOutStream = getContext().openFileOutput(newFileName, Context.MODE_PRIVATE);
            croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutStream);
            fileOutStream.flush();
            fileOutStream.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        // return file Path
        if(fileOutStream != null){
        //    return Uri.fromFile(new File(getContext().getFilesDir() + "/" + newFileName));
            return getContext().getFilesDir() + "/" + newFileName;
        }
        else {
            return null;
        }
    }

    //give back horizontal offset for the sides because of having the image centercropped

    private int getHorizontalImageViewOffset(ExifInterface exif, BitmapFactory.Options bitmapOptions) {
        float viewRatio_W_by_H = ((float) getWidth() / (float) getHeight());
        int imageWidth = bitmapOptions.outWidth;
        int imageHeight = bitmapOptions.outHeight;
        int visibleImageWidth;
        int result;

        if(viewRatio_W_by_H < 1) {
            //handle orientation of image
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                case ExifInterface.ORIENTATION_ROTATE_270:
                    visibleImageWidth = (int) (viewRatio_W_by_H * (float) imageWidth);
                    result = ((imageHeight - visibleImageWidth) / 2);
                    break;
                default:
                    visibleImageWidth = (int) (viewRatio_W_by_H * (float) imageHeight);
                    result = ((imageWidth - visibleImageWidth) / 2);
                    break;
            }
        }
        else {
            result = 0;
        }

        return result;
    }

    //give back vertical offset for the sides because of having the image centercropped

    private int getVerticalImageViewOffset(ExifInterface exif, BitmapFactory.Options bitmapOptions) {
        float viewRatio_H_by_W = ((float) getHeight() / (float) getWidth());
        int imageWidth = bitmapOptions.outWidth;
        int imageHeight = bitmapOptions.outHeight;
        int visibleImageHeight;
        int result;

        if(viewRatio_H_by_W < 1) {
            //handle orientation of image
            switch (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                case ExifInterface.ORIENTATION_ROTATE_270:
                    visibleImageHeight = (int) (viewRatio_H_by_W * (float) imageHeight);
                    result = ((imageWidth - visibleImageHeight) / 2);
                    break;
                default:
                    visibleImageHeight = (int) (viewRatio_H_by_W * (float) imageWidth);
                    result = ((imageHeight - visibleImageHeight) / 2);
                    break;
            }
        }
        else {
            result = 0;
        }

        return result;
    }

}
