package org.ig.observer.pniewinski.activities;

import static org.ig.observer.pniewinski.SnackbarUtils.networkNotFoundSnackbar;
import static org.ig.observer.pniewinski.SnackbarUtils.sessionEndSnackbar;
import static org.ig.observer.pniewinski.SnackbarUtils.snackbar;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.SELECTED_USER_NAME;
import static org.ig.observer.pniewinski.activities.UserSettingsActivity.SELECTED_USER_POSITION;
import static org.ig.observer.pniewinski.io.FileManager.loadCookieFromFile;
import static org.ig.observer.pniewinski.io.FileManager.loadUsersFromFile;
import static org.ig.observer.pniewinski.io.FileManager.saveCookieToFile;
import static org.ig.observer.pniewinski.io.FileManager.saveUsersToFile;
import static org.ig.observer.pniewinski.network.NetworkProcessor.clearCookies;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.adapters.IgListAdapter;
import org.ig.observer.pniewinski.auth.AuthenticationDialog;
import org.ig.observer.pniewinski.auth.AuthenticationListener;
import org.ig.observer.pniewinski.exceptions.ConnectionError;
import org.ig.observer.pniewinski.exceptions.NetworkNotFound;
import org.ig.observer.pniewinski.exceptions.UserNotFoundException;
import org.ig.observer.pniewinski.model.User;
import org.ig.observer.pniewinski.network.NetworkProcessor;
import org.ig.observer.pniewinski.service.AlarmReceiver;

public class MainActivity extends AppCompatActivity implements AuthenticationListener {

  public static final String LOG_TAG = "IG_TAG";
  public static final Long SERVICE_INTERVAL = 10 * 60_000L; // 10min
  public static final String IG_BROADCAST_LIST_UPDATE = "ig_broadcast_list_update";
  public static final String IG_BROADCAST_SESSION_END = "ig_broadcast_session_end";
  private static final int MAX_OBSERVED = 10;
  private ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
  private ListView listView;
  private IgListAdapter adapter;
  private Context context;
  private NetworkProcessor networkProcessor;
  private BroadcastReceiver broadcastReceiver; // receive events from IgService
  private MenuItem logInItem;

  // Create an action bar button
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.toolbar_menu, menu);
    // Login button
    if (loadCookieFromFile(this) != null) {
      logInItem = menu.findItem(R.id.button_login);
      logInItem.setTitle(R.string.app_log_out);
    }
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      Intent intent = new Intent(this, SettingsActivity.class);
      this.startActivity(intent);
    } else if (id == R.id.history_settings) {
      Intent intent = new Intent(this, HistoryActivity.class);
      this.startActivity(intent);
    } else if (id == R.id.button_login) {
      if (null != loadCookieFromFile(this)) {
        // Log out
        signOut();
      } else {
        // Log in
        AuthenticationDialog authenticationDialog = new AuthenticationDialog(this);
        authenticationDialog.show();
      }
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    this.context = this;
    // Adapter
    CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>(loadUsersFromFile(context));
    this.networkProcessor = new NetworkProcessor();
    // List
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
        Intent myIntent = new Intent(MainActivity.this, UserSettingsActivity.class);
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
        if (intent.getAction().equals(IG_BROADCAST_SESSION_END)) {
          signOut();
          sessionEndSnackbar(listView);
        } else if (intent.getAction().equals(IG_BROADCAST_LIST_UPDATE)) {
          CopyOnWriteArrayList<User> users = new CopyOnWriteArrayList<>((ArrayList<User>) intent.getSerializableExtra("user_list"));
          leftJoinUserList(users);
        }
      }
    };
    registerReceiver(broadcastReceiver, new IntentFilter(IG_BROADCAST_LIST_UPDATE));
    registerReceiver(broadcastReceiver, new IntentFilter(IG_BROADCAST_SESSION_END));

    // Load Ads
    MobileAds.initialize(this, initializationStatus -> {
    });
    AdView mAdView = findViewById(R.id.adView);
    AdRequest adRequest = new AdRequest.Builder().build();
    mAdView.loadAd(adRequest);
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
    AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
    alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
        SERVICE_INTERVAL, pIntent); // every 5 min
  }

  @Override
  protected void onResume() {
    super.onResume();
//    this.users = loadUsersFromFile();
  }

  private void showAddItemDialog() {
    EditText taskEditText = new EditText(this);
    taskEditText.setSingleLine(true);
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
          final String cookie = loadCookieFromFile(context);
          User user = networkProcessor.getUser(userName, cookie);
          addUserToList(user);
          snackbar(listView, "You are now observing user: " + userName);
        } catch (UserNotFoundException e) {
          snackbar(listView, "User " + userName + " was not found");
        } catch (NetworkNotFound e) {
          networkNotFoundSnackbar(listView);
        } catch (ConnectionError connectionError) {
          sessionEndSnackbar(listView);
          signOut();
        }
      }
    });
  }

  /**
   * Use from within adapter.
   */
  public void removeUserFromList(int position) {
    CopyOnWriteArrayList<User> list = new CopyOnWriteArrayList<>(adapter.getUserList());
    list.remove(position);
    adapter.refreshItems(list);
    saveUsersToFile(list, context);
  }

  public void addUserToList(User user) {
    Log.i(LOG_TAG, "addUserToList: " + user);
    CopyOnWriteArrayList<User> list = new CopyOnWriteArrayList<>(adapter.getUserList());
    list.add(user);
    adapter.refreshItems(list, list.size() == 1);
    saveUsersToFile(list, context);
  }

  /**
   * Used only from IgService
   */
  private void leftJoinUserList(List<User> newUsers) {
//    Log.i(LOG_TAG, "leftJoinUserList: " + users);
    CopyOnWriteArrayList<User> finalList = adapter.leftJoinItems(newUsers);
    saveUsersToFile(finalList, context);
  }

  /**
   * Only development purposes.
   */
  private void clearStorage() {
    saveUsersToFile(new CopyOnWriteArrayList<>(), context);
  }

  private void signOut() {
    clearCookies(this);
    saveCookieToFile(null, context);
    logInItem.setTitle(R.string.app_log_in);
  }

  @Override
  public void onLoginSuccessful(String cookie) {
    Log.i(LOG_TAG, "New cookie found: " + cookie);
    if (cookie != null) {
      snackbar(listView, "Successfully logged in");
      saveCookieToFile(cookie, context);
      runOnUiThread(() -> logInItem.setTitle(R.string.app_log_out));
    } else {
      snackbar(listView, "Opss.. failed to log in");
    }
  }
}