package org.ig.observer.pniewinski.auth;

import static org.ig.observer.pniewinski.activities.MainActivity.LOG_TAG;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.annotation.NonNull;
import org.ig.observer.pniewinski.R;

public class AuthenticationDialog extends Dialog {

  private AuthenticationListener listener;
  private WebViewClient webViewClient = new WebViewClient() {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      return false;
    }

    /**
     * Valid session cookie example:
     * ig_cb=1; mid=XbxlnAABAAEDuolWUDSpIBpzzKi3; csrftoken=XXKkF3KINFtScwsaLQ6cCjYUfhyDfL5h;
     * shbid=10136; shbts=1572627883.9795897; ds_user_id=3032831214; sessionid=3032831214%3Awbf0jKOEOJEX9S%3A29;
     * rur=ASH; urlgen="{\"109.231.5.46\": 34525}:1iQaLx:n72ZqSIiBdsSMXXxJA3Ec9FZbJU"
     * @param view
     * @param url
     */
    @Override
    public void onPageFinished(WebView view, String url) {
      super.onPageFinished(view, url);
      Log.i(LOG_TAG, "onPageFinished: " + url);
      if (url.equals("https://www.instagram.com/")) {
        String cookie = CookieManager.getInstance().getCookie("https://www.instagram.com");
        if (cookie.contains("ds_user_id") && cookie.contains("sessionid")) {
          new Thread(new Runnable() {
            @Override
            public void run() {
              listener.onLoginSuccessful(cookie);
            }
          }).start();
        }
        findViewById(R.id.auth_dialog).setVisibility(View.GONE);
        return;
      } else if (url.contains("error=access_denied")) {
        findViewById(R.id.auth_dialog).setVisibility(View.GONE);
        return;
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