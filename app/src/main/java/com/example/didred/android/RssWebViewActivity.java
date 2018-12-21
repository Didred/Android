package com.example.didred.android;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;


public class RssWebViewActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rss_web_view);

        WebView mWebView = findViewById(R.id.rssWebView);
        String url = getIntent().getStringExtra(String.valueOf(R.string.url));
        mWebView.loadUrl(url);
    }
}
