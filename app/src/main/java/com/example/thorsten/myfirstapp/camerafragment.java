package com.example.thorsten.myfirstapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.security.Timestamp;
import java.util.List;

public class camerafragment extends Fragment implements SurfaceHolder.Callback {
    private static final String TAG = camerafragment.TAG;

    public CameraThread mCameraThread;
    private CameraThread.CameraHandler ch;
    public SurfaceView cSurfaceView;

    ImageView mShutterButton = null;
    ImageView mCheckButton = null;

    private UiHandler mHandler = new UiHandler();
    String recentPicturePath = null;

    /*
     * Definition of the UiHandler class for communication with the CameraThread
     */

    private class UiHandler extends Handler {
        private static final int CAMERA_READY = 0;
        private static final int PICTURE_TAKEN = 1;
        private static final int PICTURE_SAVED = 2;
        private static final int PICTURE_NEW = 3;

        public UiHandler() {
        }

        public void sendCameraReady() {
            sendMessage(obtainMessage(CAMERA_READY));
        }

        public void sendPictureSaved(String pictureName) {
            Bundle bundle = new Bundle();
            Message message = obtainMessage(PICTURE_SAVED);
            bundle.putString("picturePath", pictureName);
            message.setData(bundle);
            sendMessage(message);
        }

        public void sendPictureTaken() {
            sendMessage(obtainMessage(PICTURE_TAKEN));
        }

        public void sendPictureNew() {
            sendMessage(obtainMessage(PICTURE_NEW));
        }

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;

            // make sure buttons are initialized before using variables

            if(mCheckButton != null && mShutterButton != null) {

                switch (what) {
                    case CAMERA_READY:
                        recentPicturePath = null;
                        mShutterButton.setImageResource(R.drawable.shutter_button_selector);
                        mShutterButton.setVisibility(View.VISIBLE);
                        break;
                    case PICTURE_TAKEN:
                        mShutterButton.setImageResource(R.drawable.shutter_button_replay_selector);
                        break;
                    case PICTURE_SAVED:
                        Bundle bundle = msg.getData();
                        recentPicturePath = bundle.getString("picturePath");
                        mCheckButton.setVisibility(View.VISIBLE);
                        break;
                    case PICTURE_NEW:
                        recentPicturePath = null;
                        mCheckButton.setVisibility(View.GONE);
                        mShutterButton.setVisibility(View.GONE);
                        break;
                    default:
                        throw new RuntimeException("unknown message " + what);
                }

            }

        }
    };

    /*
     * Definition of the other function of UI-Thread
     */

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment, maybe there is a better way for this
        View V = inflater.inflate(R.layout.cameratab, container, false);

        hideExtraMenu(V);

        // add callback to surfaceview in cameratab layout
        cSurfaceView = (SurfaceView) V.findViewById(R.id.camera_preview);
        cSurfaceView.getHolder().addCallback(this);
        cSurfaceView.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // setup shutter button
        mShutterButton = (ImageView) V.findViewById(R.id.shutter_button);
        mShutterButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                ch.sendTakePicture();
            }
        });

        // setup check button to go to cropping layout
        mCheckButton = (ImageView) V.findViewById(R.id.check_button);
        mCheckButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {
                if(recentPicturePath != null) {
                    mCheckButton.setVisibility(View.GONE);
                    mShutterButton.setVisibility(View.GONE);
                    Intent intent = new Intent(getActivity(), CroppingActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("picturePath", recentPicturePath);
                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            }
        });

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

    @Override
    public void onDestroy() {
        super.onDestroy();

        ch.sendStopPreviewAndReleaseCamera();
        ch.sendShutdown();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // set up CameraThread to touch all camera methods from the background
        mCameraThread = new CameraThread(mHandler,
                cSurfaceView.getHolder(),
                getActivity().getWindowManager().getDefaultDisplay().getRotation());
        mCameraThread.start();
        ch = mCameraThread.getHandler();
        ch.sendOpenCamera();
        ch.sendStartPreview();
        cSurfaceView.setWillNotDraw(false);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        ch.sendStopPreviewAndReleaseCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // some work to be done here for screen rotation etc...
    }

    /*
     * Definition of the camera thread
     */

    private /* static*/ class CameraThread extends Thread {
        private volatile CameraHandler mHandler;
        private android.hardware.Camera mCamera;
        private volatile SurfaceHolder mHolder; // might be updated by UI Thread
        private int mPreviewState;
        private int mScreenOrientation;
        private int mCameraOrientation = 0;
        private volatile UiHandler uiHandler;

        private static final int K_STATE_PREVIEW = 0;
        private static final int K_STATE_FROZEN = 1;
        private static final int K_STATE_BUSY = 2;

        Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
            public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
                String fileName = "tempIMG.jpg";
                String fullPicturePath = getContext().getFilesDir() + "/" + fileName;
                FileOutputStream fileOutStream = null;
                ExifInterface exif;

                try {
                    fileOutStream = getContext().openFileOutput(fileName, Context.MODE_PRIVATE);
                    fileOutStream.write(data);
                    fileOutStream.flush();
                    fileOutStream.close();

                    //save orientation in EXIF-header of the image file

                    exif = new ExifInterface(fullPicturePath);
                    switch(mCameraOrientation) {
                        case 90:
                            exif.setAttribute(ExifInterface.TAG_ORIENTATION,"" + ExifInterface.ORIENTATION_ROTATE_90);
                            break;
                        case 180:
                            exif.setAttribute(ExifInterface.TAG_ORIENTATION,"" + ExifInterface.ORIENTATION_ROTATE_180);
                            break;
                        case 270:
                            exif.setAttribute(ExifInterface.TAG_ORIENTATION,"" + ExifInterface.ORIENTATION_ROTATE_270);
                            break;
                    }
                    exif.saveAttributes();

                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }

                if(fileOutStream != null) {
                    uiHandler.sendPictureSaved(fullPicturePath);
                }
            }
        };

        public CameraThread (UiHandler handler, SurfaceHolder holder, int screen_orientation) {
            uiHandler = handler;
            mHolder = holder;
            mScreenOrientation = screen_orientation;
        }

        public void run() {
            Looper.prepare();

            mHandler = new CameraHandler(this);

            Looper.loop();
        }

        /*
         *  Returns the CameraThreads Handler - can be called from any thread
         */

        public synchronized CameraHandler getHandler() {
            while (mHandler == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    System.out.println("got interrupted!");
                }
            }
            return mHandler;
        }

        /*
         *  Below this we only have methods to be called from CameraThread
         */

        private boolean open_camera(/*int id*/) {
            boolean qOpened = false;

            try {
                stop_preview_and_release_camera();
                mCamera = android.hardware.Camera.open(0);
                qOpened = (mCamera != null);
            } catch (Exception e) {
                Log.e(getString(R.string.app_name), "failed to open Camera");
                e.printStackTrace();
            }

            return qOpened;
        }

        public void set_camera_display_orientation() {
            if (mCamera != null) {
                android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
                android.hardware.Camera.getCameraInfo(0, info);
                int degrees = 0;
                switch (mScreenOrientation) {
                    case Surface.ROTATION_0:
                        degrees = 0;
                        break;
                    case Surface.ROTATION_90:
                        degrees = 90;
                        break;
                    case Surface.ROTATION_180:
                        degrees = 180;
                        break;
                    case Surface.ROTATION_270:
                        degrees = 270;
                        break;
                }

                int result;
                //int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                // do something for phones running an SDK before lollipop
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    result = (info.orientation + degrees) % 360;
                    result = (360 - result) % 360; // compensate the mirror
                } else { // back-facing
                    result = (info.orientation - degrees + 360) % 360;
                }

                //store Orientation of Camera for rotation Pictures later
                mCameraOrientation = result;
                mCamera.setDisplayOrientation(result);
            }

            else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "Camera not ready yet.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void start_preview() {
            if (mCamera != null) {
                List<Camera.Size> localSizes = mCamera.getParameters().getSupportedPreviewSizes();
/*                mSupportedPreviewSizes = localSizes;

                requestLayout();
*/
                try {
                    mCamera.setPreviewDisplay(mHolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Important: Call startPreview() to start updating the preview
                // surface. Preview must be started before you can take a picture.
                mCamera.startPreview();
                mPreviewState = K_STATE_PREVIEW;
                uiHandler.sendCameraReady();
            }
        }

        private void take_picture() {
            if (mCamera != null) {
                switch (mPreviewState) {
                    //case K_STATE_FROZEN:
                    case K_STATE_BUSY:
                        uiHandler.sendPictureNew();
                        start_preview();
                        mPreviewState = K_STATE_PREVIEW;
                        break;
                    default:
                        mCamera.takePicture(null, null, jpegCallback);
                        mPreviewState = K_STATE_BUSY;
                        uiHandler.sendPictureTaken();
                } // switch
            }
            else {
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(getActivity(), "Camera not ready yet.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        private void stop_preview_and_release_camera() {
            if (mCamera != null) {
                // Call stopPreview() to stop updating the preview surface.
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
        }

        private void shutdown() {
            Looper.myLooper().quit();
        }

        private void test() {
            getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(getActivity(), "Test successfull!!", Toast.LENGTH_SHORT).show();
                }
            });
        }

        /*
         * Handles all messages from UI thread and calls CameraThreads functions
         */

        private /*static */ class CameraHandler extends Handler {
            private static final int MSG_OPEN_CAMERA = 0;
            private static final int MSG_START_PREVIEW = 1;
            private static final int MSG_TAKE_PICTURE = 2;
            private static final int MSG_STOP_PREVIEW_AND_RELEASE_CAMERA = 3;
            private static final int MSG_SHUTDOWN = 4;
            private static final int MSG_TEST =5;

            private WeakReference<CameraThread> mWeakCameraThread;

            /*
             * Call from Ui Thread
             */
            public CameraHandler(CameraThread rt) {
                mWeakCameraThread = new WeakReference<CameraThread>(rt);
            }

            /*
             *  Call from Ui Thread
             */
            public void sendOpenCamera() {
                sendMessage(obtainMessage(CameraHandler.MSG_OPEN_CAMERA));
            }

            /*
             *  Call from Ui Thread
             */
            public void sendStartPreview() {
                sendMessage(obtainMessage(CameraHandler.MSG_START_PREVIEW));
            }

            /*
             *  Call from Ui Thread
             */
            public void sendTakePicture() {
                sendMessage(obtainMessage(CameraHandler.MSG_TAKE_PICTURE));
            }

            /*
             *  Call from Ui Thread
             */
            public void sendStopPreviewAndReleaseCamera() {
                sendMessage(obtainMessage(CameraHandler.MSG_STOP_PREVIEW_AND_RELEASE_CAMERA));
            }

            /*
             *  Call from Ui Thread
             */
            public void sendShutdown() {

                sendMessage(obtainMessage(CameraHandler.MSG_SHUTDOWN));
            }

            /*
             *  Call from Ui Thread
             */
            public void sendTest() {

                sendMessage(obtainMessage(CameraHandler.MSG_TEST));
            }

            @Override
            public void handleMessage(Message msg) {
                int what = msg.what;

                CameraThread cameraThread = mWeakCameraThread.get();

                if (cameraThread == null) {
                    Log.w(TAG, "RenderHandler.handleMessage: weak ref is null");
                    return;
                }

                switch (what) {
                    case MSG_OPEN_CAMERA:
                        cameraThread.open_camera();
                        break;
                    case MSG_START_PREVIEW:
                        cameraThread.set_camera_display_orientation();
                        cameraThread.start_preview();
                        break;
                    case MSG_TAKE_PICTURE:
                        cameraThread.take_picture();
                        break;
                    case MSG_STOP_PREVIEW_AND_RELEASE_CAMERA:
                        cameraThread.stop_preview_and_release_camera();
                        break;
                    case MSG_SHUTDOWN:
                        cameraThread.shutdown();
                        break;
                    case MSG_TEST:
                        cameraThread.test();
                        break;
                    default:
                        throw new RuntimeException("unknown message " + what);
                }

            }
        }
    }
}
