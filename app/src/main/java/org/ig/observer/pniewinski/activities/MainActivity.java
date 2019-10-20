package org.ig.observer.pniewinski.activities;

import static org.ig.observer.pniewinski.SnackbarUtils.networkNotFoundSnackbar;
import static org.ig.observer.pniewinski.SnackbarUtils.snackbar;
import static org.ig.observer.pniewinski.activities.SettingsActivity.SELECTED_USER_NAME;
import static org.ig.observer.pniewinski.activities.SettingsActivity.SELECTED_USER_POSITION;
import static org.ig.observer.pniewinski.io.FileManager.FILE_NAME_AUTH;
import static org.ig.observer.pniewinski.io.FileManager.FILE_NAME_USERS;
import static org.ig.observer.pniewinski.io.FileManager.loadAuthFromFile;
import static org.ig.observer.pniewinski.io.FileManager.loadUsersFromFile;
import static org.ig.observer.pniewinski.network.Processor.clearCookies;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ig.observer.pniewinski.IgListAdapter;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.auth.AuthenticationDialog;
import org.ig.observer.pniewinski.auth.AuthenticationListener;
import org.ig.observer.pniewinski.exceptions.NetworkNotFound;
import org.ig.observer.pniewinski.exceptions.UserNotFoundException;
import org.ig.observer.pniewinski.model.Auth;
import org.ig.observer.pniewinski.model.User;
import org.ig.observer.pniewinski.network.Processor;
import org.ig.observer.pniewinski.service.AlarmReceiver;

public class MainActivity extends AppCompatActivity implements AuthenticationListener {

  public static final String LOG_TAG = "IG_TAG";
  private static final Long SERVICE_INTERVAL = 5 * 60_000L; // 5min
  private static final int MAX_OBSERVED = 10;
  private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
  private ExecutorService fileIOExecutor = Executors.newSingleThreadExecutor();
  private ListView listView;
  private IgListAdapter adapter;
  private Context context;
  private Processor networkProcessor;
  private BroadcastReceiver broadcastReceiver; // receive events from IgService
  private volatile Auth auth;
  private MenuItem menuLoginItem;
  private boolean isFirstRun = true;

  // Create an action bar button
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.toolbar_menu, menu);
    menuLoginItem = menu.findItem(R.id.button_login);
    if (isFirstRun) {
      if (isUserSignedIn()) {
        menuLoginItem.setTitle(R.string.app_log_out);
        getSupportActionBar().setTitle(auth.getUser_name());
      } else {
        menuLoginItem.setTitle(R.string.app_log_in);
      }
      isFirstRun = false;
    }
    return super.onCreateOptionsMenu(menu);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.button_login) {
      Log.i(LOG_TAG, "Login clicked::" + auth);
      if (isUserSignedIn()) {
        signOutUser();
      } else {
        signInUser();
      }
    }
    return super.onOptionsItemSelected(item);
  }


  private boolean isAccessTokenValid() {
    return auth != null && auth.getAccess_token() != null && !auth.getAccess_token().isEmpty();
  }

  private synchronized void signInUser() {
    AuthenticationDialog authenticationDialog = new AuthenticationDialog(this, this);
    authenticationDialog.show();
  }

  private void signOutUser() {
    auth = null;
    saveAuthToFile(null);
    clearCookies(context);
    runOnUiThread(() -> {
      menuLoginItem.setTitle(R.string.app_log_in);
      getSupportActionBar().setTitle(R.string.app_name);
    });
//    networkExecutor.submit(new Runnable() {
//      @Override
//      public void run() {
//        try {
//          networkProcessor.signOut();
//        } catch (NetworkNotFound networkNotFound) {
//        }
//      }
//    });

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.context = this;
    // Adapter
    auth = loadAuthFromFile(context);
    Log.i(LOG_TAG, "onCreate::loaded auth:: " + auth);
    CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>(loadUsersFromFile(context));
    this.networkProcessor = new Processor();
    adapter = new IgListAdapter(this, users);
    listView = (ListView) findViewById(R.id.list_view);
    listView.setAdapter(adapter);
    listView.setLongClickable(true);
    listView.setOnItemClickListener(new OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position,
          long id) {
        Log.i(LOG_TAG, "onItemClick: " + position);
        View bottomDetailsView = view.findViewById((R.id.layout_details_bottom));
        if (bottomDetailsView.getVisibility() == View.GONE) {
          bottomDetailsView.setVisibility(View.VISIBLE);
        } else {
          bottomDetailsView.setVisibility(View.GONE);
        }
      }
    });
    listView.setOnItemLongClickListener(new OnItemLongClickListener() {
      @Override
      public boolean onItemLongClick(AdapterView<?> adapterView, View view, int pos, long id) {
        Log.i(LOG_TAG, "onItemClickLong: " + pos);
        Intent myIntent = new Intent(MainActivity.this, SettingsActivity.class);
        myIntent.putExtra(SELECTED_USER_POSITION, pos);
        User selectedUser = users.get(pos);
        myIntent.putExtra(SELECTED_USER_NAME, selectedUser.getName());
        MainActivity.this.startActivity(myIntent);
        return true;
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
//     clearStorage();
    scheduleAlarmService();
    // IgService BroadcastReceiver
    this.broadcastReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>((ArrayList<User>) intent.getSerializableExtra("user_list"));
        updateUsersList(users);
      }
    };
    registerReceiver(broadcastReceiver, new IntentFilter("ig_broadcast_intent"));
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    unregisterReceiver(broadcastReceiver);
  }

  // Setup a recurring alarm every half hour
  private void scheduleAlarmService() {
    // Construct an intent that will execute the AlarmReceiver
    Intent intent = new Intent(this, AlarmReceiver.class);
    // Create a PendingIntent to be triggered when the alarm goes off
    final PendingIntent pIntent = PendingIntent.getBroadcast(this, AlarmReceiver.REQUEST_CODE,
        intent, PendingIntent.FLAG_UPDATE_CURRENT);
    // Setup periodic alarm every every half hour from this point onwards
    long firstMillis = System.currentTimeMillis(); // alarm is set right away
    AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
        SERVICE_INTERVAL, pIntent); // every 5 min
  }

  @Override
  protected void onResume() {
    super.onResume();
//    this.users = loadUsersFromFile();
  }

  private void showAddItemDialog() {
    if (!isUserSignedIn()) {
      snackbar(listView, "You must be logged in to start observing accounts");
      return;
    }
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
    for (User user : adapter.getUserList()) {
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
          User user = networkProcessor.getUser(userName, auth.getAccess_token());
          addUserToList(user);
          snackbar(listView, "You are now observing user: " + userName);
        } catch (UserNotFoundException e) {
          snackbar(listView, "User " + userName + " was not found");
        } catch (NetworkNotFound e) {
          networkNotFoundSnackbar(listView);
        }
      }
    });
  }

  public boolean isUserSignedIn() {
    return isAccessTokenValid();
  }


  /**
   * Use from within adapter.
   */
  public void removeUserFromList(int position) {
    List<User> list = new CopyOnWriteArrayList<>(adapter.getUserList());
    list.remove(position);
    adapter.refreshItems(list);
    saveToFile(list);
  }

  public void addUserToList(User user) {
    Log.i(LOG_TAG, "addUserToList: " + user);
    List<User> list = new CopyOnWriteArrayList<>(adapter.getUserList());
    list.add(user);
    adapter.refreshItems(list, list.size() == 1);
    saveToFile(list);
  }

  /**
   * Used only from IgService
   */
  private void updateUsersList(List<User> newUsers) {
//    Log.i(LOG_TAG, "updateUsersList: " + users);
    adapter.refreshItems(newUsers);
    saveToFile(newUsers);
  }


  public void saveToFile(List<User> list) {
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

  private void saveAuthToFile(Auth auth) {
    fileIOExecutor.submit(new Runnable() {
      @Override
      public void run() {
        try (
            FileOutputStream fos = context.openFileOutput(FILE_NAME_AUTH, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos)) {
          os.writeObject(auth);
        } catch (IOException e) {
          Log.w(LOG_TAG, "Failed to save list to file: ", e);
        }
      }
    });
  }

  /**
   * Only development purposes.
   */
  private void clearStorage() {
    saveToFile(new CopyOnWriteArrayList<>());
  }

  @Override
  public void onTokenReceived(Auth auth) {
    Log.i(LOG_TAG, "Token received: " + auth.getAccess_token());
    if (menuLoginItem != null) {
      setUpOwnUser(auth);
    } else {
      Log.w(LOG_TAG, "Menu login item is NULL!");
    }
  }

  @Override
  public void onSignOut() {
    signOutUser();
  }

  private void setUpOwnUser(Auth auth) {
    networkExecutor.submit(new Runnable() {
      @Override
      public void run() {
        try {
          String userName = networkProcessor.getUserName(auth);
          auth.setUser_name(userName);
          MainActivity.this.auth = auth;
          saveAuthToFile(auth);
          runOnUiThread(() -> {
            menuLoginItem.setTitle(R.string.app_log_out);
            getSupportActionBar().setTitle(userName);
            snackbar(listView, "Welcome " + userName);
          });
        } catch (IOException e) {
          Log.w(LOG_TAG, "Unexpected exception while fetching user own name: ", e);
          return;
        }
      }
    });
  }
}