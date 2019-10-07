package org.ig.observer.pniewinski.service;

import static org.ig.observer.pniewinski.MainActivity.LOG_TAG;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class IgService extends IntentService {

  public IgService() {
    super("IgService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    // Do the task here
    Log.i(LOG_TAG, "Starting IgService service run");
  }
}
