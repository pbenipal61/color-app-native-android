package com.postmaninteractive.colorapp.Utils;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.view.View;

public class SnackbarHelper {

    public static Snackbar generate(View view, String message){
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
       return snackbar;


    }

    public static Snackbar generate(View view, String message, int length){
        Snackbar snackbar = Snackbar.make(view, message, length);
        return snackbar;


    }
}
