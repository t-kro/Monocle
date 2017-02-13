package com.example.thorsten.myfirstapp;

import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabWidget;
import android.widget.TextView;

public class mapfragment extends Fragment  {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.maptab, container, false);
        showExtraMenu(V);


        return V;
    }

    public void showExtraMenu(View V) {
        FrameLayout chat = (FrameLayout) getActivity().findViewById(R.id.chat_layout);
        TextView av_name = (TextView) getActivity().findViewById(R.id.avatar_name);
        ImageView av_image = (ImageView) getActivity().findViewById(R.id.avatar_image_view);
        LinearLayout tab_bar = (LinearLayout) getActivity().findViewById(R.id.tab_bar);

        chat.setVisibility(View.VISIBLE);
        av_name.setVisibility(View.VISIBLE);
        av_image.setBackgroundColor(Color.parseColor("#f8f8f8"));
/*
        // Prepare the View for the animation
        chat.setAlpha(0.0f);
        av_name.setAlpha(0.0f);

// Start the animation

        chat.animate()
                .translationYBy(chat.getHeight())
                .setDuration(500)
                .alpha(1.0f);
        av_name.animate()
                .translationYBy(av_name.getHeight())
                .setDuration(500)
                .alpha(1.0f);
        av_image.animate()
                .translationYBy(av_name.getHeight())
              //  .scaleYBy(tab_bar.getHeight() + chat.getHeight() - av_name.getHeight())
                .setDuration(500)
                .alpha(1.0f);
        */
    }
}
