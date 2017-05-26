package com.example.thorsten.myfirstapp;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class listfragment extends Fragment {

    private RecyclerView mRecyclerViewSolved, mRecyclerViewUnsolved, mRecyclerViewOwn;
    private RecyclerView.Adapter mAdapterUnsolved, mAdapterSolved, mAdapterOwn;
    private RecyclerView.LayoutManager mLayoutManagerUnsolved, mLayoutManagerSolved, mLayoutManagerOwn;

    public String[] listFiles(String dirName) {
        AssetManager assetManager = getContext().getAssets();
        File dir = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/");
        String[] fileList = null;

        if(dir.exists()) {
            //fileList = dir.list();
            try  {
                fileList = assetManager.list(dirName);

                for(int i = 0; i < fileList.length; i++) {
                    fileList[i] = "file:///android_asset/sample_images/" + fileList[i];
                }

            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        return fileList;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment ONLY for the first time it gets focus

        View V = inflater.inflate(R.layout.listtab, container, false);
        hideExtraMenu(V);

        // prepare Recylcervciew

        mRecyclerViewUnsolved = (RecyclerView) V.findViewById(R.id.recycler_view_unsolved);
        mRecyclerViewSolved = (RecyclerView) V.findViewById(R.id.recycler_view_solved);
        mRecyclerViewOwn = (RecyclerView) V.findViewById(R.id.recycler_view_own);
        //mRecyclerView.setHasFixedSize(true);

        mLayoutManagerUnsolved = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        mLayoutManagerSolved = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);
        mLayoutManagerOwn = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL, false);

        mRecyclerViewUnsolved.setLayoutManager(mLayoutManagerUnsolved);
        mRecyclerViewSolved.setLayoutManager(mLayoutManagerSolved);
        mRecyclerViewOwn.setLayoutManager(mLayoutManagerOwn);

        mAdapterUnsolved = new MyListAdapter( listFiles("sample_images") );
        mAdapterSolved = new MyListAdapter( listFiles("sample_images") );
        mAdapterOwn = new MyListAdapter( listFiles("sample_images") );

        mRecyclerViewUnsolved.setAdapter(mAdapterUnsolved);
        mRecyclerViewSolved.setAdapter(mAdapterSolved);
        mRecyclerViewOwn.setAdapter(mAdapterOwn);

        return V;
    }

    public void hideExtraMenu(View V) {
        FrameLayout chat = (FrameLayout) getActivity().findViewById(R.id.chat_layout);
        TextView av_name = (TextView) getActivity().findViewById(R.id.avatar_name);
        ImageView av_image = (ImageView) getActivity().findViewById(R.id.avatar_image_view);
        LinearLayout tab_bar = (LinearLayout) getActivity().findViewById(R.id.tab_bar);

        if(chat.getVisibility() == View.VISIBLE) {

            // Prepare the View for the animation

            chat.setVisibility(View.GONE);
            av_name.setVisibility(View.GONE);
            av_image.setBackgroundColor(Color.parseColor("#ffffff"));
/*

// Start the animation

            chat.animate()
                    .translationYBy(-1 * chat.getHeight())
                    .setDuration(500)
                    .alpha(.0f);
            av_name.animate()
                    .translationYBy(-1 * av_name.getHeight())
                    .setDuration(500)
                    .alpha(0.0f);

//           float new_av_image_height = (float) ( tab_bar.getHeight() - chat.getHeight() ) / av_image.getHeight();

            av_image.animate()
                    .translationYBy(-1 * av_name.getHeight())
                    //  .scaleYBy(tab_bar.getHeight() + chat.getHeight() - av_name.getHeight())
                    .setDuration(500)
                  //  .scaleY(new_av_image_height)
                    .alpha(1.0f);
*/
        }
    }
}

