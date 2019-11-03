package org.ig.observer.pniewinski.io;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.content.Context;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ig.observer.pniewinski.model.History;
import org.ig.observer.pniewinski.model.User;

public class FileManager {

  public static final String FILE_NAME_USERS = "ig_observer_storage";
  public static final String FILE_NAME_COOKIE = "ig_observer_storage_auth";
  public static final String FILE_NAME_TIMESTAMP = "ig_observer_storage_timestamp";
  public static final String FILE_NAME_HISTORY = "ig_observer_storage_history";
  private static ExecutorService fileIOExecutor = Executors.newSingleThreadExecutor();
  private static Object usersLock = new Object();
  private static Object historyLock = new Object();
  private static Object timestampLock = new Object();

  public static CopyOnWriteArrayList<User> loadUsersFromFile(Context context) {
    synchronized (usersLock) {
      try (
          FileInputStream fis = context.openFileInput(FILE_NAME_USERS);
          ObjectInputStream is = new ObjectInputStream(fis)) {
        CopyOnWriteArrayList<User> users = (CopyOnWriteArrayList) is.readObject();
        Log.i(LOG_TAG, "loadedFromFile: " + users);
        return users;
      } catch (FileNotFoundException e) {
        Log.w(LOG_TAG, "Failed to find file: ", e);
      } catch (IOException | ClassNotFoundException e) {
        Log.w(LOG_TAG, "Failed to load list from file: ", e);
      } catch (Exception e) {
        Log.w(LOG_TAG, "Failed to load list from file. Unexpected exception: ", e);
      }
      return new CopyOnWriteArrayList<>();
    }
  }

  public static void saveUsersToFile(CopyOnWriteArrayList<User> list, Context context) {
    synchronized (usersLock) {
      Log.i(LOG_TAG, "saveToFile: " + list);
      fileIOExecutor.submit(new Runnable() {
        @Override
        public void run() {
          try (
              FileOutputStream fos = context.openFileOutput(FILE_NAME_USERS, Context.MODE_PRIVATE);
              ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(list);
          } catch (IOException e) {
            Log.w(LOG_TAG, "Failed to save list to file: ", e);
          }
        }
      });
    }
  }

  public static LinkedList<History> loadHistoryFromFile(Context context) {
    synchronized (historyLock) {
      try (
          FileInputStream fis = context.openFileInput(FILE_NAME_HISTORY);
          ObjectInputStream is = new ObjectInputStream(fis)) {
        LinkedList<History> histories = (LinkedList<History>) is.readObject();
//        Log.i(LOG_TAG, "loadedHistoryFromFile: " + histories);
        Collections.sort(histories);
        return histories;
      } catch (FileNotFoundException e) {
        Log.w(LOG_TAG, "Failed to find file: ", e);
      } catch (IOException | ClassNotFoundException e) {
        Log.w(LOG_TAG, "Failed to load list from file: ", e);
      } catch (Exception e) {
        Log.w(LOG_TAG, "Failed to load list from file. Unexpected exception: ", e);
      }
      return new LinkedList<>();
    }
  }


  public static void saveHistoryToFile(LinkedList<History> list, Context context) {
    synchronized (historyLock) {
      Log.i(LOG_TAG, "saveHistoryToFile: " + list);
      new Thread(new Runnable() {
        @Override
        public void run() {
          try (
              FileOutputStream fos = context.openFileOutput(FILE_NAME_HISTORY, Context.MODE_PRIVATE);
              ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(list);
          } catch (IOException e) {
            Log.w(LOG_TAG, "Failed to save list to file: ", e);
          }
        }
      }).start();
    }
  }

  public static String loadCookieFromFile(Context context) {
    try (
        FileInputStream fis = context.openFileInput(FILE_NAME_COOKIE);
        ObjectInputStream is = new ObjectInputStream(fis)) {
      String cookie = (String) is.readObject();
      Log.i(LOG_TAG, "loadedCookieFromFile: " + cookie);
      return cookie;
    } catch (FileNotFoundException e) {
      Log.w(LOG_TAG, "Failed to find file: ", e);
    } catch (IOException | ClassNotFoundException e) {
      Log.w(LOG_TAG, "Failed to load list from file: ", e);
    } catch (Exception e) {
      Log.w(LOG_TAG, "Failed to load list from file. Unexpected exception: ", e);
    }
    return null;
  }

  public static void saveCookieToFile(String cookie, Context context) {
    Log.i(LOG_TAG, "saveToCookieFile: " + cookie);
    fileIOExecutor.submit(new Runnable() {
      @Override
      public void run() {
        try (
            FileOutputStream fos = context.openFileOutput(FILE_NAME_COOKIE, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos)) {
          os.writeObject(cookie);
        } catch (IOException e) {
          Log.w(LOG_TAG, "Failed to save cookie to file: ", e);
        }
      }
    });
  }

  public static Long loadTimestampFromFile(Context context) {
    synchronized (timestampLock) {
      try (
          FileInputStream fis = context.openFileInput(FILE_NAME_TIMESTAMP);
          ObjectInputStream is = new ObjectInputStream(fis)) {
        Long timestamp = (Long) is.readObject();
        Log.i(LOG_TAG, "loadedTimestampFromFile: " + timestamp);
        return timestamp;
      } catch (FileNotFoundException e) {
        Log.w(LOG_TAG, "Failed to find file: ", e);
      } catch (IOException | ClassNotFoundException e) {
        Log.w(LOG_TAG, "Failed to load list from file: ", e);
      } catch (Exception e) {
        Log.w(LOG_TAG, "Failed to load list from file. Unexpected exception: ", e);
      }
      return null;
    }
  }

  public static void saveTimestampToFile(Long timestamp, Context context) {
    synchronized (timestampLock) {
      Log.i(LOG_TAG, "saveTimestampToFile: " + timestamp);
      new Thread(new Runnable() {
        @Override
        public void run() {
          try (
              FileOutputStream fos = context.openFileOutput(FILE_NAME_TIMESTAMP, Context.MODE_PRIVATE);
              ObjectOutputStream os = new ObjectOutputStream(fos)) {
            os.writeObject(timestamp);
          } catch (IOException e) {
            Log.w(LOG_TAG, "Failed to save timestamp to file: ", e);
          }
        }
      }).start();
    }
  }
}
