package org.ig.observer.pniewinski.model;

import java.io.Serializable;
import java.text.DecimalFormat;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class User implements Serializable {

  private Long id;
  private String name;
  private String img_url;
  private Long posts;
  private Long followed_by;
  private Long follows;
  private String biography;
  private boolean is_private;
  private boolean has_stories;

  public User(Long id, String name, String img_url) {
    this(id, name, img_url, 0L, 0L, 0L, "", false, false);
  }

  public User(Long id, String name, String img_url, Long posts, Long follows, Long followed_by, String biography, boolean is_private,
      boolean has_stories) {
    this.id = id;
    this.img_url = img_url;
    this.name = name;
    this.posts = posts;
    this.follows = follows;
    this.followed_by = followed_by;
    this.biography = biography;
    this.is_private = is_private;
    this.has_stories = has_stories;
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

  public String getImg_url() {
    return img_url;
  }

  public void setImg_url(String img_url) {
    this.img_url = img_url;
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

  public Boolean getIs_private() {
    return is_private;
  }

  public void setIs_private(Boolean is_private) {
    this.is_private = is_private;
  }

  public Boolean isHas_stories() {
    return has_stories;
  }

  public void setHas_stories(boolean has_stories) {
    this.has_stories = has_stories;
  }

  public String getInfo() {
//    if (is_private) {
//      return "This account is private";
//    }
    return "Posts: " + getPosts() + "   " + (has_stories ? "User has some stories" : "No stories") +
        "\nFollows: " + prettyCount(getFollows()) + "   Followed by: " + prettyCount(getFollowed_by());
  }

  @Override
  public String toString() {
    return "User{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", img_url='" + img_url + '\'' +
        ", posts=" + posts +
        ", followed_by=" + followed_by +
        ", follows=" + follows +
        ", biography='" + biography + '\'' +
        ", is_private=" + is_private +
        ", has_stories=" + has_stories +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    User user = (User) o;

    return new EqualsBuilder()
        .append(getId(), user.getId())
        .append(getName(), user.getName())
        .append(getImg_url(), user.getImg_url())
        .append(getPosts(), user.getPosts())
        .append(getFollowed_by(), user.getFollowed_by())
        .append(getFollows(), user.getFollows())
        .append(getBiography(), user.getBiography())
        .append(getIs_private(), user.getIs_private())
        .append(isHas_stories(), user.isHas_stories())
        .isEquals();
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder(17, 37)
        .append(getId())
        .append(getName())
        .append(getImg_url())
        .append(getPosts())
        .append(getFollowed_by())
        .append(getFollows())
        .append(getBiography())
        .append(getIs_private())
        .append(isHas_stories())
        .toHashCode();
  }
}
