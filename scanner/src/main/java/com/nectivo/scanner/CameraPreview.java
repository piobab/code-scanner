package com.nectivo.scanner;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.List;

/**
 * Created by piobab on 28.03.2014.
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";

    private Camera mCamera;
    private Handler mAutoFocusHandler;
    private int mLastReportedWidth;
    private int mLastReportedHeight;
    private boolean mPreviewing = true;
    private boolean mAutoFocus = true;
    private Camera.PreviewCallback mPreviewCallback;

    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setCamera(Camera camera, Camera.PreviewCallback previewCallback) {
        mCamera = camera;
        mPreviewCallback = previewCallback;
        mAutoFocusHandler = new Handler();
    }

    public void initCameraPreview() {
        if(mCamera != null) {
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            getHolder().addCallback(this);
            // deprecated setting, but required on Android versions prior to 3.0
            getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            if(mPreviewing) {
                requestLayout();
            } else {
                startCameraPreview();
            }
        }
    }

    public void listSupportedPreviewSizes() {
        if(mCamera != null) {
            List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
            for(Camera.Size size : sizes) {
                Log.d(TAG, "preview size: " + size.width + "x" + size.height);
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        // Preparation of the camera is moved to surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height)
        // because is always called at least once, after surfaceCreated(SurfaceHolder).
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        if(surfaceHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        mLastReportedWidth = width;
        mLastReportedHeight = height;

        // stop preview before making changes
        stopCameraPreview();
        // start preview with new settings
        startCameraPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        stopCameraPreview();
    }

    private void startCameraPreview() {
        if(mCamera != null) {
            try {
                Log.d(TAG, "start camera preview with last reported size: " + mLastReportedWidth + "x" + mLastReportedHeight);
                mPreviewing = true;
                // Camera preview size is calculated in #surfaceChanged.
                setupCameraParameters(mLastReportedWidth, mLastReportedHeight);
                mCamera.setPreviewDisplay(getHolder());
                mCamera.setDisplayOrientation(getDisplayOrientation());
                mCamera.setOneShotPreviewCallback(mPreviewCallback);
                mCamera.startPreview();
                if(mAutoFocus) {
                    mCamera.autoFocus(autoFocusCallback);
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    public void stopCameraPreview() {
        if(mCamera != null) {
            try {
                Log.d(TAG, "stop camera preview");
                mPreviewing = false;
                mCamera.cancelAutoFocus();
                mCamera.setOneShotPreviewCallback(null);
                mCamera.stopPreview();
            } catch(Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }
    }

    private void setupCameraParameters(int width, int height) {
        List<Camera.Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
        Camera.Size optimalSize = getOptimalPreviewSize(sizes, width, height);
        Log.d(TAG, "optimal preview size: " + optimalSize.width + "x" + optimalSize.height);
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(optimalSize.width, optimalSize.height);
        mCamera.setParameters(parameters);
    }

    private int getDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int width, int height) {
        // Default camera resolution is for landscape mode so in portrait mode we have to change
        // width with height
        if(DisplayUtils.getScreenOrientation(getContext()) == Configuration.ORIENTATION_PORTRAIT) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) width / height;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = height;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void setAutoFocus(boolean state) {
        if (mCamera != null && mPreviewing && state != mAutoFocus) {
            mAutoFocus = state;
            if (mAutoFocus) {
                Log.v(TAG, "Starting auto-focus");
                mCamera.autoFocus(autoFocusCallback);
            } else {
                Log.v(TAG, "Cancelling auto-focus");
                mCamera.cancelAutoFocus();
            }
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if(mCamera != null && mPreviewing && mAutoFocus) {
                mCamera.autoFocus(autoFocusCallback);
            }
        }
    };

    // Mimic continuous auto-focusing
    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            mAutoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };
}
