package org.ig.observer.pniewinski.network;

import static org.ig.observer.pniewinski.MainActivity.LOG_TAG;
import static org.ig.observer.pniewinski.network.util.ResponseParser.getMatch;
import static org.ig.observer.pniewinski.network.util.ResponseParser.parseLong;
import static org.ig.observer.pniewinski.network.util.ResponseParser.parseString;

import android.util.Log;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.exceptions.PrivateOrNoPostsException;
import org.ig.observer.pniewinski.exceptions.UserNotFoundException;
import org.ig.observer.pniewinski.model.User;

public class Processor {

  public static final long NOT_FOUND = -12345;
  private static final String USER_FEED_URL = "https://www.instagram.com/%s";
  private static final Pattern USER_ID_PATTERN = Pattern.compile("\"owner\":\\{\"id\":\"(\\d+)\""); // "owner":{"id":"180859558"
  private static final Pattern FOLLOWS_PATTERN = Pattern.compile("\"edge_follow\":\\{\"count\":(\\d+)\\}"); // "edge_follow":{"count":3626}
  private static final Pattern FOLLOWED_BY_PATTERN = Pattern
      .compile("\"edge_followed_by\":\\{\"count\":(\\d+)\\}"); // "edge_followed_by":{"count":3626
  private static final Pattern USER_DESCRIPTION_PATTERN = Pattern.compile("\"description\":\"[^\"]*"); // "description":"desc"
  private static final Pattern USER_BIOGRAPHY_PATTERN = Pattern.compile("\"biography\":\"[^\"]*"); // {"biography":"bio"
  private static final Pattern USER_POSTS_COUNT_PATTERN = Pattern
      .compile("\"edge_owner_to_timeline_media\":\\{\"count\":(\\d+),"); // "edge_owner_to_timeline_media":{"count":52,
  private static final Pattern USER_IMAGE_PATTERN = Pattern.compile("\"src\":\"[^\"]*");
//  private static final String USER_STORIES_URL =
//      "https://www.instagram.com/graphql/query/?query_hash=eb1918431e946dd39bf8cf8fb870e426&variables="
//          + "{\"reel_ids\": [%s],\"precomposed_overlay\": \"False\",\"show_story_viewer_list\": \"True\",\"story_viewer_fetch_count\": 50,\"story_viewer_cursor\": \"\"}";
//  private static final Pattern USER_STORY_PATTERN = Pattern.compile(
//      "\\{\"src\":\""); // {"src":"https://scontent-frt3-1.cdninstagram.com/vp/e3cb5179952d94b173303b5fedfe717f/5D9827B5/t51.12442-15/sh0.08/e35/p640x640/71270402_596572067543731_4247509925026537721_n.jpg?_nc_ht=scontent-frt3-1.cdninstagram.com&_nc_cat=107"

  private static Map<String, String> LAST_IMG_CACHE = new HashMap<>();

  public synchronized User getUser(String userName) throws UserNotFoundException, PrivateOrNoPostsException {
    Log.i(LOG_TAG, "getUser: " + userName);
    URLConnection connection;
    long id = NOT_FOUND;
    long follows = NOT_FOUND;
    long followed_by = NOT_FOUND;
    String desc = String.valueOf(NOT_FOUND);
    String biography = String.valueOf(NOT_FOUND);
    long post_count = NOT_FOUND;
    try {
      connection = new URL(String.format(USER_FEED_URL, userName)).openConnection();
    } catch (Exception e) {
      throw new UserNotFoundException(userName);
    }
    try (Scanner scanner = new Scanner(connection.getInputStream())) {
      scanner.useDelimiter("\\Z");
      while (scanner.hasNext()) {
        String next = scanner.next();
        Log.i(LOG_TAG, "content::: " + next);
        if (id == NOT_FOUND) {
          id = parseLong(getMatch(next, USER_ID_PATTERN), "\"", 5);
        }
        if (follows == NOT_FOUND) {
          follows = parseLong(getMatch(next, FOLLOWS_PATTERN), ":", 2);
        }
        if (followed_by == NOT_FOUND) {
          followed_by = parseLong(getMatch(next, FOLLOWED_BY_PATTERN), ":", 2);
        }
        if (desc.equals(NOT_FOUND)) {
          desc = parseString(getMatch(next, USER_DESCRIPTION_PATTERN), "\"", 3);
        }
        if (biography.equals(NOT_FOUND)) {
          biography = parseString(getMatch(next, USER_BIOGRAPHY_PATTERN), "\"", 3);
        }
        if (post_count == NOT_FOUND) {
          post_count = parseLong(getMatch(next, USER_POSTS_COUNT_PATTERN), ":", 2);
        }
      }
    } catch (IOException e) {
      throw new UserNotFoundException(userName);
    }

    if (id == NOT_FOUND) {
      throw new PrivateOrNoPostsException(userName);
    }
    return new User(id, userName, R.drawable.ic_diamond, post_count, follows, followed_by, desc, biography);
  }
//  public void getUserImageUrls(String userName) {
//    Set<String> urls = getContent(String.format(USER_FEED_URL, userName), USER_IMAGE_PATTERN);
//    for (String url : urls) {
//      int start = ordinalIndexOf(url, "\"", 3);
//      try {
//        String result = url.substring(start + 1, url.length() - 1);
//        if (result.contains("480x480")) {
//          String previous = LAST_IMG_CACHE.get(userName);
//          if (previous == null) {
//            LAST_IMG_CACHE.put(userName, result);
//          } else if (!previous.equals(result)) {
//            // send email notification here
//            LAST_IMG_CACHE.put(userName, result);
//          }
//          return;
//        }
//      } catch (Exception e) {
//        continue;
//      }
//    }
//  }
//
//  private Set<String> getContent(String url, Pattern pattern) {
//    Set<String> found = new LinkedHashSet<>();
//    URLConnection connection;
//    try {
//      connection = new URL(url).openConnection();
//      Log.i(LOG_TAG, "getContent: " + url);
//      try (Scanner scanner = new Scanner(connection.getInputStream())) {
//        scanner.useDelimiter("\\Z");
//        while (scanner.hasNext()) {
//          String next = scanner.next();
//          System.out.println(next);
//          Matcher matcher = pattern.matcher(next);
//          while (matcher.find()) {
//            found.add(matcher.group());
//          }
//        }
//      }
//    } catch (MalformedURLException e) {
//      e.printStackTrace();
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    return found;
//  }
}
