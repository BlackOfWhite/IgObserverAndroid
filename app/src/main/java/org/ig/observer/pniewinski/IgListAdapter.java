package org.ig.observer.pniewinski;

import static org.ig.observer.pniewinski.MainActivity.LOG_TAG;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import org.apache.commons.text.StringEscapeUtils;
import org.ig.observer.pniewinski.image.DownloadImageTask;
import org.ig.observer.pniewinski.image.Utils;
import org.ig.observer.pniewinski.model.User;

public class IgListAdapter extends ArrayAdapter {

  private static final StyleSpan BOLD_STYLE = new StyleSpan(android.graphics.Typeface.BOLD);
  private MainActivity mainActivity;
  private List<User> users;

  public IgListAdapter(MainActivity mainActivity, List<User> list) {
    super(mainActivity, R.layout.listview_row, list);
    this.mainActivity = mainActivity;
    this.users = list;
  }

  private void loadAsyncImage(ImageView imageView, User user) {
    final String imgName = user.getId() + ".png";
    Bitmap b = Utils.loadBitmap(mainActivity, imgName);
    if (b == null) {
      new DownloadImageTask(mainActivity, imageView, imgName).execute(user.getImg_url());
    } else {
      imageView.setImageBitmap(b);
    }
  }

  public View getView(final int position, View view, ViewGroup parent) {
    LayoutInflater inflater = mainActivity.getLayoutInflater();
    final View rowView = inflater.inflate(R.layout.listview_row, null, true);
    TextView nameTextField = (TextView) rowView.findViewById(R.id.name_text_view);
    TextView infoTextField = (TextView) rowView.findViewById(R.id.info_text_view);
    final ImageView imageView = (ImageView) rowView.findViewById(R.id.image_view);
    ImageButton removeButton = (ImageButton) rowView.findViewById(R.id.remove_button);
    removeButton.setOnClickListener(new OnClickListener() {
      public void onClick(View v) {
        if (position < users.size()) {
          mainActivity.removeUserFromList(position);
        }
      }
    });
    TextView biographyTextView = (TextView) rowView.findViewById(R.id.biography_text_view);

    //this code sets the values of the objects to values from the arrays
    final User user = users.get(position);
    nameTextField.setText(user.getName());
    infoTextField.setText(user.getInfo());
    loadAsyncImage(imageView, user);
    // Bold and formatted text
    String formattedText = getFormattedText(user.getBiography());
    final SpannableStringBuilder sb = new SpannableStringBuilder("Biography: " + formattedText);
    sb.setSpan(BOLD_STYLE, 0, "Biography:".length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    biographyTextView.setText(sb);
    return rowView;
  }

  public void refreshItems(List<User> list) {
    users = list;
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      public void run() {
        notifyDataSetChanged();
      }
    });
  }

  private String getFormattedText(final String text) {
    try {
      return StringEscapeUtils.unescapeJava(text);
    } catch (Exception e) {
      Log.w(LOG_TAG, "getFormattedText: Failed to beautify text.", e);
      return text;
    }
  }
}