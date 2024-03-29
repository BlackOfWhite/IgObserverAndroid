package org.ig.observer.pniewinski.model;

import androidx.annotation.NonNull;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class History implements Serializable, Comparable<History> {

  private static final DateFormat dateFormat = new SimpleDateFormat("HH:mm dd.MM.yyyy");
  private String userName;
  private Long timestamp;
  private String timestampText;
  private String message;

  public History(String userName, Long timestamp, String message) {
    this.userName = userName;
    this.timestamp = timestamp;
    this.timestampText = dateFormat.format(timestamp);
    this.message = message;
  }

  @Override
  public String toString() {
    return "History{" +
        "userName='" + userName + '\'' +
        ", timestamp='" + timestamp + '\'' +
        ", message='" + message + '\'' +
        '}';
  }

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public Long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Long timestamp) {
    this.timestamp = timestamp;
  }

  public String getTimestampText() {
    return timestampText;
  }

  public void setTimestampText(String timestampText) {
    this.timestampText = timestampText;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  @Override
  public int compareTo(@NonNull History o) {
    return -1 * this.getTimestamp().compareTo(o.getTimestamp());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    History history = (History) o;

    return new EqualsBuilder()
        .append(getUserName(), history.getUserName())
        .append(getTimestamp(), history.getTimestamp())
        .append(getMessage(), history.getMessage())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(getUserName())
        .append(getTimestamp())
        .append(getMessage())
        .toHashCode();
  }
}
