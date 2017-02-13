package com.example.thorsten.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.Image;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.bumptech.glide.load.resource.drawable.DrawableResource;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CroppingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_rectangle);

        Bundle bundle = getIntent().getExtras();
        final String picturePath = bundle.getString("picturePath");

        final CroppingRectangleView mCropView = (CroppingRectangleView) findViewById(R.id.cropping_view);
        final ImageView mBackButton = (ImageView) findViewById(R.id.back_button);
        final ImageView mCheckButton = (ImageView) findViewById(R.id.check_button);
        final StepperView mStepperView = (StepperView) findViewById(R.id.stepper_view);

        if (mBackButton != null) {
            mBackButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        if (mCheckButton != null) {
            mCheckButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (mCropView != null) {
                      // Uri pictureUri = mCropView.getCroppedImage(picturePath);
                        String newPicturePath = mCropView.getCroppedImage(picturePath);

                        final Intent newIntent = new Intent(CroppingActivity.this, RebusActivity.class);
                        final Bundle newBundle = new Bundle();
                      //  newBundle.putString("picturePath", pictureUri.toString());
                        newBundle.putString("picturePath", newPicturePath);

                        newIntent.putExtras(newBundle);
                        startActivity(newIntent);
                    }
                }
            });
        }

        //load new picture into imageview with glide

        if (mCropView != null && mCheckButton != null && mStepperView != null) {
            mStepperView.setNumberOfSteps(3);
            mStepperView.setStepProgress(1);

            Glide.with(getBaseContext())
                    .load(picturePath)
                    .asBitmap()
                    .centerCrop()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new BitmapImageViewTarget(mCropView) {
                        @Override
                        public void onResourceReady(Bitmap drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                            mCheckButton.setVisibility(View.VISIBLE);
                            mCropView.setRectangleVisibility(CroppingRectangleView.VISIBLE);
                        }
                    });
            //    .into(mCropView);
        }
    }
}