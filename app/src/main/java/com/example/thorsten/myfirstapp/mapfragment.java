package com.example.thorsten.myfirstapp;

import android.content.Context;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class mapfragment extends Fragment  {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View V = inflater.inflate(R.layout.maptab, container, false);
        showExtraMenu(V);

        // setup osmdroid map view
        Context ctx = getContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        MapView map = (MapView) V.findViewById(R.id.map);
        map.setMultiTouchControls(true);

        map.setTileSource(TileSourceFactory.MAPNIK);

        IMapController mapController = map.getController();
        mapController.setZoom(40);
        GeoPoint startPoint = new GeoPoint(53.551085, 9.993682);
        mapController.setCenter(startPoint);

        MyLocationNewOverlay myLocationoverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getActivity().getBaseContext()), map);
        myLocationoverlay.enableMyLocation();


        map.getOverlays().add(myLocationoverlay);

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
