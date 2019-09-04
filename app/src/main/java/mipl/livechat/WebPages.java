package mipl.livechat;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

public class WebPages extends AppCompatActivity {

    WebView mWebview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_pages);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.blue)));

        mWebview = (WebView) findViewById(R.id.floorplanWeb1);

        mWebview.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                Toast.makeText(getApplicationContext(), description,
                        Toast.LENGTH_SHORT).show();
            }
        });

        mWebview.getSettings().setJavaScriptEnabled(true);
        mWebview.getSettings().setDomStorageEnabled(true);
        mWebview.getSettings().setDatabaseEnabled(true);
//        mWebview.getSettings().setDatabasePath(dbpath); //check the documentation for info about dbpath
        mWebview.getSettings().setMinimumFontSize(1);
        mWebview.getSettings().setMinimumLogicalFontSize(1);

        mWebview.loadUrl("https://mipl.co.in/privacy-policy.html");

        final ProgressDialog progressBar = new ProgressDialog(WebPages.this);
        progressBar.setMessage("Please wait...");
        progressBar.setCancelable(false);

        mWebview.setWebViewClient(new

                                          WebViewClient() {
                                              public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                                  view.loadUrl(url);
                                                  return true;
                                              }

                                              @Override
                                              public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                                  super.onPageStarted(view, url, favicon);
                                                  if (!progressBar.isShowing()) {
                                                      progressBar.show();
                                                  }
                                              }

                                              public void onPageFinished(WebView view, String url) {
                                                  if (progressBar.isShowing()) {
                                                      progressBar.dismiss();
                                                  }
                                              }

                                              public void onReceivedError(WebView view, int errorCode, String description, String
                                                      failingUrl) {
                                                  if (progressBar.isShowing()) {
                                                      progressBar.dismiss();
                                                  }
                                              }
                                          });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}