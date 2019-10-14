package org.ig.observer.pniewinski.io;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.content.Context;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import org.ig.observer.pniewinski.model.User;

public class FileManager {

  public static final String FILE_NAME = "ig_observer_storage";

  public static List<User> loadUsersFromFile(Context context) {
    try (
        FileInputStream fis = context.openFileInput(FILE_NAME);
        ObjectInputStream is = new ObjectInputStream(fis)) {
      List<User> users = (ArrayList) is.readObject();
      Log.i(LOG_TAG, "loadedFromFile: " + users);
      return users;
    } catch (IOException | ClassNotFoundException e) {
      Log.w(LOG_TAG, "Failed to load list from file: ", e);
    } catch (Exception e) {
      Log.w(LOG_TAG, "Failed to load list from file. Unexpected exception: ", e);
    }
    return new ArrayList<>();
  }

}
