package org.ig.observer.pniewinski.exceptions;

public class ConnectionError extends Exception {

  private int httpCode;

  public ConnectionError(int httpCode) {
    super("Connection error: " + httpCode);
    this.httpCode = httpCode;
  }

  public int getHttpCode() {
    return httpCode;
  }
}
