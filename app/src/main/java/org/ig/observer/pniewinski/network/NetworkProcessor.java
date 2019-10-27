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
import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Pattern;
import javax.net.ssl.HttpsURLConnection;
import org.codehaus.jackson.JsonNode;
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

  public User getUser(String userName, String cookie) throws UserNotFoundException, NetworkNotFound, ConnectionError {
    try {
      User jsonUser = getUserFromJson(userName, cookie);
      Log.i(LOG_TAG, "Json user: " + jsonUser);
      return jsonUser;
    } catch (ConnectionError e) {
      throw e;
    } catch (Exception e) {
      Log.i(LOG_TAG, "Failed to fetch data for user: " + userName + ". Attempt to scrap html page.");
      return getUserFromHtml(userName, cookie);
    }
  }

  private User getUserFromJson(String userName, String cookie) throws NetworkNotFound, UserNotFoundException, ConnectionError {
    try {
      final String json = sendGET("https://www.instagram.com/" + userName + "/?__a=1", cookie);
      JsonNode jsonNode = OBJECT_MAPPER.readTree(json).get("graphql").get("user");
      Long id = Long.parseLong(jsonNode.get("id").getTextValue());
      Long follows = jsonNode.get("edge_follow").get("count").getLongValue();
      Long followed_by = jsonNode.get("edge_followed_by").get("count").getLongValue();
      String biography = jsonNode.get("biography").getTextValue();
      Long post_count = jsonNode.get("edge_owner_to_timeline_media").get("count").getLongValue();
      Boolean is_private = jsonNode.get("is_private").getBooleanValue();
      String img_url = jsonNode.get("profile_pic_url").getTextValue();
      if (id != null) {
        Boolean has_stories = hasPublicStories(id, cookie);
        return new User(id, userName, img_url, post_count, follows, followed_by, biography, is_private, has_stories);
      } else {
        Log.w(LOG_TAG, "getUser: " + userName + ", no patterns found in response");
        throw new UserNotFoundException(userName);
      }
    } catch (UnknownHostException e) {
      Log.w(LOG_TAG, "getUser: " + userName, e);
      throw new NetworkNotFound();
    } catch (ConnectionError e) {
      throw e;
    } catch (Exception e) {
      Log.w(LOG_TAG, "getUser: " + userName, e);
      throw new UserNotFoundException(userName);
    }
  }

  private User getUserFromHtml(String userName, String cookie) throws NetworkNotFound, UserNotFoundException {
    String url = String.format(USER_FEED_URL, userName);
    String content = null;
    try {
      content = sendGET(url, cookie);
    } catch (UnknownHostException e) {
      Log.w(LOG_TAG, "getUser: " + userName, e);
      throw new NetworkNotFound();
    } catch (Exception e) {
      Log.w(LOG_TAG, "getUser: " + userName, e);
      throw new UserNotFoundException(userName);
    }

    int indexStart = content.indexOf("<script type=\"text/javascript\">window._sharedData = {\"config\":{\"csrf_token\"");
    String match = content.substring(indexStart);
    Long id = parseLong(getMatch(match, USER_ID_PATTERN), "\"", 3, 10);
    Long follows = parseLong(getMatch(match, FOLLOWS_PATTERN), ":", 2);
    Long followed_by = parseLong(getMatch(match, FOLLOWED_BY_PATTERN), ":", 2);
    String biography = parseString(getMatch(match, USER_BIOGRAPHY_PATTERN), "\"", 3, 0);
    Long post_count = parseLong(getMatch(match, USER_POSTS_COUNT_PATTERN), ":", 2);
    Boolean is_private = parseBoolean(getMatch(match, IS_PRIVATE_PATTERN), ":", 1);
    Boolean has_stories = hasPublicStories(id, cookie);
    if (id != null) {
      String img_url = parseString(getMatch(content, PROFILE_IMG_URL_PATTERN), "\"", 3, 0);
      return new User(id, userName, img_url, post_count, follows, followed_by, biography, is_private, has_stories);
    } else {
      Log.w(LOG_TAG, "getUser: " + userName + ", no patterns found in response");
      throw new UserNotFoundException(userName);
    }
  }

  private String sendGET(String url, String cookie) throws IOException, ConnectionError {
    Log.i(LOG_TAG, "Sending GET request for URL: " + url + "\nCookie: " + cookie);
    StringBuilder stringBuilder = new StringBuilder();
    HttpsURLConnection sslConnection;

    URLConnection urlConnection = new URL(url).openConnection();
    sslConnection = (HttpsURLConnection) urlConnection;
    if (sslConnection == null) {
      Log.i(LOG_TAG, "Failed to open HTTPS connection.");
      throw new ConnectionError(0);
    }
    if (cookie != null) {
      sslConnection.setRequestProperty("cookie", cookie);
    }
    sslConnection.setInstanceFollowRedirects(false);
    sslConnection.connect();
    int responseCode = sslConnection.getResponseCode();
    if (responseCode >= 400) {
      Log.i(LOG_TAG, "Got invalid response code: " + responseCode + ", response message: " + sslConnection.getResponseMessage());
      throw new ConnectionError(responseCode);
    } else if (responseCode >= 300) {
      Log.i(LOG_TAG, url + " :: Got redirect response code: " + responseCode + ", response message: " + sslConnection.getResponseMessage());
      throw new ConnectionError(responseCode);
    }
    try (BufferedReader br = new BufferedReader(new InputStreamReader(sslConnection.getInputStream()))) {
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
        throw new ConnectionError(0);
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
        throw new ConnectionError(responseCode);
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

  private boolean hasPublicStories(Long userId, String cookie) {
    String url = "https://www.instagram.com/graphql/query/?query_hash=aec5501414615eca36a9acf075655b1e"
        + "&variables={\"user_id\":\"" + userId + "\","
        + "\"include_reel\":true,"
        + "\"include_logged_out_extras\":true}";
    try {
      String json = sendGET(url, cookie);
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
   * https://www.instagram.com/graphql/query/?query_hash=5da4b106f3e821421ea90356bb98d226&variables={"reel_ids":["13008672336"],"precomposed_overlay":false,"story_viewer_fetch_count":50}
   */
  private ArrayList<String> getUserStoriesUrls() {
    //https://www.instagram.com/graphql/query/?query_hash=5da4b106f3e821421ea90356bb98d226&variables={"reel_ids":["13008672336"],"precomposed_overlay":false,"story_viewer_fetch_count":50}
    return null;
  }
}