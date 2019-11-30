package org.ig.observer.pniewinski.exceptions;

public class TooManyRequestsException extends Exception {

  private int httpCode;

  public TooManyRequestsException(int httpCode) {
    super("TooManyRequests error: " + httpCode);
    this.httpCode = httpCode;
  }

  public int getHttpCode() {
    return httpCode;
  }
}
