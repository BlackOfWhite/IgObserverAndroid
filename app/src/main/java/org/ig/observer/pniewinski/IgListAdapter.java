package org.ig.observer.pniewinski;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
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
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.text.StringEscapeUtils;
import org.ig.observer.pniewinski.activities.MainActivity;
import org.ig.observer.pniewinski.image.DownloadImageTask;
import org.ig.observer.pniewinski.image.Utils;
import org.ig.observer.pniewinski.model.User;

public class IgListAdapter extends ArrayAdapter {

  private static final Spanned DIALOG_MESSAGE = (Html.fromHtml("&#8226; Click list item to see user details.<br/><br/>"
      + "&#8226; Click and hold to enter notification settings."));
  private static final StyleSpan BOLD_STYLE = new StyleSpan(android.graphics.Typeface.BOLD);
  private final MainActivity mainActivity;
  private CopyOnWriteArrayList<User> users;

  public IgListAdapter(MainActivity mainActivity, CopyOnWriteArrayList<User> list) {
    super(mainActivity, R.layout.listview_row, list);
    this.mainActivity = mainActivity;
    this.users = list;
  }

  private void loadAsyncImage(ImageView imageView, User user) {
    final String imgName = user.getImg_url().replace("/", "_") + ".png";
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

    // Main section
    final User user = users.get(position);
    nameTextField.setText(user.getName());
    infoTextField.setText(user.getInfo());
    loadAsyncImage(imageView, user);
    // Biography & bold and formatted text
    TextView biographyTextView = (TextView) rowView.findViewById(R.id.biography_text_view);
    String formattedText = getFormattedText(user.getBiography());
    final SpannableStringBuilder sb = new SpannableStringBuilder("Biography: " + formattedText);
    sb.setSpan(BOLD_STYLE, 0, "Biography:".length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
    biographyTextView.setText(sb);
    return rowView;
  }

  public void refreshItems(List<User> list) {
    refreshItems(list, false);
  }

  public void refreshItems(final List<User> list, boolean showDialog) {
    users.clear();
    users.addAll(list);
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      public void run() {
        notifyDataSetChanged();
        if (showDialog) {
          showInformationalDialog();
        }
      }
    });
  }

  private void showInformationalDialog() {
    Log.i(LOG_TAG, "showInformationalDialog: show");
    AlertDialog alertDialog = new Builder(mainActivity, R.style.AlertDialogStyle).setTitle("Tip")
        .setMessage(DIALOG_MESSAGE)
        .setIcon(R.drawable.ic_user_not_found).setPositiveButton("OK", null).create();

    alertDialog.show();
  }

  private String getFormattedText(final String text) {
    try {
      return StringEscapeUtils.unescapeJava(text);
    } catch (Exception e) {
      Log.w(LOG_TAG, "getFormattedText: Failed to beautify text.", e);
      return text;
    }
  }

  public List<User> getUserList() {
    return users;
  }
}