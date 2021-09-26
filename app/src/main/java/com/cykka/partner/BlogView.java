package com.cykka.partner;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

public class BlogView extends AppCompatActivity {

    private WebView wb;
    private String link, title;
    private TextView tvTitle;

    private class HelloWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            return false;
        }

    }
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_view);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Blog");
        actionBar.setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        link = getIntent().getStringExtra("Link");
        title = getIntent().getStringExtra("Title");

        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(title);

        wb=(WebView)findViewById(R.id.webView);
        wb.getSettings().setJavaScriptEnabled(true);
        wb.getSettings().setLoadWithOverviewMode(true);
        wb.getSettings().setUseWideViewPort(true);
        wb.getSettings().setBuiltInZoomControls(true);
        wb.getSettings().setPluginState(WebSettings.PluginState.ON);
        wb.setWebViewClient(new HelloWebViewClient());
        wb.loadUrl(link);

        //wb.setWebChromeClient(new WebChromeClient());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                ImagePickerActivity.clearCache(BlogView.this);
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
