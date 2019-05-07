package com.postmaninteractive.colorapp.Utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;

public class ResizeHelper {

    public static int[] getScreenDimensions(Context context){

        int[] vals = new int[2];
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        vals[1] = displayMetrics.heightPixels;
        vals[0] = displayMetrics.widthPixels;


        return vals;
    }
}
