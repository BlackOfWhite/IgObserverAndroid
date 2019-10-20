package org.ig.observer.pniewinski.network;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

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
import javax.net.ssl.HttpsURLConnection;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.ig.observer.pniewinski.exceptions.ConnectionError;
import org.ig.observer.pniewinski.exceptions.NetworkNotFound;
import org.ig.observer.pniewinski.exceptions.UserNotFoundException;
import org.ig.observer.pniewinski.model.Auth;
import org.ig.observer.pniewinski.model.User;

public class Processor {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

  @SuppressWarnings("deprecation")
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

  private String sendGET(String url) throws NetworkNotFound {
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

  public User getUser(String userId, String authToken) throws UserNotFoundException, NetworkNotFound {
    String url = "https://api.instagram.com/v1/users/" + userId + "/media/recent?access_token=" + authToken;
    String contents = sendGET(url);
    return null;
  }

  public Long getUserId(String userName, String authToken) throws NetworkNotFound {
    String url = "https://api.instagram.com/v1/users/search?q=" + userName + "&access_token=" + authToken;
    String contents = sendGET(url);
    return 0L;
  }

  /**
   * Example: https://api.instagram.com/v1/self/media/recent?access_token=3032831214.9a02a8e.e506af4ba168404e9feab0275f8babc1
   */
  public String getUserName(Auth auth) throws IOException {
    String url = "https://graph.instagram.com/" + auth.getUser_id()
        + "?fields=username&access_token=" + auth.getAccess_token();
    final ObjectNode node = new ObjectMapper().readValue(new URL(url), ObjectNode.class);
    if (node.has("username")) {
      String username = node.get("username").getTextValue();
      Log.i(LOG_TAG, "User name: " + username);
      return username;
    }
    return "";
  }

  public User getUser(Auth auth) throws NetworkNotFound {
    String url = "https://graph.instagram.com/" + auth.getUser_id()
        + "?fields=id,username,biography,followers_count,follows_count,media_count&access_token=" + auth.getAccess_token();
    String contents = sendGET(url);
    return null;
  }

  public Auth getAccessTokenAuth(String url, Map<String, String> headers) throws IOException, NetworkNotFound {
    String response = sendPOST(url, headers);
    Auth auth = OBJECT_MAPPER.readValue(response, Auth.class);
    Log.i(LOG_TAG, "User auth is: " + auth);
    return auth;
  }

  public void signOut() throws NetworkNotFound {
    String url = "https://www.instagram.com/accounts/logout/";
    sendGET(url);
  }
}