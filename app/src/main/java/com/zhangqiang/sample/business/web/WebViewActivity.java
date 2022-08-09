package com.zhangqiang.sample.business.web;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.business.web.image.ImageClickJSI;
import com.zhangqiang.sample.ui.dialog.CreateTaskDialog;
import com.zhangqiang.sample.utils.WebViewUtils;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class WebViewActivity extends BaseActivity {

    private WebView mWebView;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar mToolbar = findViewById(R.id.m_tool_bar);
        setSupportActionBar(mToolbar);
        mWebView = findViewById(R.id.m_web_view);
        mWebView.setWebChromeClient(new WebChromeClientImpl());
        mWebView.setWebViewClient(new WebViewClientImpl());
        mWebView.setDownloadListener(new DownloadListenerImpl(getSupportFragmentManager()));
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        ImageClickJSI.attachToWebView(getSupportFragmentManager(),mWebView);
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
                if (hitTestResult != null) {
                    if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE
                            || hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                        String extra = hitTestResult.getExtra();
                        CreateTaskDialog.createAndShow(getSupportFragmentManager(),extra);
                    }
                }
                return false;
            }
        });

        String urlFromIntent = getIntent().getStringExtra(WebViewUtils.INTENT_KEY_URL);
        if(TextUtils.isEmpty(urlFromIntent)){
            urlFromIntent = "https://www.baidu.com";
        }
        mWebView.loadUrl(urlFromIntent);
    }

    @Override
    public void onBackPressed() {
        if (mWebView.canGoBack()) {
            mWebView.goBack();
            return;
        }
        super.onBackPressed();
    }
}
