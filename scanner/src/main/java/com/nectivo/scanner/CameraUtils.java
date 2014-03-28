package com.nectivo.scanner;

import android.hardware.Camera;

/**
 * Created by piobab on 28.03.2014.
 */
public class CameraUtils {

    public static Camera getFrontCameraInstance() {
        return getCameraInstance(Camera.CameraInfo.CAMERA_FACING_FRONT);
    }

    public static Camera getBackCameraInstance() {
        return getCameraInstance(Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    /** A safe way to get an instance of the Camera object. */
    private static Camera getCameraInstance(int facing) {
        Camera c = null;
        try {
            int cameraId = getCameraId(facing);
            c = Camera.open(cameraId); // attempt to get a Camera instance
        } catch(Exception e) {
            e.printStackTrace();
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * Gets camera id if available.
     * @return
     */
    private static int getCameraId(int facing) throws CameraNotFoundException {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for(int i=0; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if(facing == ci.facing)
                return i;
        }

        throw new CameraNotFoundException(facing); // No camera found
    }
}
