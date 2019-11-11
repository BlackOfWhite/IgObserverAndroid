package org.ig.observer.pniewinski.activities;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog.Builder;
import androidx.core.app.NavUtils;
import org.ig.observer.pniewinski.R;

public class SettingsActivity extends AppCompatPreferenceActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(R.style.SettingsStyle);
    setupActionBar();
    getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
  }

  /**
   * Set up the {@link android.app.ActionBar}, if the API is available.
   */
  private void setupActionBar() {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      // Show the Up button in the action bar.
      actionBar.setDisplayHomeAsUpEnabled(true);
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

      // Just to make it look good
      Preference about = findPreference("pref_header_about");
      about.setSummary(getText(R.string.summary_about));

      // Privacy policy
      Preference privacyPolicy = findPreference("pref_header_privacy_policy");
      privacyPolicy.setSummary(getText(R.string.summary_privacy_policy));
      privacyPolicy.setOnPreferenceClickListener(new OnPreferenceClickListener() {
        @Override
        public boolean onPreferenceClick(Preference preference) {
          showPrivacyPolicyDialog();
          return false;
        }
      });
    }

    private void showPrivacyPolicyDialog() {
      LayoutInflater inflater = LayoutInflater.from(getActivity());
      // ScrollView
      View dialogView = inflater.inflate(R.layout.privacy_policy_dialog, null);
      TextView textView = dialogView.findViewById(R.id.text_view_privacy_policy);
      // Handle links
      final SpannableString text =
          new SpannableString(getText(R.string.privacy_policy));
      Linkify.addLinks(text, Linkify.WEB_URLS);
      textView.setText(text);
      textView.setAutoLinkMask(0);
      textView.setMovementMethod(LinkMovementMethod.getInstance());
      // Show dialog
      Dialog dialog = new Builder(getActivity(), R.style.AlertDialogStyle)
          .setCustomTitle(makeCustomTitle())
          .setView(dialogView)
          .setPositiveButton("OK", null)
          .create();
      dialog.show();
    }

    private TextView makeCustomTitle() {
      TextView title = new TextView(getActivity());
      title.setText("Privacy Policy");
      title.setPadding(10, 10, 10, 10);
      title.setGravity(Gravity.CENTER);
      title.setTextColor(Color.BLACK);
      return title;
    }
  }
}
