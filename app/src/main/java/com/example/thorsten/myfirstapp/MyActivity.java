package com.example.thorsten.myfirstapp;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.hardware.Camera;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;

import org.w3c.dom.Text;

import javax.sql.CommonDataSource;

public class MyActivity extends AppCompatActivity {

    public final static String EXTRA_MESSAGE = "com.example.thorsten.myfirstapp.MESSAGE";
    public final static String EXTRA_IMAGE = "com.example.thorsten.myfirstapp.IMAGE";
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;

    private FragmentTabHost host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);


        // setup Tabs

        host = (FragmentTabHost)findViewById(R.id.tabHost);
        host.setup(this, getSupportFragmentManager(),android.R.id.tabcontent);
        setupTab("List", R.layout.tab_row, R.id.imgTab, R.drawable.list_tab_selector, listfragment.class);
        setupTab("Map", R.layout.tab_row, R.id.imgTab, R.drawable.map_tab_selector, mapfragment.class);
        setupTab("Camera", R.layout.tab_row, R.id.imgTab, R.drawable.camera_tab_selector, camerafragment.class);

        // set image in upper left corner
        ImageView avatar = (ImageView) findViewById(R.id.avatar_image_view);
        avatar.setImageDrawable(getResources().getDrawable(R.mipmap.logo_monocle));

    }

    private void setupTab(String tName, int tIndicatorLayoutId, int tIndicatorViewId, int tIndicatorIconId, Class tFragmentClass) {
        TabHost.TabSpec spec = host.newTabSpec(tName);
        View v = LayoutInflater.from(this).inflate(tIndicatorLayoutId, null);
        ImageView tabView = (ImageView) v.findViewById(tIndicatorViewId);
        tabView.setImageDrawable(getResources().getDrawable(tIndicatorIconId));
        spec.setIndicator(v);
        host.addTab(spec, tFragmentClass, null);
    }
/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
*/
}

