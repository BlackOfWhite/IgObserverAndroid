package org.ig.observer.pniewinski.model.own;

import java.io.Serializable;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserOwn implements Serializable {

  private Data data;
  private String accessToken;

  public UserOwn() {

  }

  public Data getData() {
    return data;
  }

  public void setData(Data data) {
    this.data = data;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @Override
  public String toString() {
    return "UserOwn{" +
        "data=" + data +
        ", accessToken='" + accessToken + '\'' +
        '}';
  }
}
