package com.nectivo.scanner;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by piobab on 28.03.2014.
 */
public abstract class ScannerView extends FrameLayout implements Camera.PreviewCallback  {
    private Camera mCamera;
    private CameraPreview mPreview;

    public ScannerView(Context context) {
        super(context);
        setupLayout();
    }

    public ScannerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setupLayout();
    }

    public void setupLayout() {
        mPreview = new CameraPreview(getContext());
        addView(mPreview);
    }

    public void startFrontCamera() {
        startCamera(CameraUtils.getFrontCameraInstance());
    }

    public void startBackCamera() {
        startCamera(CameraUtils.getBackCameraInstance());
    }

    private void startCamera(Camera camera) {
        mCamera = camera;
        if(mCamera != null) {
            mPreview.setCamera(mCamera, this);
            mPreview.initCameraPreview();
        }
    }

    public void stopCamera() {
        if(mCamera != null) {
            mPreview.stopCameraPreview();
            mPreview.setCamera(null, null);
            mCamera.release();
            mCamera = null;
        }
    }

    public void setAutoFocus(boolean state) {
        if(mPreview != null) {
            mPreview.setAutoFocus(state);
        }
    }
}
