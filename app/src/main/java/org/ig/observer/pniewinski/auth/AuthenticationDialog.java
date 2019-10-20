package org.ig.observer.pniewinski.auth;

import static org.ig.observer.pniewinski.SnackbarUtils.networkNotFoundSnackbar;
import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.ig.observer.pniewinski.R;
import org.ig.observer.pniewinski.exceptions.NetworkNotFound;
import org.ig.observer.pniewinski.model.Auth;
import org.ig.observer.pniewinski.network.Processor;

public class AuthenticationDialog extends Dialog {

  private String redirect_url = "";
  private String request_url = "";
  private AuthenticationListener listener;
  private WebViewClient webViewClient = new WebViewClient() {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (url.startsWith(redirect_url)) {
        view.loadUrl(url);
      }
      return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      Log.i(LOG_TAG, "onPageFinished: " + url);
      if (url.contains("?code=")) {
        Log.i(LOG_TAG, "Access code URL: " + url);
        String code = url.substring(url.indexOf("?code=") + 6);
        if (code.endsWith("#_")) {
          code = code.substring(0, code.length() - 2);
        }
        final Map<String, String> headers = new HashMap<>();
        headers.put("app_id", getContext().getResources().getString(R.string.app_id));
        headers.put("app_secret", "fcef74976434dcee521450327a6b4937");
        headers.put("grant_type", "authorization_code");
        headers.put("redirect_uri", redirect_url);
        headers.put("code", code);
        new Thread(new Runnable() {
          @Override
          public void run() {
            try {
              Auth auth = new Processor().getAccessTokenAuth("https://api.instagram.com/oauth/access_token/", headers);
              listener.onTokenReceived(auth);
            } catch (IOException e) {
              Log.w(LOG_TAG, "Unexpected error while parsing user access token response: ", e);
            } catch (NetworkNotFound networkNotFound) {
              Log.w(LOG_TAG, "Failed to fetch access token: ", networkNotFound);
              networkNotFoundSnackbar(findViewById(R.id.list_view));
            }
          }
        }).start();
        findViewById(R.id.auth_dialog).setVisibility(View.GONE);
      }
    }
  };

  public AuthenticationDialog(@NonNull Context context) {
    super(context);
  }

  public AuthenticationDialog(@NonNull Context context, AuthenticationListener listener) {
    super(context);
    setTitle("Log in");
    setCancelable(true);
    this.listener = listener;
    this.redirect_url = context.getResources().getString(R.string.redirect_url);
    String scopes = context.getResources().getString(R.string.scope);
    this.request_url = context.getResources().getString(R.string.base_url) +
        "oauth/authorize/?app_id=" +
        context.getResources().getString(R.string.app_id) +
        "&redirect_uri=" + redirect_url +
        "&response_type=code&display=touch&scope=" + scopes;
    Log.i(LOG_TAG, "Login url is: " + this.request_url);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.auth_dialog);

    WebView webView = findViewById(R.id.webView);
    webView.setWebViewClient(webViewClient);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.loadUrl(request_url);
  }

}