package org.ig.observer.pniewinski.exceptions;

public class NetworkNotFound extends Exception {

  public NetworkNotFound() {
    super("Unable to connect to internet.");
  }
}
