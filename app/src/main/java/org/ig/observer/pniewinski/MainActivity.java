package org.ig.observer.pniewinski;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
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
import org.ig.observer.pniewinski.exceptions.NetworkNotFound;
import org.ig.observer.pniewinski.exceptions.UserNotFoundException;
import org.ig.observer.pniewinski.model.User;
import org.ig.observer.pniewinski.network.Processor;
import org.ig.observer.pniewinski.service.AlarmReceiver;

public class MainActivity extends AppCompatActivity {

  public static final String LOG_TAG = "IG_TAG";
  private static final Long SERVICE_PERIOD = 300000L; // 5min
  private static final String FILE_NAME = "ig_observer_storage";
  private static final int MAX_OBSERVED = 10;
  private static List<User> users = new ArrayList<>();
  private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
  private ExecutorService fileIOExecutor = Executors.newSingleThreadExecutor();
  private ListView listView;
  private IgListAdapter adapter;
  private Context context;
  private Processor networkProcessor;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.context = this;
    this.users = loadFromFile();
    this.networkProcessor = new Processor();
    adapter = new IgListAdapter(this, users);
    listView = (ListView) findViewById(R.id.list_view);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        Log.i(LOG_TAG, "onItemClick: " + position);
        View bottomView = view.findViewById((R.id.layout_bottom));
        if (bottomView.getVisibility() == View.GONE) {
          bottomView.setVisibility(View.VISIBLE);
        } else {
          bottomView.setVisibility(View.GONE);
        }
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
//    clearStorage();
    scheduleAlarm();
  }

  // Setup a recurring alarm every half hour
  private void scheduleAlarm() {
    // Construct an intent that will execute the AlarmReceiver
    Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
    // Create a PendingIntent to be triggered when the alarm goes off
    final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
        intent, PendingIntent.FLAG_UPDATE_CURRENT);
    // Setup periodic alarm every every half hour from this point onwards
    long firstMillis = System.currentTimeMillis(); // alarm is set right away
    AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
    // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
        SERVICE_PERIOD, pIntent); // every 5 min
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
          User user = networkProcessor.getUser(userName);
          addUserToList(user);
          snackbar(listView, "You are now observing user: " + userName);
        } catch (UserNotFoundException e) {
          snackbar(listView, "User " + userName + " was not found");
        } catch (NetworkNotFound networkNotFound) {
          snackbar(listView, "No internet connection");
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
    Log.i(LOG_TAG, "addUserToList: " + user);
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
      Log.w(LOG_TAG, "Failed to load list from file: ", e);
    } catch (Exception e) {
      Log.w(LOG_TAG, "Failed to load list from file. Unexpected exception: ", e);
    }
    return new ArrayList<>();
  }

  /**
   * Only development purposes.
   */
  private void clearStorage() {
    saveToFile(new ArrayList<>());
  }
}