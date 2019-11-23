package org.ig.observer.pniewinski.service;

import static org.ig.observer.pniewinski.activities.MainActivity.IG_BROADCAST_LIST_UPDATE;
import static org.ig.observer.pniewinski.activities.MainActivity.IG_BROADCAST_SESSION_END;
import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;
import static org.ig.observer.pniewinski.activities.MainActivity.SERVICE_INTERVAL;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_ACCOUNT_STATUS;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_BIOGRAPHY;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_FOLLOWED_BY;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_FOLLOWS;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_HAS_STORIES;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_PICTURE;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.KEY_NOTIFICATION_POSTS;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.PREFERENCE_SEPARATOR;
import static org.ig.observer.pniewinski.io.FileManager.loadCookieFromFile;
import static org.ig.observer.pniewinski.io.FileManager.loadHistoryFromFile;
import static org.ig.observer.pniewinski.io.FileManager.loadTimestampFromFile;
import static org.ig.observer.pniewinski.io.FileManager.loadUsersFromFile;
import static org.ig.observer.pniewinski.io.FileManager.saveHistoryToFile;
import static org.ig.observer.pniewinski.io.FileManager.saveTimestampToFile;

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
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationCompat.BigTextStyle;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.activities.MainActivity;
import org.ig.observer.pniewinski.exceptions.ConnectionError;
import org.ig.observer.pniewinski.exceptions.UserRemovedError;
import org.ig.observer.pniewinski.model.BlockedUser;
import org.ig.observer.pniewinski.model.History;
import org.ig.observer.pniewinski.model.User;
import org.ig.observer.pniewinski.network.NetworkProcessor;

public class IgService extends IntentService {

  private static final int QUEUE_SIZE = 100;
  private static final NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
  private static ReentrantLock lock = new ReentrantLock();
  private final String ARROW_RIGHT = " \\u27a1\\ufe0f ";
  private SharedPreferences preferences;
  private String preferencePattern = "%s" + PREFERENCE_SEPARATOR + "%s"; // username + PREFERENCE_SEPARATOR + key
  private LinkedList<History> historiesTmp = new LinkedList<>();

  public IgService() {
    super("IgService");
    Log.i(LOG_TAG, "IgService constructor");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.i(LOG_TAG, "onHandleIntent: Starting IgService service run");
    if (lock.tryLock()) {
      try {
        // Check last service run
        final long newTimestamp = System.currentTimeMillis();
        final String cookie = loadCookieFromFile(this);

        // Handle user logged out
        if (cookie == null) {
          Log.i(LOG_TAG, "Tried to run service, but no cookie was found.");
          return;
        }

        final Long lastTimestamp = loadTimestampFromFile(this);
        if (lastTimestamp != null && newTimestamp - lastTimestamp <= SERVICE_INTERVAL) {
          Log.i(LOG_TAG, "Tried to run service before interval has passed.");
          return;
        } else {
          saveTimestampToFile(newTimestamp, getApplicationContext());
        }
        Log.i(LOG_TAG, "Starting IgService run. Last timestamp was: " + lastTimestamp + "\n, now is: " + newTimestamp);
        // Logic
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Log.i(LOG_TAG, "getCookie: " + cookie);
        Log.i(LOG_TAG, "allPreferences: " + preferences.getAll());
        CopyOnWriteArrayList<User> users = loadUsersFromFile(getApplicationContext());
        Log.i(LOG_TAG, "onHandleIntent::userList: " + users);
        if (users != null && !users.isEmpty()) {
          processUsers(users, cookie);
        }
      } finally {
        lock.unlock();
      }
    } else {
      Log.i(LOG_TAG, "Lock not acquired.");
      return;
    }
  }

  private void signOut() {
    // Send broadcast
    Intent intent = new Intent(IG_BROADCAST_SESSION_END);
    sendBroadcast(intent);
  }

  private void processUsers(List<User> userList, final String cookie) {
    NetworkProcessor networkProcessor = new NetworkProcessor();
    CopyOnWriteArrayList<User> newUserList = new CopyOnWriteArrayList<>();
    Map<User, String> userNotificationMessages = new HashMap<>();
    historiesTmp.clear();
    // Get notification settings
    for (User user : userList) {
      try {
        User newUser = networkProcessor.getUser(user.getName(), cookie);
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
        if (e instanceof ConnectionError) {
          if (((ConnectionError) e).getHttpCode() == 302) {
            // Clear notifications
            userNotificationMessages.clear();
            userNotificationMessages.put(new User(1L, "Your session has just ended", ""), "Please log in.");
            sendNotifications(userNotificationMessages);
            signOut();
            break;
          }
        }
        Log.w(LOG_TAG, "Exception while fetching data for user:: " + user.getName(), e);
        newUserList.add(user);
        continue;
      }
      try {
        // Some delay to avoid being treated as DDOS
        Thread.sleep(300L);
      } catch (InterruptedException e) {
        Log.e(LOG_TAG, "Interrupted IgService!", e);
      }
    }

    // Synchronization complete
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
    Intent intent = new Intent(IG_BROADCAST_LIST_UPDATE);
    intent.putExtra("user_list", newUserList);
    sendBroadcast(intent);

    // Update history list
    if (historiesTmp.size() > 0) {
      LinkedList<History> histories = loadHistoryFromFile(this);
      for (History history : historiesTmp) {
        histories.addFirst(history);
      }
      if (histories.size() > QUEUE_SIZE) {
        histories = new LinkedList<>(histories.subList(0, QUEUE_SIZE));
      }
      saveHistoryToFile(histories, getApplicationContext());
    }

    // Send notifications
    sendNotifications(userNotificationMessages);
  }

  private void sendNotifications(Map<User, String> userNotificationMessages) {
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
            .setContentTitle(userName)
            .setContentText(message)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setStyle(new BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH);

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
    long timestamp = System.currentTimeMillis();

    // Handle blocked users
    if (oldUser instanceof BlockedUser) {
      String message = "You have been un-blocked. ";
      historiesTmp.addFirst(new History(oldUser.getName(), timestamp, message));
      return message;
    } else if (newUser instanceof BlockedUser) {
      String message = "You have been blocked. ";
      historiesTmp.addFirst(new History(oldUser.getName(), timestamp, message));
      return message;
    }

    // Handle other cases
    if (!oldUser.getBiography().equals(newUser.getBiography()) && isNotificationEnabled(userName, KEY_NOTIFICATION_BIOGRAPHY)) {
      String message = "The biography has changed. ";
      sb.append(message);
      historiesTmp.addFirst(new History(oldUser.getName(), timestamp,
          message + "\n" + oldUser.getBiography() + "\n" + ARROW_RIGHT + "\n" + newUser.getBiography()));
    }
    if (!oldUser.getImg_url().equals(newUser.getImg_url()) && isNotificationEnabled(userName, KEY_NOTIFICATION_PICTURE)) {
      String message = "There is a new profile picture. ";
      sb.append(message);
      historiesTmp.addFirst(new History(oldUser.getName(), timestamp, message));
    }
    if ((long) oldUser.getFollows() != newUser.getFollows() && isNotificationEnabled(userName, KEY_NOTIFICATION_FOLLOWS)) {
      Long old = oldUser.getFollows();
      Long newV = newUser.getFollows();
      long diff = newV - old;
      String message = "User is now following " + Math.abs(diff) + " accounts " + (diff > 0 ? "more." : "less.") + " ";
      sb.append(message);
      historiesTmp.addFirst(
          new History(oldUser.getName(), timestamp,
              message + "\n" + numberFormat.format(old) + ARROW_RIGHT + numberFormat.format(newV)));
    }
    if ((long) oldUser.getFollowed_by() != newUser.getFollowed_by() && isNotificationEnabled(userName, KEY_NOTIFICATION_FOLLOWED_BY)) {
      Long old = oldUser.getFollowed_by();
      Long newV = newUser.getFollowed_by();
      long diff = newV - old;
      String message = "User has just " + (diff > 0 ? "gained " : "lost ") + Math.abs(diff) + " followers. ";
      sb.append(message);
      historiesTmp.addFirst(
          new History(oldUser.getName(), timestamp,
              message + "\n" + numberFormat.format(old) + ARROW_RIGHT + numberFormat.format(newV)));
    }
    if ((long) oldUser.getPosts() != newUser.getPosts() && isNotificationEnabled(userName, KEY_NOTIFICATION_POSTS)) {
      Long old = oldUser.getPosts();
      Long newV = newUser.getPosts();
      long diff = newV - old;
      String message = "User has just " + (diff > 0 ? "added " : "removed ") + Math.abs(diff) + " post(s). ";
      sb.append(message);
      historiesTmp.addFirst(
          new History(oldUser.getName(), timestamp,
              message + "\n" + numberFormat.format(old) + ARROW_RIGHT + numberFormat.format(newV)));
    }
    if (oldUser.getIs_private() != newUser.getIs_private() && isNotificationEnabled(userName, KEY_NOTIFICATION_ACCOUNT_STATUS)) {
      String message = "Account status has just changed to " + (newUser.getIs_private() ? "private" : "public") + "! ";
      sb.append(message);
      historiesTmp.addFirst(new History(oldUser.getName(), timestamp, message));
    }
    if (oldUser.isHas_stories() != newUser.isHas_stories() && isNotificationEnabled(userName, KEY_NOTIFICATION_HAS_STORIES)) {
      String message = newUser.isHas_stories() ? "Account has at least one story. " : "Account doesn't have stories anymore. ";
      sb.append(message);
      historiesTmp.addFirst(new History(oldUser.getName(), timestamp, message));
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
