package com.example.thorsten.myfirstapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.DrawableCrossFadeFactory;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;


public class RebusActivity extends AppCompatActivity {

    final int BLUR_RADIUS = 20;
    final float BITMAP_SCALE = 0.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rebus);

        Bundle bundle = getIntent().getExtras();
        final String picturePath = bundle.getString("picturePath");
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        final Rebus newRebus = new Rebus(picturePath, RebusImageView.MODE_BLUR);

        // change this from RebusImageView to a normal view, since thats the one we want to use

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
                                if(mCheckButton != null && mCheckButton.getVisibility() == View.VISIBLE) { mCheckButton.setVisibility(View.INVISIBLE); }
                                break;
                            case "Blur":
                                newRebus.setRebusMode(RebusImageView.MODE_BLUR);
                                if(mCheckButton != null && mCheckButton.getVisibility() == View.INVISIBLE) { mCheckButton.setVisibility(View.VISIBLE); }
                                break;
                            case "Puzzle":
                                newRebus.setRebusMode(RebusImageView.MODE_PUZZLE);
                                newRebus.rebuildPuzzle();
                                if(mCheckButton != null && mCheckButton.getVisibility() == View.INVISIBLE) { mCheckButton.setVisibility(View.VISIBLE); }
                                break;
                            default:
                                break;
                        }

                        Glide.with(getBaseContext())
                                .load(picturePath)
                                .crossFade(200)
                                .skipMemoryCache(true)
                                .diskCacheStrategy(DiskCacheStrategy.NONE)
                                .transform(new Rebus.GlideTransformation(getBaseContext(), newRebus))
                                .into(rebusView);
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
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(rebusView);

        }
    }
}
