package org.ig.observer.pniewinski.service;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class IgService extends IntentService {

  public IgService() {
    super("IgService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    // Do the task here
    Log.i(LOG_TAG, "Starting IgService service run");
    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
    Log.i(LOG_TAG, "allPreferences: " + preferences.getAll());
  }
}
