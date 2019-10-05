package org.ig.observer.pniewinski.model;

import java.io.Serializable;

public class User implements Serializable {

  private final long id;
  private final String name;
  private Integer image;
  private int posts;
  private int followers;
  private String bio;

  public User(long id, String name, Integer image) {
    this(id, name, image, 0, 0, "");
  }

  public User(long id, String name, Integer image, int posts, int followers, String bio) {
    this.id = id;
    this.image = image;
    this.name = name;
    this.posts = posts;
    this.followers = followers;
    this.bio = bio;
  }

  public long getId() {
    return id;
  }

  public Integer getImage() {
    return image;
  }

  public void setImage(Integer image) {
    this.image = image;
  }

  public String getName() {
    return name;
  }

  public int getPosts() {
    return posts;
  }

  public void setPosts(int posts) {
    this.posts = posts;
  }

  public int getFollowers() {
    return followers;
  }

  public void setFollowers(int followers) {
    this.followers = followers;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", image=" + image +
        ", posts=" + posts +
        ", followers=" + followers +
        ", bio='" + bio + '\'' +
        '}';
  }

  public String getDetails() {
    return "Posts: " + posts + "   Followers: " + followers;
  }
}
