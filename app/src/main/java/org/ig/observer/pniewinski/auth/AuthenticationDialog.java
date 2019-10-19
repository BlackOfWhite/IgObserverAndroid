package org.ig.observer.pniewinski.auth;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.ig.observer.pniewinski.R;

public class AuthenticationDialog extends Dialog {

  private static final String SCOPES = "basic"; // basic+public_content ->> only after review
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
      Log.d(LOG_TAG, "Access token URL: " + url);
      if (url.contains("access_token=")) {
        Uri uri = Uri.EMPTY.parse(url);
        String access_token = uri.getEncodedFragment();
        access_token = access_token.substring(access_token.lastIndexOf("=") + 1);
        listener.onTokenReceived(access_token);
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
    this.request_url = context.getResources().getString(R.string.base_url) +
        "oauth/authorize/?client_id=" +
        context.getResources().getString(R.string.client_id) +
        "&redirect_uri=" + redirect_url +
        "&response_type=token&display=touch&scope=" + SCOPES;
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