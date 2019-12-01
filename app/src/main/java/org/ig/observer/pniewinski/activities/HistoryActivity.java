package org.ig.observer.pniewinski.activities;

import static org.ig.observer.pniewinski.io.FileManager.loadHistoryFromFile;

import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.LinkedList;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.adapters.HistoryListAdapter;
import org.ig.observer.pniewinski.model.History;

public class HistoryActivity extends AppCompatActivity {

  private ListView listView;
  private HistoryListAdapter adapter;
  private Context context;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_history);
    this.context = this;
    // Adapter
    LinkedList<History> historyList = new LinkedList<>(loadHistoryFromFile(context));
    adapter = new HistoryListAdapter(this, historyList);
    listView = findViewById(R.id.list_view);
    listView.setAdapter(adapter);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }
}