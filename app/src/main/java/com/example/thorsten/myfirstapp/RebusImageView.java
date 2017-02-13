package com.example.thorsten.myfirstapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

/**
 * Created by Thorsten on 30.01.2017.
 */
public class RebusImageView extends ImageView {
    private static final int X = 0;
    private static final int Y = 1;
    public static final int MODE_BLUR = 0;
    public static final int MODE_PUZZLE = 1;
    public static final int MODE_VORONOI = 2;
    public static final int MODE_SOLVED = 3;

    Rebus rebus = null;

    public RebusImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setImageBitmap(Bitmap bitmap) {
        if(rebus != null) {
            switch (rebus.rebusMode) {
                case MODE_BLUR:
                    super.setImageBitmap(this.getBlurredImage(getContext(), bitmap));
                    break;
                case MODE_PUZZLE:
                    super.setImageBitmap(this.getShuffledImage(bitmap));
                    break;
                case MODE_SOLVED:
                    super.setImageBitmap(bitmap);
                    break;
                case MODE_VORONOI:
                default:
                    super.setImageBitmap(this.getBlurredImage(getContext(), bitmap));
                    break;
            }
        }
        else {
            super.setImageBitmap(bitmap);
        }
    }

    public void setRebus(Rebus newRebus) {
        rebus = newRebus;
    }

    private Bitmap getBlurredImage(Context context, Bitmap image) {
        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, rebus.blurSize, rebus.blurSize, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        android.support.v8.renderscript.RenderScript rs = android.support.v8.renderscript.RenderScript.create(context);
        android.support.v8.renderscript.ScriptIntrinsicBlur theIntrinsic = android.support.v8.renderscript.ScriptIntrinsicBlur.create(rs, android.support.v8.renderscript.Element.U8_4(rs));
        android.support.v8.renderscript.Allocation tmpIn = android.support.v8.renderscript.Allocation.createFromBitmap(rs, inputBitmap);
        android.support.v8.renderscript.Allocation tmpOut = android.support.v8.renderscript.Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(rebus.blurRadius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    private Bitmap getShuffledImage(Bitmap image) {
        Bitmap pieceBitmap;
        Bitmap puzzledImage = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas puzzledImageCanvas = new Canvas(puzzledImage);
        int[] position = {0,0};
        int pieceCount = 0;
        int piece_with = (image.getWidth() / rebus.num_x_y);
        int piece_height = (image.getHeight() / rebus.num_x_y);

        //loop through all pieces and store shuffled fragments in shuffled image

        while(position[Y] < rebus.num_x_y) {
            while( position[X] < rebus.num_x_y) {
                pieceBitmap = Bitmap.createBitmap(image, (rebus.puzzlePieceOrder[pieceCount][X] * piece_with), (rebus.puzzlePieceOrder[pieceCount][Y] * piece_height), piece_with, piece_height);

                puzzledImageCanvas.drawBitmap(pieceBitmap, (float) (position[X] * piece_with), (float) (position[Y] * piece_height), null);
                pieceCount++;

                position[X]++;
            }

            position[X] = 0;
            position[Y]++;
        }

        return puzzledImage;
    }

    static class Rebus {
        private String imageName; // or checksum here
        private int rebusMode = MODE_BLUR;

        private Integer[][] puzzlePieceOrder = null;
        int num_x_y;
        int numberOfPieces = 16;

        private int blurSize = 400;
        private int blurRadius = 20;

        public Rebus(String imagePath, int rebusMode) {
            this.imageName = imagePath; // generate checksum here
            switch(rebusMode) {
                case MODE_BLUR:
                    this.rebusMode = MODE_BLUR;
                    break;
                case MODE_PUZZLE:
                    this.rebusMode = MODE_PUZZLE;
                    this.setupPuzzle();
                    break;
                case MODE_VORONOI:
                    this.rebusMode = MODE_VORONOI;
                    break;
                default:
                    this.rebusMode = MODE_BLUR;
                    break;
            }
        }

        public void setBlurSize(int width_height) {
            if(width_height > 0) {
                this.blurSize = width_height;
            }
        }

        public void setBlurRadius(int radius) {
            if(radius > 0) {
                this.blurRadius = radius;
            }
        }

        public void setRebusMode(int mode) {
            switch(mode) {
                case MODE_BLUR:
                    this.rebusMode = MODE_BLUR;
                    break;
                case MODE_PUZZLE:
                    this.rebusMode = MODE_PUZZLE;
                    break;
                case MODE_VORONOI:
                    this.rebusMode = MODE_VORONOI;
                    break;
                case MODE_SOLVED:
                    this.rebusMode = MODE_SOLVED;
                    break;
                default:
                    this.rebusMode = MODE_BLUR;
                    break;
            }
        }

        public void setNumberOfPieces(int newNumber) {
            numberOfPieces = newNumber;
        }

        public  void rebuildPuzzle() {
            setupPuzzle();
        }

        private void setupPuzzle() {
            this.num_x_y = (int) Math.round( Math.sqrt(numberOfPieces) );
            this.numberOfPieces = num_x_y * num_x_y;
            int[] position = {0,0};
            int pieceCount = 0;

            puzzlePieceOrder = new Integer[numberOfPieces][2];

            // setting up list of puzzle pieces
            while(position[Y] < num_x_y) {
                while( position[X] < num_x_y) {
                    puzzlePieceOrder[pieceCount][X] = position[X];
                    puzzlePieceOrder[pieceCount][Y] = position[Y];
                    position[X]++;
                    pieceCount++;
                }

                position[X] = 0;
                position[Y]++;
            }

            //shuffle list of puzzle pieces
            Collections.shuffle(Arrays.asList(puzzlePieceOrder));
        }
    }
}