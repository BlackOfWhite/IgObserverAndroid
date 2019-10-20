package org.ig.observer.pniewinski.auth;

import org.ig.observer.pniewinski.model.Auth;

public interface AuthenticationListener {

  void onTokenReceived(Auth auth);

  void onSignOut();
}
