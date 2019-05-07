package com.postmaninteractive.colorapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class ResizeHelper {

    private static int[] _vals = null;      // Screen Dimensions

    /**
     * Get screen size dimensions
     *
     * @param context Context from where the function is called
     * @return Array of screen dimensions where value at 0 is height and at 1 is width
     */
    public static int[] getScreenDimensions(Context context) {

        if (_vals == null) {
            _vals = new int[2];
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            _vals[1] = displayMetrics.heightPixels;
            _vals[0] = displayMetrics.widthPixels;
        }

        return _vals;
    }
}
