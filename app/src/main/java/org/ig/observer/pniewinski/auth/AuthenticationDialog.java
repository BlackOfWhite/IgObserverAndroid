package org.ig.observer.pniewinski.auth;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import org.ig.observer.pniewinski.R;

public class AuthenticationDialog extends Dialog {

  public AuthenticationDialog(@NonNull Context context) {
    super(context);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    this.setContentView(R.layout.auth_dialog);
  }
}