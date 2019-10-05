package org.ig.observer.pniewinski;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ig.observer.pniewinski.exceptions.PrivateOrNoPostsException;
import org.ig.observer.pniewinski.exceptions.UserNotFoundException;
import org.ig.observer.pniewinski.model.User;
import org.ig.observer.pniewinski.network.Processor;

public class MainActivity extends AppCompatActivity {

  public static final String LOG_TAG = "IG_TAG";
  private static final String FILE_NAME = "ig_observer_storage";
  private static final int MAX_OBSERVED = 10;
  private static List<User> users = new ArrayList<>();
  private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
  private ExecutorService fileIOExecutor = Executors.newSingleThreadExecutor();
  private ListView listView;
  private IgListAdapter adapter;
  private Context context;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.context = this;
    this.users = loadFromFile();
    adapter = new IgListAdapter(this, users);
    listView = (ListView) findViewById(R.id.list_view);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
//        Intent intent = new Intent(MainActivity.this, DetailActivity.class);
//        String message = nameArray[position];
//        intent.putExtra("animal", message);
//        startActivity(intent);
      }
    });
    final FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fab_add_new);
    fabButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (users.size() >= MAX_OBSERVED) {
          snackbar(fabButton, "You have reached a maximum number of users observed: " + MAX_OBSERVED);
        } else {
          showAddItemDialog();
        }
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
//    this.users = loadFromFile();
  }

  private void showAddItemDialog() {
    final EditText taskEditText = new EditText(this);
    AlertDialog dialog = new Builder(this, R.style.AlertDialogStyle)
        .setTitle("Observe new user")
        .setMessage("Enter name of the user to follow")
        .setView(taskEditText)
        .setPositiveButton("Observe", new OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            final String userName = String.valueOf(taskEditText.getText());
            if (userName.trim().isEmpty()) {
            } else if (isUserPresent(userName)) {
              snackbar(listView, "User " + userName + " is already observed.");
            } else {
              addNewUser(userName);
            }
          }
        })
        .setNegativeButton("Cancel", null)
        .create();
    dialog.show();
  }

  private boolean isUserPresent(String userName) {
    for (User user : users) {
      if (user.getName().equalsIgnoreCase(userName)) {
        return true;
      }
    }
    return false;
  }

  private void addNewUser(final String userName) {
    networkExecutor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          User user = Processor.getUser(userName);
          addUserToList(user);
          snackbar(listView, "You are now observing user: " + userName);
        } catch (UserNotFoundException e) {
          snackbar(listView, "User " + userName + " was not found");
        } catch (PrivateOrNoPostsException e) {
          snackbar(listView,
              "User " + userName + " was found. However, only profiles that are public and have at least one post can be observed.");
        }
      }
    });
  }

  private void snackbar(View view, String message) {
    Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        .setAction("Action", null).show();
  }

  /**
   * Use from within adapter.
   */
  public void removeUserFromList(int position) {
    users.remove(position);
    adapter.refreshItems(users);
    saveToFile(users);
  }

  public void addUserToList(User user) {
    users.add(user);
    adapter.refreshItems(users);
    saveToFile(users);
  }

  private void saveToFile(List<User> list) {
    Log.i(LOG_TAG, "saveToFile: " + list);
    fileIOExecutor.submit(new Runnable() {
      @Override
      public void run() {
        try (
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos)) {
          os.writeObject(list);
        } catch (IOException e) {
          Log.w(LOG_TAG, "Failed to save list to file: ", e);
        }
      }
    });
  }

  private List<User> loadFromFile() {
    try (
        FileInputStream fis = context.openFileInput(FILE_NAME);
        ObjectInputStream is = new ObjectInputStream(fis)) {
      List<User> storage = (ArrayList) is.readObject();
      Log.i(LOG_TAG, "loadedFromFile: " + users);
      return storage;
    } catch (IOException | ClassNotFoundException e) {
      Log.w(LOG_TAG, "Failed to save list to file: ", e);
    }
    return new ArrayList<>();
  }
}