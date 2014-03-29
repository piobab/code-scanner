package com.nectivo.scanner;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by piobab on 29.03.2014.
 */
public class DisplayUtils {

    public static Point getScreenResolution(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point screenResolution = new Point();
        if(android.os.Build.VERSION.SDK_INT >= 13) {
            display.getSize(screenResolution);
        } else {
            screenResolution.set(display.getWidth(), display.getHeight());
        }

        return screenResolution;
    }

    public static int getScreenOrientation(Context context) {
        Point screenResolution = getScreenResolution(context);
        int width = screenResolution.x;
        int height = screenResolution.y;
        int orientation;
        if (width < height) {
            orientation = Configuration.ORIENTATION_PORTRAIT;
        } else {
            orientation = Configuration.ORIENTATION_LANDSCAPE;
        }
        return orientation;
    }
}
