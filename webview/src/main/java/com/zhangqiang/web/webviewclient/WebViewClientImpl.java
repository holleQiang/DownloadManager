package com.zhangqiang.web.webviewclient;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.zhangqiang.common.utils.IntentUtils;
import com.zhangqiang.web.context.WebContext;
import com.zhangqiang.web.log.WebLogger;
import com.zhangqiang.web.manager.OpenOptions;
import com.zhangqiang.web.manager.WebManager;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class WebViewClientImpl extends WebViewClient {

    private final WebContext webContext;

    public WebViewClientImpl(WebContext webContext) {
        this.webContext = webContext;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
        return super.shouldInterceptRequest(view, request);
    }

    @Nullable
    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return super.shouldInterceptRequest(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if ("http".equals(scheme) || "https".equals(scheme)) {
            return false;
        } else {
            try {
                IntentUtils.openActivityByUri(view.getContext(), uri);
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (request.isRedirect()) {
                return false;
            }
        }
        return super.shouldOverrideUrlLoading(view, request);
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        webContext.dispatchPageStarted(view, url, favicon);
    }


    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
        webContext.dispatchLoadResource(view, url);
        WebLogger.info("=====onLoadResource======"+url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        webContext.dispatchPageFinished(view, url);
    }

}
