package org.ig.observer.pniewinski.network;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;
import static org.ig.observer.pniewinski.network.util.ResponseParser.getMatch;
import static org.ig.observer.pniewinski.network.util.ResponseParser.parseBoolean;
import static org.ig.observer.pniewinski.network.util.ResponseParser.parseLong;
import static org.ig.observer.pniewinski.network.util.ResponseParser.parseString;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import org.ig.observer.pniewinski.exceptions.ConnectionError;
import org.ig.observer.pniewinski.exceptions.NetworkNotFound;
import org.ig.observer.pniewinski.exceptions.UserNotFoundException;
import org.ig.observer.pniewinski.model.User;

public class Processor {

  private static final String USER_FEED_URL = "https://www.instagram.com/%s/"; // https://www.instagram.com/xd/?__a=1
  private static final Pattern USER_ID_PATTERN = Pattern.compile("\"id\":\"(\\d+)\",\\\"is_busi"); // "id":"(\d+)","is_buss
  private static final Pattern FOLLOWS_PATTERN = Pattern.compile("\"edge_follow\":\\{\"count\":(\\d+)\\}"); // "edge_follow":{"count":3626}
  private static final Pattern FOLLOWED_BY_PATTERN = Pattern
      .compile("\"edge_followed_by\":\\{\"count\":(\\d+)\\}"); // "edge_followed_by":{"count":3626
  private static final Pattern USER_BIOGRAPHY_PATTERN = Pattern.compile("\"biography\":\"[^\"]*"); // {"biography":"bio"
  private static final Pattern USER_POSTS_COUNT_PATTERN = Pattern
      .compile("\"edge_owner_to_timeline_media\":\\{\"count\":(\\d+),"); // "edge_owner_to_timeline_media":{"count":52,
  private static final Pattern IS_PRIVATE_PATTERN = Pattern.compile("\"is_private\":(true|false),"); // "is_private":true,
  private static final Pattern PROFILE_IMG_URL_PATTERN = Pattern
      .compile("<meta property=\"og:image\" content=\"[^\"]*"); // <meta property="og:image" content="url"
//  private static final String USER_STORIES_URL =
//      "https://www.instagram.com/graphql/query/?query_hash=eb1918431e946dd39bf8cf8fb870e426&variables="
//          + "{\"reel_ids\": [%s],\"precomposed_overlay\": \"False\",\"show_story_viewer_list\": \"True\",\"story_viewer_fetch_count\": 50,\"story_viewer_cursor\": \"\"}";
//  private static final Pattern USER_STORY_PATTERN = Pattern.compile(
//      "\\{\"src\":\""); // {"src":"https://scontent-frt3-1.cdninstagram.com/vp/e3cb5179952d94b173303b5fedfe717f/5D9827B5/t51.12442-15/sh0.08/e35/p640x640/71270402_596572067543731_4247509925026537721_n.jpg?_nc_ht=scontent-frt3-1.cdninstagram.com&_nc_cat=107"

  public User getUser(String userName) throws UserNotFoundException, NetworkNotFound {
    HttpsURLConnection sslConnection;
    try {
      URLConnection urlConnection = new URL(String.format(USER_FEED_URL, userName)).openConnection();
      sslConnection = (HttpsURLConnection) urlConnection;
      if (sslConnection == null) {
        Log.i(LOG_TAG, "Failed to open HTTPS connection.");
        throw new ConnectionError();
      }
      sslConnection.setInstanceFollowRedirects(false);
      int responseCode = sslConnection.getResponseCode();
      if (responseCode >= 400) {
        Log.i(LOG_TAG, "Got invalid response code: " + responseCode + ", response message: " + sslConnection.getResponseMessage());
        throw new ConnectionError();
      } else {
        Log.i(LOG_TAG, "Response code: " + responseCode);
      }
      String img_url = null;
      try (BufferedReader br =
          new BufferedReader(
              new InputStreamReader(sslConnection.getInputStream()))) {
        String next;
        while ((next = br.readLine()) != null) {
          Log.i(LOG_TAG, "RL: " + next);
          if (next.contains("<script type=\"text/javascript\">window._sharedData = {\"config\":{\"csrf_token\"")) {
//            Log.i(LOG_TAG, "User data line was found: " + next);
            Long id = parseLong(getMatch(next, USER_ID_PATTERN), "\"", 3, 10);
            Long follows = parseLong(getMatch(next, FOLLOWS_PATTERN), ":", 2);
            Long followed_by = parseLong(getMatch(next, FOLLOWED_BY_PATTERN), ":", 2);
            String biography = parseString(getMatch(next, USER_BIOGRAPHY_PATTERN), "\"", 3, 0);
            Long post_count = parseLong(getMatch(next, USER_POSTS_COUNT_PATTERN), ":", 2);
            Boolean is_private = parseBoolean(getMatch(next, IS_PRIVATE_PATTERN), ":", 1);
            if (id != null) {
              return new User(id, userName, img_url, post_count, follows, followed_by, biography, is_private);
            } else {
              throw new UserNotFoundException(userName);
            }
          } else if (img_url == null) {
            // this  should be matched before statement above
            img_url = parseString(getMatch(next, PROFILE_IMG_URL_PATTERN), "\"", 3, 0);
          }
        }
      }
    } catch (UnknownHostException e) {
      Log.w(LOG_TAG, "getUser: " + userName, e);
      throw new NetworkNotFound();
    } catch (Exception e) {
      Log.w(LOG_TAG, "getUser: " + userName, e);
      throw new UserNotFoundException(userName);
    }
    Log.w(LOG_TAG, "getUser: " + userName + ", no patterns found in response");
    throw new UserNotFoundException(userName);
  }
}