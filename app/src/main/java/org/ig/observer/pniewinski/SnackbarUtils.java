package org.ig.observer.pniewinski;

import android.support.design.widget.Snackbar;
import android.view.View;

public class SnackbarUtils {

  public static void snackbar(View view, String message) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();
  }

  public static void networkNotFoundSnackbar(View view) {
    snackbar(view, "No internet connection");
  }
}
