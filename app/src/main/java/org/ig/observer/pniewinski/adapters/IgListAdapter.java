package org.ig.observer.pniewinski.adapters;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;
import static org.ig.observer.pniewinski.activities.MainActivity.MAX_OBSERVED;

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
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.text.StringEscapeUtils;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.activities.MainActivity;
import org.ig.observer.pniewinski.image.DownloadImageTask;
import org.ig.observer.pniewinski.image.Utils;
import org.ig.observer.pniewinski.model.BlockedUser;
import org.ig.observer.pniewinski.model.User;

public class IgListAdapter extends ArrayAdapter {

  private static final String DIALOG_MESSAGE = "Click list item to see details. Click and hold to view notification settings.";
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
    nameTextField.setText(user.getName() + (user.isIs_private() ? getFormattedText(" \\uD83D\\uDD12") : ""));
    if (user instanceof BlockedUser) {
      infoTextField.setText("You are blocked");
    } else {
      infoTextField.setText(user.getInfo());
      loadAsyncImage(imageView, user);
      // Biography & bold and formatted text
      TextView biographyTextView = (TextView) rowView.findViewById(R.id.biography_text_view);
      String formattedText = getFormattedText(user.getBiography());
      final SpannableStringBuilder sb = new SpannableStringBuilder("Biography: " + formattedText);
      sb.setSpan(BOLD_STYLE, 0, "Biography:".length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
      biographyTextView.setText(sb);
    }
    return rowView;
  }

  public CopyOnWriteArrayList<User> leftJoinItems(List<User> list) {
    CopyOnWriteArrayList<User> finaList = new CopyOnWriteArrayList<>();
    for (User user : users) {
      finaList.add(leftJoinItem(user, list));
    }
    refreshItems(finaList);
    return finaList;
  }

  public boolean isListMaxSizeReached() {
    return users.size() >= MAX_OBSERVED;
  }

  private User leftJoinItem(User leftItem, List<User> right) {
    for (User i : right) {
      // It is important to join by name, because id can be 0L for blocked users
      if (i.getName().equals(leftItem.getName())) {
        return i;
      }
    }
    return leftItem;
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
    AlertDialog alertDialog = new Builder(mainActivity, R.style.AlertDialogStyle).setTitle("Hint")
        .setMessage(DIALOG_MESSAGE).setPositiveButton("Got it", null).create();
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