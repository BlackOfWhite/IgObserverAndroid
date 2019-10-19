package org.ig.observer.pniewinski.model.own;

import java.io.Serializable;

public class Data implements Serializable {

  /**
   * {"data": {"id": "3032831214", "username": "niewinskipiotr",
   * "profile_picture": "https://scontent.cdninstagram.com/vp/2b9b4346b8bc9fc167ca2a72d08c6395/5E5DCFF6/t51.2885-19/s150x150/71033882_1325873760917105_487215631154806784_n.jpg?_nc_ht=scontent.cdninstagram.com",
   * "full_name": "",
   * "bio": "\ud83d\udc89\ud83c\udfcb\ud83c\udffc\u200d\u2642\ufe0f\u26f5\ufe0f\ud83e\udd4a\ud83c\udf77\ud83d\udeb4\ud83c\udffc\u200d\u2642\ufe0f\ud83e\udd37\ud83c\udffc\u200d\u2640\ufe0f\ud83d\udec1\ud83d\udcff\ud83c\udfca\ud83c\udffc\u200d\u2642\ufe0f\u2708\ufe0f\ud83d\udcbb\ud83e\udd39\ud83c\udffc\u200d\u2642\ufe0f\ud83c\udf9e\u265f\n          \n\u2615\ufe0f @learnjavadaily",
   * "website": "",
   * "is_business": false,
   * "counts": {"media": 28, "follows": 226, "followed_by": 169}},
   * }
   **/
  private Long id;
  private String username;
  private String profile_picture;
  private String bio;
  private Counts counts;

  public Data() {
  }

  @Override
  public String toString() {
    return "Data{" +
        "id=" + id +
        ", username='" + username + '\'' +
        ", profile_picture='" + profile_picture + '\'' +
        ", bio='" + bio + '\'' +
        ", counts=" + counts +
        '}';
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getProfile_picture() {
    return profile_picture;
  }

  public void setProfile_picture(String profile_picture) {
    this.profile_picture = profile_picture;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public Counts getCounts() {
    return counts;
  }

  public void setCounts(Counts counts) {
    this.counts = counts;
  }
}

