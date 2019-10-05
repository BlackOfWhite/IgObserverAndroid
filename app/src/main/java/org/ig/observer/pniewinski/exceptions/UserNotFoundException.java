package org.ig.observer.pniewinski.exceptions;

public class UserNotFoundException extends Exception {

  public UserNotFoundException(String userName) {
    super("User " + userName + " was not found.");
  }

}
