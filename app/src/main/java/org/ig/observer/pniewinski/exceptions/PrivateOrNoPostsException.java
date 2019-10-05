package org.ig.observer.pniewinski.exceptions;

public class PrivateOrNoPostsException extends Exception {

  public PrivateOrNoPostsException(String userName) {
    super("User " + userName + " must have public account and/or at least one post published.");
  }
}
