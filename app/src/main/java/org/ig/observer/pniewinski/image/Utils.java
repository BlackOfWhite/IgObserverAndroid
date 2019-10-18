package org.ig.observer.pniewinski.image;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class Utils {

  public static void saveBitmap(Context context, Bitmap b, String picName) {
    try {
      FileOutputStream fos = context.openFileOutput(picName, Context.MODE_PRIVATE);
      if (b != null) {
        b.compress(Bitmap.CompressFormat.PNG, 100, fos);
      } else {
        Log.w(LOG_TAG, "Bitmap for picture: " + picName + " was null!");
      }
    } catch (FileNotFoundException e) {
      Log.w(LOG_TAG, "File " + picName + " not found.", e);
    }
  }

  public static Bitmap loadBitmap(Context context, String picName) {
    try {
      FileInputStream fis = context.openFileInput(picName);
      return BitmapFactory.decodeStream(fis);
    } catch (FileNotFoundException e) {
      Log.w(LOG_TAG, "File " + picName + " not found.", e);
      return null;
    }
  }

}
