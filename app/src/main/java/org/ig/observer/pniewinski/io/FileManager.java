package org.ig.observer.pniewinski.io;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.content.Context;
import android.util.Log;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;
import org.ig.observer.pniewinski.model.History;
import org.ig.observer.pniewinski.model.User;

public class FileManager {

  public static final String FILE_NAME_USERS = "ig_observer_storage";
  public static final String FILE_NAME_COOKIE = "ig_observer_storage_auth";
  public static final String FILE_NAME_TIMESTAMP = "ig_observer_storage_timestamp";
  public static final String FILE_NAME_HISTORY = "ig_observer_storage_history";

  public static CopyOnWriteArrayList<User> loadUsersFromFile(Context context) {
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

  public static LinkedList<History> loadHistoryFromFile(Context context) {
    try (
        FileInputStream fis = context.openFileInput(FILE_NAME_HISTORY);
        ObjectInputStream is = new ObjectInputStream(fis)) {
      LinkedList<History> histories = (LinkedList<History>) is.readObject();
//      Log.i(LOG_TAG, "loadedHistoryFromFile: " + histories);
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

  public static Long loadTimestampFromFile(Context context) {
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
