package com.zhangqiang.web.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.zhangqiang.common.activity.BaseActivity;
import com.zhangqiang.web.WebContext;
import com.zhangqiang.web.export.WebInterface;
import com.zhangqiang.web.hybrid.ConsoleLogMonitorMethod;
import com.zhangqiang.web.hybrid.DocumentLoadMonitorMethod;
import com.zhangqiang.web.hybrid.RemoveElementByTagMethod;
import com.zhangqiang.web.image.ImageClickMethod;
import com.zhangqiang.web.log.WebLogger;
import com.zhangqiang.web.utils.WebViewUtils;
import com.zhangqiang.web.view.DownloadListenerImpl;
import com.zhangqiang.web.view.WebChromeClientImpl;
import com.zhangqiang.web.view.WebViewClientImpl;
import com.zhangqiang.webview.BuildConfig;
import com.zhangqiang.webview.databinding.ActivityWebViewBinding;

import java.util.Arrays;


/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class WebViewActivity extends BaseActivity {

    private WebView mWebView;
    private WebContext mWebContext;

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
        WebActivityContext webActivityContext = new WebActivityContext();
        webActivityContext.activity = this;
        webActivityContext.looper = Looper.myLooper();
        webActivityContext.fragmentManager = getSupportFragmentManager();
        webActivityContext.webView = mActivityWebViewBinding.mWebView;
        webActivityContext.dispatchState(WebContext.STATE_WEB_VIEW_CREATE);
        this.mWebContext = webActivityContext;
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
        mWebView.setWebViewClient(new WebViewClientImpl(this.mWebContext));
        mWebView.setDownloadListener(new DownloadListenerImpl(this.mWebContext));
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
        WebInterface.javaScriptInterface.attachToWebContext(mWebContext);
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
        WebInterface.javaScriptInterface.detachFromWebContext();
        mWebContext.dispatchState(WebContext.STATE_WEB_VIEW_DESTROY);
        mWebContext.fragmentManager = null;
        mWebContext.looper = null;
        mWebContext.webView = null;
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
