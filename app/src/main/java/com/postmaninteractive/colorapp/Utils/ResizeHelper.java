package com.postmaninteractive.colorapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

public class ResizeHelper {

    private static int[] dimensions = null;      // Screen Dimensions

    /**
     * Get screen size dimensions
     *
     * @param context Context from where the function is called
     * @return Array of screen dimensions where value at 0 is height and at 1 is width
     */
    public static int[] getScreenDimensions(Context context) {
        if (dimensions == null) {
            dimensions = new int[2];
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dimensions[1] = displayMetrics.heightPixels;
            dimensions[0] = displayMetrics.widthPixels;
        }

        return dimensions;
    }
}
