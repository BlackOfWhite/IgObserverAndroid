package org.ig.observer.pniewinski.model;

import java.io.Serializable;
import java.text.DecimalFormat;

public class User implements Serializable {

  private final Long id;
  private final String name;
  private Integer image;
  private Long posts;
  private Long followed_by;
  private Long follows;
  private String biography;

  public User(Long id, String name, Integer image) {
    this(id, name, image, 0l, 0l, 0l, "");
  }

  public User(Long id, String name, Integer image, Long posts, Long follows, Long followed_by, String biography) {
    this.id = id;
    this.image = image;
    this.name = name;
    this.posts = posts;
    this.follows = follows;
    this.followed_by = followed_by;
    this.biography = biography;
  }

  public static String prettyCount(Number number) {
    char[] suffix = {' ', 'k', 'M'};
    Long numValue = number.longValue();
    int value = (int) Math.floor(Math.log10(numValue));
    int base = value / 3;
    if (value >= 3 && base < suffix.length) {
      return new DecimalFormat("#0.0").format(numValue / Math.pow(10, base * 3)) + suffix[base];
    } else {
      return new DecimalFormat("#,##0").format(numValue);
    }
  }

  private static String validValue(String s) {
    return s == null ? "" : s;
  }

  private static Long validValue(Long l) {
    return l == null ? 0 : l;
  }

  public Long getId() {
    return id;
  }

  public Integer getImage() {
    return image;
  }

  public void setImage(Integer image) {
    this.image = image;
  }

  public String getName() {
    return validValue(name);
  }

  public Long getPosts() {
    return validValue(posts);
  }

  public void setPosts(Long posts) {
    this.posts = posts;
  }

  public Long getFollowed_by() {
    return followed_by;
  }

  public void setFollowed_by(Long followed_by) {
    this.followed_by = followed_by;
  }

  public Long getFollows() {
    return validValue(follows);
  }

  public void setFollows(Long follows) {
    this.follows = follows;
  }

  public String getBiography() {
    return validValue(biography);
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
        ", biography='" + biography + '\'' +
        '}';
  }

  public String getInfo() {
    return "Posts: " + getPosts() + "\nFollows: " + prettyCount(getFollows()) + "   Followed by: " + prettyCount(getFollowed_by());
  }
}
