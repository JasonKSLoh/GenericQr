package com.lohjason.genericqr.ui.scanner;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.common.images.Size;
import com.lohjason.genericqr.util.Logg;



/**
 * CameraSourcePreview
 * Created by jason on 26/6/18.
 */
public class CameraSourcePreview extends RelativeLayout {

    private Context                                                viewContext;
    private SurfaceView                                            surfaceView;
    private boolean                                                isStartRequested;
    private boolean                                                isSurfaceAvailable;
    private CameraSource cameraSource;
    private final String LOG_TAG = "+_CamSrcPrv";



    public CameraSourcePreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        viewContext = context;
        isStartRequested = false;
        isSurfaceAvailable = false;

        surfaceView = new SurfaceView(context);
        surfaceView.getHolder().addCallback(new SurfaceCallback());
        addView(surfaceView);
    }

    public void start(CameraSource cameraSource) {
        if (cameraSource == null) {
            stop();
        }

        this.cameraSource = cameraSource;

        if (this.cameraSource != null) {
            isStartRequested = true;
            startIfReady();
        }
    }

    public void stop() {
        if (cameraSource != null) {
            cameraSource.stop();
        }
    }

    public void release() {
        if (cameraSource != null) {
            cameraSource.release();
            cameraSource = null;
        }
    }

    @SuppressLint("MissingPermission")
    private void startIfReady() {
        if (isStartRequested && isSurfaceAvailable) {
            try{
                cameraSource.start(surfaceView.getHolder());
            } catch (Exception e){
                Logg.d(LOG_TAG, e.getMessage(), e);
            }
            isStartRequested = false;
        }
    }

    private class SurfaceCallback implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder surface) {
            isSurfaceAvailable = true;
            try {
                startIfReady();
            } catch (SecurityException se) {
                Logg.d(LOG_TAG, "Do not have permission to start the camera", se);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surface) {
            isSurfaceAvailable = false;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            //No need to call back if surface changed
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int width = 320;
        int height = 240;
        if (cameraSource != null) {
            Size size = cameraSource.getPreviewSize();
            if (size != null) {
                width = size.getWidth();
                height = size.getHeight();
            }
        }

        // Swap width and height sizes when in portrait, since it will be rotated 90 degrees
        if (isPortraitMode()) {
            int tmp = width;
            //noinspection SuspiciousNameCombination
            width = height;
            height = tmp;
        }

        final int layoutWidth = right - left;
        final int layoutHeight = bottom - top;

        // Computes height and width for potentially doing fit width.
        int childWidth = layoutWidth;
        int childHeight = (int)(((float) layoutWidth / (float) width) * height);

        // Fits Height & Width based to fill entire screen
        if (childHeight < layoutHeight) {
            childHeight = layoutHeight;
            childWidth = (int)(((float) layoutHeight / (float) height) * width);
        }

        int diffHeight = layoutHeight - childHeight;
        int diffWidth = layoutWidth - childWidth;

        for (int i = 0; i < getChildCount(); ++i) {
            View childView = getChildAt(i);
            if(childView instanceof SurfaceView){
                childView.layout(diffWidth/2, diffHeight / 2, diffWidth/2 + childWidth, childHeight + diffHeight / 2);
            } else {
                childView.layout(0, 0, childWidth,  childHeight);
            }
        }

        try {
            startIfReady();
        } catch (SecurityException se) {
            Logg.e(LOG_TAG, "Do not have permission to start the camera", se);
        }
    }

    private boolean isPortraitMode() {
        int orientation = viewContext.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return false;
        }
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            return true;
        }

        Logg.d(LOG_TAG,"isPortraitMode returning false by default");
        return false;
    }
}