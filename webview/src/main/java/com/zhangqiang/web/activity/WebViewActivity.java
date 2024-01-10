package com.zhangqiang.web.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhangqiang.common.activity.BaseActivity;
import com.zhangqiang.web.context.WebContext;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.webchromeclient.WebChromeClientImpl;
import com.zhangqiang.web.webviewclient.WebViewClientImpl;
import com.zhangqiang.webview.BuildConfig;
import com.zhangqiang.webview.databinding.ActivityWebViewBinding;


/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class WebViewActivity extends BaseActivity {

    private WebView mWebView;
    private WebActivityContext mWebContext;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG);
        }
    }

    private static final String INTENT_KEY_URL = "url";
    private static final String INTENT_KEY_WEB_ID = "web_id";

    public static Intent newIntent(Context context, String url, String id) {
        Intent intent = new Intent(context, WebViewActivity.class);
        intent.putExtra(INTENT_KEY_URL, url);
        intent.putExtra(INTENT_KEY_WEB_ID, id);
        return intent;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebContext webContext = WebManager.getInstance().getWebContext(getIntent().getStringExtra(INTENT_KEY_WEB_ID));
        if (webContext instanceof WebActivityContext) {
            mWebContext = (WebActivityContext) webContext;
        }else if(savedInstanceState != null){
            mWebContext = WebManager.getInstance().fromActivityRestore(savedInstanceState.getString(INTENT_KEY_WEB_ID),
                    savedInstanceState.getString(INTENT_KEY_URL));
        }else {
            throw new IllegalArgumentException("webContext error");
        }
        ActivityWebViewBinding mActivityWebViewBinding = ActivityWebViewBinding.inflate(getLayoutInflater());
        mWebContext.dispatchActivityCreate(this);
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
        mWebContext.dispatchWebViewCreate(mWebView);

        loadResource(savedInstanceState,"https://www.baidu.com");
    }

    private void loadResource(Bundle savedInstanceState, String defaultUrl) {
        String targetUrl;
        if (savedInstanceState != null) {
            targetUrl = savedInstanceState.getString(INTENT_KEY_URL);
        }else{
            targetUrl = getIntent().getStringExtra(INTENT_KEY_URL);
        }
        if (TextUtils.isEmpty(targetUrl)) {
            targetUrl = defaultUrl;
        }
        mWebView.loadUrl(targetUrl);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INTENT_KEY_WEB_ID, mWebContext.getId());
        outState.putString(INTENT_KEY_URL,mWebContext.getUrl());
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
        mWebContext.dispatchWebViewDestroy();
        mWebContext.dispatchActivityDestroy();
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
