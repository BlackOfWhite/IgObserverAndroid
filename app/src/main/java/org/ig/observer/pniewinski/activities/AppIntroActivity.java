package org.ig.observer.pniewinski.activities;

import static org.ig.observer.pniewinski.activities.MainActivity.IS_FIRST_RUN_PREFERENCE;
import static org.ig.observer.pniewinski.activities.MainActivity.PREFERENCES;

import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntroFragment;
import org.ig.observer.pniewinski.R;

public class AppIntroActivity extends AppIntro {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getSupportActionBar().hide();

    addSlide(AppIntroFragment
        .newInstance("Introduction", null,
            "You need to be logged in to receive notifications. You can log in using ANY Instagram account.", null,
            R.drawable.ic_magnifying_glass, getColorAccent(), getColorDark(), getColorDark()));
    addSlide(AppIntroFragment
        .newInstance("Introduction", null, "Use PLUS button in lower right corner to start observing new Instagram account.", null,
            R.drawable.ic_add_256, getColorAccent(), getColorDark(), getColorDark()));
    addSlide(AppIntroFragment.newInstance("Introduction", null, "Visit IgObserver from time to time to make sure you are up to date.", null,
        R.drawable.ic_magnifying_glass, getColorAccent(), getColorDark(), getColorDark()));

    findViewById(R.id.bottomContainer).setBackgroundColor(getColorPrimary());
    setColorSkipButton(getColorAccent());
    setIndicatorColor(getColorAccent(), DEFAULT_COLOR);
    setColorDoneText(getColorAccent());
    setNextArrowColor(getColorAccent());
  }

  private int getColorPrimary() {
    return ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary);
  }

  private int getColorAccent() {
    return ContextCompat.getColor(getApplicationContext(), R.color.colorAccent);
  }

  private int getColorDark() {
    return ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark);
  }


  @Override
  public void onDonePressed(Fragment currentFragment) {
    super.onDonePressed(currentFragment);
    complete();
  }

  @Override
  public void onSkipPressed(Fragment currentFragment) {
    super.onSkipPressed(currentFragment);
    complete();
  }

  private void complete() {
    getSharedPreferences(PREFERENCES, MODE_PRIVATE).edit()
        .putBoolean(IS_FIRST_RUN_PREFERENCE, false).commit();
    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
    startActivity(intent);
    finish();
  }
}