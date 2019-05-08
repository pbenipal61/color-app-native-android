package com.postmaninteractive.colorapp.Utils;

import android.support.design.widget.Snackbar;
import android.view.View;

public class SnackBarHelper {

    /**
     * Creates a {@link Snackbar}
     *
     * @param view View
     * @param message Message on the SnackBar
     * @return SnackBar
     */
    public static Snackbar generate(View view, String message) {
        if (view != null && message != null) {
            return Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        }else{
            throw new NullPointerException("view and message not provided for making a SnackBar");
        }
    }

    /**
     * Creates a SnackBar
     *
     * @param view View
     * @param message Message on the SnackBar
     * @param length  Length of the SnackBar
     * @return SnackBar
     */
    public static Snackbar generate(View view, String message, int length) {
        if ((view != null) && (message != null)) {
            return Snackbar.make(view, message, length);
        }else{
            throw new NullPointerException("view, message and length not provided for making a SnackBar");
        }

    }
}
