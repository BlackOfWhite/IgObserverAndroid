package org.ig.observer.pniewinski;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;
import org.ig.observer.pniewinski.model.User;

public class IgListAdapter extends ArrayAdapter {

  private final MainActivity mainActivity;
  private List<User> users;

  public IgListAdapter(MainActivity mainActivity, List<User> list) {
    super(mainActivity, R.layout.listview_row, list);
    this.mainActivity = mainActivity;
    this.users = list;
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
    //this code sets the values of the objects to values from the arrays
    nameTextField.setText(users.get(position).getName());
    infoTextField.setText(users.get(position).getDetails());
    imageView.setImageResource(users.get(position).getImage());
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
}