package com.example.thorsten.myfirstapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.net.Uri;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by thorsten on 30.05.17.
 */


public class MosaicImageMap {
    private static final int X = 0;
    private static final int Y = 1;

    // for testing only have dataset here

    Context context;
    private Bitmap masterImage;     // the master mosaic image
    boolean isInitialized = false;
    int rows, columns;              // resolution of master image

    public PicPoint[][] points;     // containing information about vibrant color at this spot and location on screen
    private List<tempImage> palette_images; // list with palette images from dataset (depends on user), which is used as a list with all available images and the corresponding color


    /*
     * constructor and functions of the MosaicImageMap class itself
     */

    public MosaicImageMap(Context context, String dataset[], Bitmap masterImage, int width, int height) {
        int x = 0;
        int y = 0;
        this.palette_images = new ArrayList<tempImage>();
        this.points = new PicPoint[width][height];
        this.rows = height;
        this.columns = width;
        this.context = context;

        // reduce colordepth of masterImage and bind each pixels information to one PicPoint

        this.masterImage = Bitmap.createScaledBitmap(masterImage, width, height, false);
        this.masterImage = decreaseColorDepth(this.masterImage,64);

        while(y < rows) {
            while(x < columns) {
                // bind PicPoint to Rebus according to Color here later
                // at this point just transfer color information and leave rebus "null"
                this.points[x][y] = new PicPoint(this.masterImage.getPixel(x,y));
                x++;
            }
            x = 0;
            y++;
        }

        // create array for color palette palette_images
        // save rebuses here later inside palette images;
        for(String imagePath : dataset) {
            palette_images.add(new tempImage(imagePath));
        }

    }

    /*
     * definition of the PicPoint class, which represents each pixels position, color and connected Rebus
     */

    public class PicPoint {
        private float screenLocation[] = new float[2]; //must be set, updated and maintained by view
        private int color;

        private PicPoint(int color) {
            this.color = color;
        }

        void set_color(int color) {
            this.color = color;
        }
        int get_color() {
            return color;
        }

        public void set_screenLocation (int screen_x, int screen_y) {
            this.screenLocation[X] = (float) screen_x;
            this.screenLocation[Y] = (float) screen_y;
        }
        public float[] get_screenLocation () {
            return screenLocation;
        }

        public boolean isInsideView(int viewWidth, int viewHeight, int circleR) {
            if(screenLocation[X] > -1 * circleR && screenLocation[X] < viewWidth + circleR && screenLocation[Y] > -1 * circleR && screenLocation[Y] < viewHeight + circleR) {
                return true;
            }
            else {
                return false;
            }
        }
    }

    /*
     * definition of tempImage Class, which holds a prescaled version of the image, which is updated after scaling by mosaicimageview
     * also this holds the information abouut the vibrant color in the image and it holds the rebus itself later
     */

    public class tempImage {
        private int color = 0;
        private String pathtoImage; // save rebus in place here
        private Bitmap tempImage = null; // is updated by mosaicimageview every time after scaling
        private Bitmap bufferedTempImage = null;

        public tempImage(String path) {
            this.color = 0;
            this.pathtoImage = path;

            // DOES THIS REALY GET US THE DOMINANT COLOR?
            Glide.with(context)
                    .load(Uri.parse(path)) //change this after testing!!
                    .asBitmap()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<Bitmap> (1, 1) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            color = findEquivalentColor(masterImage, bitmap.getPixel(0,0));
                        }
                    });

            // no match found return empty bitmap
            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            tempImage = Bitmap.createBitmap(10, 10, conf); // this creates a MUTABLE bitmap
        }

        void set_color(int color) {
            this.color = color;
        }
        int get_color() {
            return color;
        }

        void set_pathToTempimage(String path) {
            pathtoImage = path;
        }
        String get_pathToTempImage() {
            return pathtoImage;
        }

        void set_tempImage (Bitmap image) {
            this.tempImage = image;
            this.bufferedTempImage = null;
        }
        Bitmap get_tempImage(int width, int height) {
            Paint drawPaint = new Paint();
            drawPaint.setStyle(Paint.Style.FILL);

            if(bufferedTempImage != null) {
                if(width == bufferedTempImage.getWidth() || height == bufferedTempImage.getHeight()) {
                    return bufferedTempImage;
                }
            }

            bufferedTempImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas tempCanvas = new Canvas(bufferedTempImage);
            tempCanvas.drawCircle(width / 2,height / 2, width / 2,drawPaint);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            tempCanvas.drawBitmap(Bitmap.createScaledBitmap(tempImage, width, height, false),0,0,drawPaint);

            return bufferedTempImage;
        }

    }

    /*
     * Definition of other functions in MosaicImagemap below
     */

    // this function returns the right image from palette_images for the requested color

    tempImage get_image_by_color(int reqColor) {
        for(int n = 0; n < this.palette_images.size(); n++) {
            if(this.palette_images.get(n).get_color() == reqColor) {
                return this.palette_images.get(n);
            }
        }

        // CHANGE THIS; DONT RETURN FIRST ELEMENT
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        return null; // this creates a MUTABLE bitmap
    }

    // updates all tempImage in map to actual size
    public void updateTempImages(int width, int height) {
        for(int n = 0; n < this.palette_images.size(); n++) {
            final int pos = n;
            Glide.with(context)
                    .load(this.palette_images.get(n).get_pathToTempImage()) //change this after testing!!
                    .asBitmap()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new SimpleTarget<Bitmap> (width, height) {
                        @Override
                        public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                            palette_images.get(pos).set_tempImage(bitmap);
                        }
                    });
        }

    }

    public void update_locationsByMatrix(Matrix new_matrix) {
        float old_locations[] = this.get_locations();
        float updated_locations[] = new float[old_locations.length];

        new_matrix.mapPoints(updated_locations, old_locations);
        this.set_locations(updated_locations);

    }

    private float[] get_locations() {
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
            x = 0;
            y++;
        }

        return locations;
    }
    private void set_locations(float[] new_locations) {
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
                x = 0;
                y++;
            }
        }
    }

    //IMPROOVE THIS FUNCTION!
    // finds the color from src colorpalette which ist closest to srcColor

    private int findEquivalentColor(Bitmap src, int srcColor) {
        // get image size
        int equivalentColor;
        int width = src.getWidth();
        int height = src.getHeight();
        float diff;
        float best = 100f;

        //color information
        int A, R, G, B;
        int pixel;

        equivalentColor = -1;

        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // round-off color offset
                diff = Math.abs((A / Color.alpha(srcColor) -1));
                diff += Math.abs((A / Color.red(srcColor) -1));
                diff += Math.abs((A / Color.green(srcColor) -1));
                diff += Math.abs((A / Color.blue(srcColor) -1));

                diff = diff / 4;

                if(diff < best) {
                    //check if this color is already fixed to a image
                    if(get_image_by_color(pixel) == null ) {
                        equivalentColor = pixel;
                        best = diff;
                    }
                }
            }
        }

        // return final image
        return equivalentColor;
    }

    // IMPROVE THIS FUNCTION
    // USE OPENCVs KMEANS FUNCTION HERE
    // reduces the color in src. right now this is done bit offsetting, maybe theres a better way

    private static Bitmap decreaseColorDepth(Bitmap src, int bitOffset) {
        // get image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // round-off color offset
                R = ((R + (bitOffset / 2)) - ((R + (bitOffset / 2)) % bitOffset) - 1);
                if(R < 0) { R = 0; }
                G = ((G + (bitOffset / 2)) - ((G + (bitOffset / 2)) % bitOffset) - 1);
                if(G < 0) { G = 0; }
                B = ((B + (bitOffset / 2)) - ((B + (bitOffset / 2)) % bitOffset) - 1);
                if(B < 0) { B = 0; }

                // set pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }
}

