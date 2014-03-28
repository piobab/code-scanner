package com.nectivo.scanner;

/**
 * Created by pbabel on 28.03.2014.
 */
public class CameraNotFoundException extends Exception {

    public CameraNotFoundException(int facing) {
        super("Camera facing " + facing + " not found (check android.hardware.Camera.CameraInfo).");
    }
}
