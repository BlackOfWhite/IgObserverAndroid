package org.ig.observer.pniewinski.service;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_ACCOUNT_STATUS;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_BIOGRAPHY;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_FOLLOWED_BY;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_FOLLOWS;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_HAS_STORIES;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_PICTURE;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_POSTS;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.PREFERENCE_SEPARATOR;
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
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
import android.util.Log;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.activities.MainActivity;
import org.ig.observer.pniewinski.exceptions.UserRemovedError;
import org.ig.observer.pniewinski.model.User;
import org.ig.observer.pniewinski.network.NetworkProcessor;

public class IgService extends IntentService {


  private SharedPreferences preferences;
  private String preferencePattern = "%s" + PREFERENCE_SEPARATOR + "%s"; // username + PREFERENCE_SEPARATOR + key

  public IgService() {
    super("IgService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    // Do the task here
    Log.i(LOG_TAG, "Starting IgService service run");
    preferences = PreferenceManager.getDefaultSharedPreferences(this);
    Log.i(LOG_TAG, "allPreferences: " + preferences.getAll());
    CopyOnWriteArrayList<User> users = loadUsersFromFile(getApplicationContext());
    Log.i(LOG_TAG, "onHandleIntent::userList: " + users);
    if (users != null && !users.isEmpty()) {
      processUsers(users);
    }
  }

  private void processUsers(List<User> userList) {
    NetworkProcessor networkProcessor = new NetworkProcessor();
    CopyOnWriteArrayList<User> newUserList = new CopyOnWriteArrayList<>();
    Map<User, String> userNotificationMessages = new HashMap<>();
    // Get notification settings
    for (User user : userList) {
      try {
        User newUser = networkProcessor.getUser(user.getName());
        // force notification, test purposes
//        newUser.setBiography(user.getBiography() + " test");
        if (!user.equals(newUser)) {
          Log.i(LOG_TAG, "User " + newUser.getName() + " has changed. Comparing with its old version.");
          Log.i(LOG_TAG, "old: " + user + "\nnew: " + newUser);
          newUserList.add(newUser);
          // Check for notifications
          String userNotificationMsg = buildUserNotificationMessage(user, newUser);
          if (!userNotificationMsg.isEmpty()) {
            userNotificationMessages.put(user, userNotificationMsg);
          }
        } else {
          // Skip user, they are the same
          newUserList.add(user);
        }
        // Handle special case where user has removed account, do not keep it in the list
      } catch (UserRemovedError e) {
        Log.w(LOG_TAG, "User probably removed his/her account!", e);
        userNotificationMessages.put(user, "User has probably removed his/her account!");
      } catch (Exception e) {
        Log.w(LOG_TAG, "Exception while fetching data for user:: " + user.getName(), e);
        newUserList.add(user);
        continue;
      }
      try {
        // Some delay to avoid being redirected
        Thread.sleep(5_000 + 1_000 * new Random().nextInt(5));
      } catch (InterruptedException e) {
        Log.e(LOG_TAG, "Interrupted IgService!", e);
      }
    }
    if (userList.size() != newUserList.size()) {
      Log.i(LOG_TAG,
          "processUsers - lists have different sizes :: " + userList.size() + " >> " + userList + "\n" + newUserList.size() + " >> "
              + newUserList);
      return;
    }
    if (userList.equals(newUserList)) {
      Log.i(LOG_TAG, "processUsers - both lists are equal");
      return;
    }

    // Update list used in main activity, send broadcast
    Intent intent = new Intent("ig_broadcast_intent");
    intent.putExtra("user_list", newUserList);
    sendBroadcast(intent);

    // Send notifications
    final NotificationManager mNotificationManager = getNotificationManager();
    Log.i(LOG_TAG, "Notification massages: " + userNotificationMessages);
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
            .setStyle(new BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT);

    PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
        new Intent(this, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
    mBuilder.setContentIntent(contentIntent);
    mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});
    mBuilder.setSound(Settings.System.DEFAULT_NOTIFICATION_URI);
//    mBuilder.setAutoCancel(true);
    return mBuilder;
  }

  private String buildUserNotificationMessage(User oldUser, User newUser) {
    StringBuilder sb = new StringBuilder();
    String userName = oldUser.getName();
    if (!oldUser.getBiography().equals(newUser.getBiography()) && isNotificationEnabled(userName, KEY_NOTIFICATION_BIOGRAPHY)) {
      sb.append("The biography has changed. ");
    }
    if (!oldUser.getImg_url().equals(newUser.getImg_url()) && isNotificationEnabled(userName, KEY_NOTIFICATION_PICTURE)) {
      sb.append("There is a new profile picture. ");
    }
    if ((long) oldUser.getFollows() != newUser.getFollows() && isNotificationEnabled(userName, KEY_NOTIFICATION_FOLLOWS)) {
      Long old = oldUser.getFollows();
      Long newV = newUser.getFollows();
      long diff = newV - old;
      sb.append("User is now following " + Math.abs(diff) + " accounts " + (diff > 0 ? "more." : "less.") + " ");
    }
    if ((long) oldUser.getFollowed_by() != newUser.getFollowed_by() && isNotificationEnabled(userName, KEY_NOTIFICATION_FOLLOWED_BY)) {
      Long old = oldUser.getFollowed_by();
      Long newV = newUser.getFollowed_by();
      long diff = newV - old;
      sb.append("User has just " + (diff > 0 ? "gained " : "lost ") + Math.abs(diff) + " followers. ");
    }
    if ((long) oldUser.getPosts() != newUser.getPosts() && isNotificationEnabled(userName, KEY_NOTIFICATION_POSTS)) {
      Long old = oldUser.getPosts();
      Long newV = newUser.getPosts();
      long diff = newV - old;
      sb.append("User has just " + (diff > 0 ? "added " : "removed ") + Math.abs(diff) + " post(s). ");
    }
    if (oldUser.getIs_private() != newUser.getIs_private() && isNotificationEnabled(userName, KEY_NOTIFICATION_ACCOUNT_STATUS)) {
      sb.append("Account status has just changed to " + (newUser.getIs_private() ? "private" : "public") + "! ");
    }
    if (oldUser.isHas_stories() != newUser.isHas_stories() && isNotificationEnabled(userName, KEY_NOTIFICATION_HAS_STORIES)) {
      sb.append(newUser.isHas_stories() ? "Account has at least one story. " : "Account doesn't have stories anymore. ");
    }
    // Remove last space
    String message = sb.toString();
    if (!message.isEmpty()) {
      message = message.substring(0, message.length() - 1);
    }
    Log.i(LOG_TAG, "buildUserNotificationMessage: " + message);
    return message;
  }

  private boolean isNotificationEnabled(String userName, String prefKey) {
    if (preferences == null) {
      return false;
    }
    return preferences
        .getBoolean(String.format(preferencePattern, userName, prefKey), true);
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
