package org.ig.observer.pniewinski.network;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;
import static org.ig.observer.pniewinski.network.util.ResponseParser.getMatch;
import static org.ig.observer.pniewinski.network.util.ResponseParser.parseBoolean;
import static org.ig.observer.pniewinski.network.util.ResponseParser.parseLong;
import static org.ig.observer.pniewinski.network.util.ResponseParser.parseString;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.ig.observer.pniewinski.exceptions.ConnectionError;
import org.ig.observer.pniewinski.exceptions.NetworkNotFound;
import org.ig.observer.pniewinski.exceptions.UserNotFoundException;
import org.ig.observer.pniewinski.model.User;

public class NetworkProcessor {

  private static final String USER_FEED_URL = "https://www.instagram.com/%s/";
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
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

  public static void clearCookies(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
      Log.d(LOG_TAG, "Using clearCookies code for API >=" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
      CookieManager.getInstance().removeAllCookies(null);
      CookieManager.getInstance().flush();
    } else {
      Log.d(LOG_TAG, "Using clearCookies code for API <" + String.valueOf(Build.VERSION_CODES.LOLLIPOP_MR1));
      CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(context);
      cookieSyncMngr.startSync();
      CookieManager cookieManager = CookieManager.getInstance();
      cookieManager.removeAllCookie();
      cookieManager.removeSessionCookie();
      cookieSyncMngr.stopSync();
      cookieSyncMngr.sync();
    }
  }

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
      sslConnection.connect();
      // Headers
//      sslConnection.setRequestProperty("User-Agent", "PostmanRuntime/7.18.0");
//      sslConnection.setRequestProperty("Accept", "*/*");
//      sslConnection.setRequestProperty("Cache-Control", "no-cache");
//      sslConnection.setRequestProperty("Postman-Token", "0f4a37b5-715c-4770-8c77-5cd8025be427");
//      sslConnection.setRequestProperty("Host", "www.instagram.com");
//      sslConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
//      sslConnection.setRequestProperty("Connection", "keep-alive");
      int responseCode = sslConnection.getResponseCode();
      if (responseCode >= 400) {
        Log.i(LOG_TAG, "Got invalid response code: " + responseCode + ", response message: " + sslConnection.getResponseMessage());
        throw new ConnectionError();
      } else if (responseCode >= 300) {
        Log.i(LOG_TAG, "Got redirect response code: " + responseCode + ", response message: " + sslConnection.getResponseMessage());
        throw new ConnectionError();
      }
      String img_url = null;
      try (BufferedReader br =
          new BufferedReader(
              new InputStreamReader(sslConnection.getInputStream()))) {
        String next;
        while ((next = br.readLine()) != null) {
//          Log.i(LOG_TAG, next);
          if (next.contains("<script type=\"text/javascript\">window._sharedData = {\"config\":{\"csrf_token\"")) {
//            Log.i(LOG_TAG, "User data line was found: " + next);
            Long id = parseLong(getMatch(next, USER_ID_PATTERN), "\"", 3, 10);
            Long follows = parseLong(getMatch(next, FOLLOWS_PATTERN), ":", 2);
            Long followed_by = parseLong(getMatch(next, FOLLOWED_BY_PATTERN), ":", 2);
            String biography = parseString(getMatch(next, USER_BIOGRAPHY_PATTERN), "\"", 3, 0);
            Long post_count = parseLong(getMatch(next, USER_POSTS_COUNT_PATTERN), ":", 2);
            Boolean is_private = parseBoolean(getMatch(next, IS_PRIVATE_PATTERN), ":", 1);
            Boolean has_stories = hasPublicStories(id);
            if (id != null) {
              return new User(id, userName, img_url, post_count, follows, followed_by, biography, is_private, has_stories);
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

  private String sendGET(String url) throws IOException, ConnectionError {
    StringBuilder stringBuilder = new StringBuilder();
    HttpsURLConnection sslConnection;

    URLConnection urlConnection = new URL(url).openConnection();
    sslConnection = (HttpsURLConnection) urlConnection;
    if (sslConnection == null) {
      Log.i(LOG_TAG, "Failed to open HTTPS connection.");
      throw new ConnectionError();
    }
    sslConnection.setInstanceFollowRedirects(false);
    sslConnection.connect();
    int responseCode = sslConnection.getResponseCode();
    if (responseCode >= 400) {
      Log.i(LOG_TAG, "Got invalid response code: " + responseCode + ", response message: " + sslConnection.getResponseMessage());
      throw new ConnectionError();
    } else if (responseCode >= 300) {
      Log.i(LOG_TAG, "Got redirect response code: " + responseCode + ", response message: " + sslConnection.getResponseMessage());
      throw new ConnectionError();
    }
    try (BufferedReader br =
        new BufferedReader(
            new InputStreamReader(sslConnection.getInputStream()))) {
      String next;
      while ((next = br.readLine()) != null) {
        stringBuilder.append(next);
      }
    }
    return stringBuilder.toString();
  }

  private String sendPOST(String url, Map<String, String> headers) throws NetworkNotFound {
    HttpsURLConnection sslConnection;
    StringBuilder stringBuilder = new StringBuilder();
    try {
      URLConnection urlConnection = new URL(url).openConnection();
      sslConnection = (HttpsURLConnection) urlConnection;
      if (sslConnection == null) {
        Log.i(LOG_TAG, "Failed to open HTTPS connection.");
        throw new ConnectionError();
      }
      sslConnection.setInstanceFollowRedirects(false);
      sslConnection.setRequestMethod("POST");
      sslConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      StringBuilder postData = new StringBuilder();
      for (Map.Entry<String, String> param : headers.entrySet()) {
        if (postData.length() != 0) {
          postData.append('&');
        }
        postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
        postData.append('=');
        postData.append(URLEncoder.encode(param.getValue(), "UTF-8"));
      }
      byte[] bytes = postData.toString().getBytes("UTF-8");
      Log.i(LOG_TAG, "Input data: " + postData);
      sslConnection.setDoOutput(true);
      try (OutputStream os = sslConnection.getOutputStream()) {
        os.write(bytes, 0, bytes.length);
      }
      int responseCode = sslConnection.getResponseCode();
      if (responseCode >= 400) {
        Log.i(LOG_TAG, "Got invalid response code: " + responseCode + ", response message: " + sslConnection.getResponseMessage());
        throw new ConnectionError();
      } else {
        Log.i(LOG_TAG, "Response code: " + responseCode);
      }
      try (BufferedReader br =
          new BufferedReader(
              new InputStreamReader(sslConnection.getInputStream()))) {
        String next;
        while ((next = br.readLine()) != null) {
          stringBuilder.append(next);
        }
      }
    } catch (UnknownHostException e) {
      Log.w(LOG_TAG, "No internet connection: ", e);
      throw new NetworkNotFound();
    } catch (Exception e) {
      Log.w(LOG_TAG, "Unexpected exception: ", e);
    }
    return stringBuilder.toString();
  }

  private boolean hasPublicStories(Long userId) {
    String url = "https://www.instagram.com/graphql/query/?query_hash=aec5501414615eca36a9acf075655b1e"
        + "&variables={\"user_id\":\"" + userId + "\","
        + "\"include_reel\":true,"
        + "\"include_logged_out_extras\":true}";
    try {
      String json = sendGET(url);
      if (json.contains("has_public_story\":true")) {
        return true;
      }
    } catch (Exception e) {
      Log.w(LOG_TAG, "Unexpected exception while getting content of: " + url);
      return false;
    }
    return false;
  }

  /**
   * Example: https://api.instagram.com/v1/self/media/recent?access_token=3032831214.9a02a8e.e506af4ba168404e9feab0275f8babc1
   */
//  public String getUserName(Auth auth) throws IOException {
//    String url = "https://graph.instagram.com/" + auth.getUser_id()
//        + "?fields=username&access_token=" + auth.getAccess_token();
//    final ObjectNode node = new ObjectMapper().readValue(new URL(url), ObjectNode.class);
//    if (node.has("username")) {
//      String username = node.get("username").getTextValue();
//      Log.i(LOG_TAG, "User name: " + username);
//      return username;
//    }
//    return "";
//  }

}