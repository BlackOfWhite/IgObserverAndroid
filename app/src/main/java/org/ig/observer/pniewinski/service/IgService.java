package org.ig.observer.pniewinski.service;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;
import static org.ig.observer.pniewinski.io.FileManager.loadUsersFromFile;

import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.activities.MainActivity;
import org.ig.observer.pniewinski.model.User;
import org.ig.observer.pniewinski.network.Processor;

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
    ArrayList<User> users = (ArrayList<User>) loadUsersFromFile(getApplicationContext());
    Log.i(LOG_TAG, "onHandleIntent::userList: " + users);
    if (users != null && !users.isEmpty()) {
      processUsers(users);
    }
  }

  private void processUsers(ArrayList<User> userList) {
    Log.i(LOG_TAG, "processUsers::" + userList);
    Processor processor = new Processor();
    ArrayList<User> newUserList = new ArrayList<>();
    Map<User, String> userNotificationMessages = new HashMap<>();
    // Get notification settings

    for (User user : userList) {
      try {
        User newUser = processor.getUser(user.getName());
        if (!user.equals(newUser)) {
          Log.i(LOG_TAG, "User " + newUser.getName() + " has changed. Comparing with its old version.");
          newUserList.add(newUser);
          // Check for notifications
          String userNotificationMsg = buildUserNotificationMessage(user, newUser);
          if (!userNotificationMsg.isEmpty()) {
            userNotificationMessages.put(user, userNotificationMsg);
          }
        } else {
          // Skip user, something strange happened
          newUserList.add(user);
        }
      } catch (Exception e) {
        newUserList.add(user);
        continue;
      }
    }
    if (userList.size() != newUserList.size()) {
      Log.i(LOG_TAG, "processUsers - lists have different size");
      return;
    }
    if (userList.equals(newUserList)) {
      Log.i(LOG_TAG, "processUsers - both lists are equal");
      return;
    }

    // Update list used in main activity

    // Send notifications
    final NotificationManager mNotificationManager = getNotificationManager();
    for (Entry<User, String> entry : userNotificationMessages.entrySet()) {
      NotificationCompat.Builder notificationBuilder = buildNotification(entry.getKey().getName(), entry.getValue());
      mNotificationManager.notify(entry.getKey().getId().intValue(), notificationBuilder.build());
    }
  }

  private NotificationCompat.Builder buildNotification(String userName, String message) {
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(this)
            .setSmallIcon(R.drawable.ic_magnifying_glass)
            .setContentTitle("Changes for user " + userName + " detected!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(contentIntent);
    mBuilder.setAutoCancel(true);
    return mBuilder;
  }

  private String buildUserNotificationMessage(User oldUser, User newUser) {
    StringBuilder sb = new StringBuilder();
    if (!oldUser.getBiography().equals(newUser.getBiography())) {
      sb.append("The biography has changed. ");
    } else if (!oldUser.getImg_url().equals(newUser.getImg_url())) {
      sb.append("There is a new profile picture. ");
    } else if (oldUser.getFollows() != newUser.getFollows()) {
      Long old = oldUser.getFollows();
      Long newV = newUser.getFollows();
      long diff = newV - old;
      sb.append("User is now following " + Math.abs(diff) + " accounts " + (diff > 0 ? "more." : "less.") + " ");
    } else if (oldUser.getFollowed_by() != newUser.getFollowed_by()) {
      Long old = oldUser.getFollowed_by();
      Long newV = newUser.getFollowed_by();
      long diff = newV - old;
      sb.append("User has just " + (diff > 0 ? " gained " : "lost ") + Math.abs(diff) + " followers. ");
    }
    // Remove last space
    String s = sb.toString().substring(0, sb.toString().length() - 1);
    Log.i(LOG_TAG, "buildUserNotificationMessage: " + s);
    return s;
  }

  private NotificationManager getNotificationManager() {
    NotificationManager notification_manager = (NotificationManager) this
        .getSystemService(Context.NOTIFICATION_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      String chanel_id = "3000";
      CharSequence name = "IG Observer Channel";
      String description = "Channel for IG Observer notifications";
      int importance = NotificationManager.IMPORTANCE_LOW;
      NotificationChannel mChannel = new NotificationChannel(chanel_id, name, importance);
      mChannel.setDescription(description);
      mChannel.enableLights(true);
      mChannel.setLightColor(Color.BLUE);
      notification_manager.createNotificationChannel(mChannel);
    }
    return notification_manager;
  }
}
