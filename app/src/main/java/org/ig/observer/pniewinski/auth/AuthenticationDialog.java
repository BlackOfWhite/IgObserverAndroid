package org.ig.observer.pniewinski.auth;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.ig.observer.pniewinski.R;

public class AuthenticationDialog extends Dialog {

  private AuthenticationListener listener;
  private WebViewClient webViewClient = new WebViewClient() {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return false;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      Log.i(LOG_TAG, "onPageFinished: " + url);
      if (url.equals("https://www.instagram.com/")) {
        new Thread(new Runnable() {
          @Override
          public void run() {
            String cookie = CookieManager.getInstance().getCookie("https://www.instagram.com");
            listener.onLoginSuccessful(cookie);
          }
        }).start();
        findViewById(R.id.auth_dialog).setVisibility(View.GONE);
      }
    }
  };

  public AuthenticationDialog(@NonNull Context context) {
    super(context);
    setTitle("Log in");
    setCancelable(true);
    this.listener = (AuthenticationListener) context;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.auth_dialog);

    WebView webView = findViewById(R.id.webView);
    webView.setWebViewClient(webViewClient);
    webView.getSettings().setJavaScriptEnabled(true);
    webView.getSettings().setDomStorageEnabled(true);
    webView.loadUrl("https://www.instagram.com/accounts/login/");
  }

}