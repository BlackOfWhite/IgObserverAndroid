package org.ig.observer.pniewinski.model;

import java.io.Serializable;

public class User implements Serializable {

  private final long id;
  private final Integer image;
  private final String name;
  private final String info;

  public User(long id, String name, String info, Integer image) {
    this.id = id;
    this.image = image;
    this.name = name;
    this.info = info;
  }

  public long getId() {
    return id;
  }

  public Integer getImage() {
    return image;
  }

  public String getName() {
    return name;
  }

  public String getInfo() {
    return info;
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", image=" + image +
        ", name='" + name + '\'' +
        ", info='" + info + '\'' +
        '}';
  }
}
