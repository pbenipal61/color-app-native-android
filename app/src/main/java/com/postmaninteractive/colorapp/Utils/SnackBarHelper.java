package com.postmaninteractive.colorapp.Utils;

import android.support.design.widget.Snackbar;
import android.view.View;

public class SnackBarHelper {

    /**
     * Creates a SnackBar
     *
     * @param view    View
     * @param message Message on the SnackBar
     * @return SnackBar
     */
    public static Snackbar generate(View view, String message) {
        return Snackbar.make(view, message, Snackbar.LENGTH_LONG);

    }

    /**
     * Creates a SnackBar
     *
     * @param view    View
     * @param message Message on the SnackBar
     * @param length  Length of the SnackBar
     * @return SnackBar
     */
    public static Snackbar generate(View view, String message, int length) {
        return Snackbar.make(view, message, length);

    }
}
