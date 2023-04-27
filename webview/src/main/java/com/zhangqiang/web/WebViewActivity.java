package com.zhangqiang.web;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.zhangqiang.common.activity.BaseActivity;
import com.zhangqiang.web.export.WebInterface;
import com.zhangqiang.web.image.ImageClickMethod;
import com.zhangqiang.web.utils.WebViewUtils;
import com.zhangqiang.webview.BuildConfig;
import com.zhangqiang.webview.databinding.ActivityWebViewBinding;


/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class WebViewActivity extends BaseActivity {

    private WebView mWebView;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityWebViewBinding mActivityWebViewBinding = ActivityWebViewBinding.inflate(getLayoutInflater());
        setContentView(mActivityWebViewBinding.getRoot());
        mActivityWebViewBinding.ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        mActivityWebViewBinding.ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mWebView = mActivityWebViewBinding.mWebView;
        mWebView.setWebChromeClient(new WebChromeClientImpl(mActivityWebViewBinding.pbProgress));
        mWebView.setWebViewClient(new WebViewClientImpl());
        mWebView.setDownloadListener(new DownloadListenerImpl(getSupportFragmentManager()));
        WebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowContentAccess(true);
        settings.setAllowFileAccess(true);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setDatabaseEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        settings.setAllowFileAccessFromFileURLs(true);
        WebInterface.javaScriptInterface.registerHybridMethod(new ImageClickMethod(new ImageClickMethod.OnImageClickListener() {
            @Override
            public void onImageClick(String src) {
                Toast.makeText(WebViewActivity.this, src, Toast.LENGTH_SHORT).show();
            }
        }));
        WebInterface.javaScriptInterface.attachToWebView(mWebView);
        mWebView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
                if (hitTestResult != null) {
                    if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE
                            || hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                        String extra = hitTestResult.getExtra();
//                        CreateTaskDialog.createAndShow(getSupportFragmentManager(), extra);
                    }
                }
                return false;
            }
        });

        loadResource("https://www.baidu.com");
    }

    private void loadResource(String defaultUrl) {
        String urlFromIntent = getIntent().getStringExtra(WebViewUtils.INTENT_KEY_URL);
        if (TextUtils.isEmpty(urlFromIntent)) {
            urlFromIntent = defaultUrl;
        }
        mWebView.loadUrl(urlFromIntent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        loadResource(null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mWebView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mWebView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mWebView.destroy();
        WebInterface.javaScriptInterface.detachFromWebView();
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
