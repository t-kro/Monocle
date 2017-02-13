package com.example.thorsten.myfirstapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

public class RebusActivity_back extends AppCompatActivity {

    final int BLUR_RADIUS = 20;
    final float BITMAP_SCALE = 0.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rebus);

        Bundle bundle = getIntent().getExtras();
        final String picturePath = bundle.getString("picturePath");
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        final Bitmap image = BitmapFactory.decodeFile(picturePath, bitmapOptions);
        final RebusImageView.Rebus newRebus = new RebusImageView.Rebus(picturePath,RebusImageView.MODE_BLUR);

        final RebusImageView rebusView = (RebusImageView) findViewById(R.id.rebus_view);
        final ImageView mBackButton = (ImageView) findViewById(R.id.back_button);
        final ImageView mCheckButton = (ImageView) findViewById(R.id.check_button);
        final StepperView mStepperView = (StepperView) findViewById(R.id.stepper_view);
        final PickerView mPickerView = (PickerView) findViewById(R.id.picker_view);

        // setting up OnClickListeners for Buttons

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
                    if (rebusView != null) {
                        rebusView.setRebus(newRebus);
                        rebusView.setImageBitmap(image);
                    }
                }
            });
        }

        // setup PickerView and Listener for Focus

        if(mPickerView != null) {
            mPickerView.addTextField("Original");
            mPickerView.addTextField("Blur");
            mPickerView.addTextField("Puzzle");


            mPickerView.setOnNewFocusListener(new PickerView.OnNewFocusListener() {
                @Override
                public void onEvent() {
                    if (rebusView != null) {
                        switch (mPickerView.getFocusedField().getText()) {
                            case "Original":
                                newRebus.setRebusMode(RebusImageView.MODE_SOLVED);
                                rebusView.setRebus(newRebus);
                                rebusView.setImageBitmap(image);

                                if(mCheckButton != null && mCheckButton.getVisibility() == View.VISIBLE) { mCheckButton.setVisibility(View.INVISIBLE); }
                                break;
                            case "Blur":
                                newRebus.setRebusMode(RebusImageView.MODE_BLUR);
                                rebusView.setRebus(newRebus);
                                rebusView.setImageBitmap(image);

                                if(mCheckButton != null && mCheckButton.getVisibility() == View.INVISIBLE) { mCheckButton.setVisibility(View.VISIBLE); }
                                break;
                            case "Puzzle":
                                newRebus.setRebusMode(RebusImageView.MODE_PUZZLE);
                                newRebus.rebuildPuzzle();
                                rebusView.setRebus(newRebus);
                                rebusView.setImageBitmap(image);

                                if(mCheckButton != null && mCheckButton.getVisibility() == View.INVISIBLE) { mCheckButton.setVisibility(View.VISIBLE); }
                                break;
                            default:
                                break;
                        }
                    }
                }
            });
        }

        //load cropped picture into imageview with glide

        if (rebusView != null && mCheckButton != null && mStepperView != null) {
            mStepperView.setNumberOfSteps(3);
            mStepperView.setStepProgress(2);

            Glide.with(getBaseContext())
                    .load(picturePath)
                    .asBitmap()
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(new BitmapImageViewTarget(rebusView) {
                        @Override
                        public void onResourceReady(Bitmap drawable, GlideAnimation anim) {
                            super.onResourceReady(drawable, anim);
                           // mBlurButton.setVisibility(View.VISIBLE);
                        }
                    });
        }
    }
}