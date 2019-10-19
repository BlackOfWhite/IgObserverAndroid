package org.ig.observer.pniewinski.auth;

public interface AuthenticationListener {

  void onTokenReceived(String auth_token);

  void onSignOut();
}
