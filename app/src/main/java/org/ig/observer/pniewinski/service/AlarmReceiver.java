package org.ig.observer.pniewinski.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {

  public static final int REQUEST_CODE = 12345;

  // Triggered by the Alarm periodically (starts the service to run task)
  @Override
  public void onReceive(Context context, Intent pendingIntent) {
    Intent i = new Intent(context, IgService.class);
    context.startService(i);
  }
}