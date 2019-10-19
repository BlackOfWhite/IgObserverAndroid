package org.ig.observer.pniewinski.model.own;

import java.io.Serializable;

public class Counts implements Serializable {

  /**
   * "counts": {"media": 28, "follows": 226, "followed_by": 169}},
   */
  private Long media;
  private Long follows;
  private Long followed_by;

  public Counts() {

  }

  @Override
  public String toString() {
    return "Counts{" +
        "media=" + media +
        ", follows=" + follows +
        ", followed_by=" + followed_by +
        '}';
  }

  public Long getMedia() {
    return media;
  }

  public void setMedia(Long media) {
    this.media = media;
  }

  public Long getFollows() {
    return follows;
  }

  public void setFollows(Long follows) {
    this.follows = follows;
  }

  public Long getFollowed_by() {
    return followed_by;
  }

  public void setFollowed_by(Long followed_by) {
    this.followed_by = followed_by;
  }
}
