package org.ig.observer.pniewinski.network;

import static org.codehaus.jackson.map.DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import javax.net.ssl.HttpsURLConnection;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.ig.observer.pniewinski.exceptions.ConnectionError;
import org.ig.observer.pniewinski.exceptions.NetworkNotFound;
import org.ig.observer.pniewinski.exceptions.UserNotFoundException;
import org.ig.observer.pniewinski.model.User;
import org.ig.observer.pniewinski.model.own.UserOwn;

public class Processor {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().configure(Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false);

  private String getContents(String url) throws NetworkNotFound {
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

  public User getUser(String userId, String authToken) throws UserNotFoundException, NetworkNotFound {
    String url = "https://api.instagram.com/v1/users/" + userId + "/media/recent?access_token=" + authToken;
    String contents = getContents(url);
    return null;
  }

  public Long getUserId(String userName, String authToken) throws NetworkNotFound {
    String url = "https://api.instagram.com/v1/users/search?q=" + userName + "&access_token=" + authToken;
    String contents = getContents(url);
    return 0L;
  }

  /**
   * Example: https://api.instagram.com/v1/self/media/recent?access_token=3032831214.9a02a8e.e506af4ba168404e9feab0275f8babc1
   */
  public UserOwn getOwn(String authToken) throws IOException {
    String url = "https://api.instagram.com/v1/users/self/?access_token=" + authToken;
    UserOwn userOwn = OBJECT_MAPPER.readValue(new URL(url), UserOwn.class);
    Log.i(LOG_TAG, "User own: " + userOwn);
    return userOwn;
  }

  public User getOwnMedia(String authToken) throws NetworkNotFound {
    String url = "https://api.instagram.com/v1/users/self/media/recent/?access_token=" + authToken;
    String contents = getContents(url);
    return null;
  }
}