package org.ig.observer.pniewinski.adapters;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.LinkedList;
import org.apache.commons.text.StringEscapeUtils;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.activities.HistoryActivity;
import org.ig.observer.pniewinski.model.History;

public class HistoryListAdapter extends ArrayAdapter {

  private final HistoryActivity historyActivity;
  private LinkedList<History> historyList;

  public HistoryListAdapter(HistoryActivity historyActivity, LinkedList<History> list) {
    super(historyActivity, R.layout.listview_row, list);
    this.historyActivity = historyActivity;
    this.historyList = list;
  }

  public View getView(final int position, View view, ViewGroup parent) {
    LayoutInflater inflater = historyActivity.getLayoutInflater();
    final View rowView = inflater.inflate(R.layout.history_listview_row, null, true);
    TextView nameTextField = rowView.findViewById(R.id.name_text_view);
    TextView infoTextField = rowView.findViewById(R.id.info_text_view);

    // Main section
    final History history = historyList.get(position);
    nameTextField.setText(history.getTimestampText() + "   " + history.getUserName());
    infoTextField.setText(getFormattedText(history.getMessage()));
    return rowView;
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