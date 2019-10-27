package org.ig.observer.pniewinski.model;

import java.io.Serializable;

public class History implements Serializable {

  private String userName;
  private String timeStamp;
  private String message;

  public History(String userName, String timeStamp, String message) {
    this.userName = userName;
    this.timeStamp = timeStamp;
    this.message = message;
  }

  @Override
  public String toString() {
    return "History{" +
        "userName='" + userName + '\'' +
        ", timeStamp='" + timeStamp + '\'' +
        ", message='" + message + '\'' +
        '}';
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(String timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
