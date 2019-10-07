package org.ig.observer.pniewinski.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import java.io.InputStream;

public class DownloadImageTask extends AsyncTask {

  private final  ImageView imageView;
  private final String picName;
  private final Context context;

  public DownloadImageTask(Context context, ImageView img, String picName) {
    this.imageView = img;
    this.picName = picName;
    this.context = context;
  }

  @Override
  protected void onPostExecute(Object result) {
    Bitmap post = (Bitmap) result;
    Utils.saveBitmap(context, post, this.picName);
    this.imageView.setImageBitmap(post);
  }

  @Override
  protected Bitmap doInBackground(Object[] objects) {
    String urlToDisplay = (String) objects[0];
    Bitmap mIcon11 = null;
    try {
      InputStream in = new java.net.URL(urlToDisplay).openStream();
      mIcon11 = BitmapFactory.decodeStream(in);
    } catch (Exception e) {
    }
    return mIcon11;
  }
}
