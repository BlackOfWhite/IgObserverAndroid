package org.ig.observer.pniewinski;

import android.view.View;
import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {

  public static void snackbar(View view, String message) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();
  }

  public static void networkNotFoundSnackbar(View view) {
    snackbar(view, "No internet connection");
  }

  public static void sessionEndSnackbar(View view) {
    snackbar(view, "Your session has ended. Please log in.");
  }
}
