package org.ig.observer.pniewinski.exceptions;

public class UserRemovedError extends UserNotFoundException {

  public UserRemovedError(String userName) {
    super("User " + userName + " was probably removed.");
  }

}
