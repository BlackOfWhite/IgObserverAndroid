package org.ig.observer.pniewinski.model;

import static org.ig.observer.pniewinski.network.Processor.NOT_FOUND;

import java.io.Serializable;
import java.text.DecimalFormat;

public class User implements Serializable {

  private final long id;
  private final String name;
  private Integer image;
  private long posts;
  private long followed_by;
  private long follows;
  private String description;
  private String biography;

  public User(long id, String name, Integer image) {
    this(id, name, image, 0, 0, 0, "", "");
  }

  public User(long id, String name, Integer image, long posts, long follows, long followed_by, String description, String biography) {
    this.id = id;
    this.image = image;
    this.name = name;
    this.posts = posts;
    this.follows = follows;
    this.followed_by = followed_by;
    this.description = description;
    this.biography = biography;
  }

  public static String prettyCount(Number number) {
    char[] suffix = {' ', 'k', 'M'};
    long numValue = number.longValue();
    int value = (int) Math.floor(Math.log10(numValue));
    int base = value / 3;
    if (value >= 3 && base < suffix.length) {
      return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
    } else {
      return new DecimalFormat("#,##0").format(numValue);
    }
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

  public long getPosts() {
    return posts;
  }

  public void setPosts(long posts) {
    this.posts = posts;
  }


  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public long getFollowed_by() {
    return followed_by;
  }

  public void setFollowed_by(long followed_by) {
    if (followed_by != NOT_FOUND) {
      this.followed_by = followed_by;
    }
  }

  public long getFollows() {
    return follows;
  }

  public void setFollows(long follows) {
    if (follows != NOT_FOUND) {
      this.follows = follows;
    }
  }

  public String getBiography() {
    return biography;
  }

  public void setBiography(String biography) {
    this.biography = biography;
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", image=" + image +
        ", posts=" + posts +
        ", followed_by=" + followed_by +
        ", follows=" + follows +
        ", description='" + description + '\'' +
        ", biography='" + biography + '\'' +
        '}';
  }

  public String getDetails() {
    return "Posts: " + posts + "   Follows: " + prettyCount(follows) + "   Observed by: " + prettyCount(followed_by);
  }
}
