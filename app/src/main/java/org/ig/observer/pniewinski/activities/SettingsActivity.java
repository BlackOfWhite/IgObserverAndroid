package org.ig.observer.pniewinski.activities;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.MenuItem;
import org.ig.observer.pniewinski.R;

public class SettingsActivity extends AppCompatPreferenceActivity {

  public static final String SELECTED_USER_POSITION = "user_position";
  public static final String SELECTED_USER_NAME = "user_name";
  private static final Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
      String stringValue = newValue.toString();

      if (preference instanceof ListPreference) {
        // For list preferences, look up the correct display value in
        // the preference's 'entries' list.
        ListPreference listPreference = (ListPreference) preference;
        int index = listPreference.findIndexOfValue(stringValue);

        // Set the summary to reflect the new value.
        preference.setSummary(
            index >= 0
                ? listPreference.getEntries()[index]
                : null);
      } else if (preference instanceof EditTextPreference) {
        if (preference.getKey().equals("key_gallery_name")) {
          // update the changed gallery name to summary filed
          preference.setSummary(stringValue);
        }
      } else {
        preference.setSummary(stringValue);
      }
      return true;
    }
  };
  private int selectedUserPosition;
  private String selectedUserName;

  /**
   * Helper method to determine if the device has an extra-large screen. For
   * example, 10" tablets are extra-large.
   */
  private static boolean isXLargeTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
  }

  /**
   * Binds a preference's summary to its value. More specifically, when the
   * preference's value is changed, its summary (line of text below the
   * preference title) is updated to reflect the value. The summary is also
   * immediately updated upon calling this method. The exact display format is
   * dependent on the type of preference.
   *
   * @see #sBindPreferenceSummaryToValueListener
   */
  private static void bindPreferenceSummaryToValue(Preference preference) {
    // Set the listener to watch for value changes.
    preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

    // Trigger the listener immediately with the preference's
    // current value.
    sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
        PreferenceManager
            .getDefaultSharedPreferences(preference.getContext())
            .getString(preference.getKey(), ""));
  }

  /**
   * Email client intent to send support mail
   * Appends the necessary device information to email body
   * useful when providing support
   */
  public static void sendFeedback(Context context) {
    String body = null;
    try {
      body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
      body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
          Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
          "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
    } catch (PackageManager.NameNotFoundException e) {
    }
    Intent intent = new Intent(Intent.ACTION_SEND);
    intent.setType("message/rfc822");
    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"contact@androidhive.info"});
    intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
    intent.putExtra(Intent.EXTRA_TEXT, body);
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(R.style.SettingsStyle);
    if (savedInstanceState != null) {
      selectedUserPosition = savedInstanceState.getInt(SELECTED_USER_POSITION, -1);
      Log.i(LOG_TAG, "onCreate: restore intent position: " + selectedUserPosition);
      selectedUserName = savedInstanceState.getString(SELECTED_USER_NAME);
      Log.i(LOG_TAG, "onCreate: restore intent name: " + selectedUserName);
    } else {
      Intent intent = getIntent();
      selectedUserPosition = intent.getIntExtra(SELECTED_USER_POSITION, -1);
      Log.i(LOG_TAG, "onCreate: intent position: " + selectedUserPosition);
      selectedUserName = intent.getStringExtra(SELECTED_USER_NAME);
      Log.i(LOG_TAG, "onCreate: intent name: " + selectedUserName);
    }
    setupActionBar();
    getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
  }

  // Save before going into deeper settings
  @Override
  public void onSaveInstanceState(Bundle savedInstanceState) {
    savedInstanceState.putInt(SELECTED_USER_POSITION, selectedUserPosition);
    savedInstanceState.putString(SELECTED_USER_NAME, selectedUserName);

    // Always call the superclass so it can save the view hierarchy state
    super.onSaveInstanceState(savedInstanceState);
  }

  /**
   * Set up the {@link android.app.ActionBar}, if the API is available.
   */
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      // Show the Up button in the action bar.
      actionBar.setDisplayHomeAsUpEnabled(true);
      if (selectedUserName != null) {
        actionBar.setTitle("Settings for " + selectedUserName);
      }
    }
  }

  @Override
  public boolean onMenuItemSelected(int featureId, MenuItem item) {
    int id = item.getItemId();
    if (id == android.R.id.home) {
      if (!super.onMenuItemSelected(featureId, item)) {
        NavUtils.navigateUpFromSameTask(this);
      }
      return true;
    }
    return super.onMenuItemSelected(featureId, item);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  public static class MainPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.pref_main);

      // gallery EditText change listener
      bindPreferenceSummaryToValue(findPreference(getString(R.string.key_gallery_name)));

      // feedback preference click listener
      Preference myPref = findPreference(getString(R.string.key_send_feedback));
      myPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
          sendFeedback(getActivity());
          return true;
        }
      });
    }
  }
}
