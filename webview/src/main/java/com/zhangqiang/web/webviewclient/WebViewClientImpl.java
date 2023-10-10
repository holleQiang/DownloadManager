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
//        String scheme = request.getUrl().getScheme();
//        if ("baiduboxapp".equals(scheme)
//        || "baiduboxlite".equals(scheme)) {
//            return new WebResourceResponse("text/html","utf-8",null);
//        }
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
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        webContext.dispatchPageStarted(view, url, favicon);

    }



    @Override
    public void onLoadResource(WebView view, String url) {
        super.onLoadResource(view, url);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        webContext.dispatchPageFinished(view, url);
//        new Handler(webContext.looper).postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                WebInterface.javaScriptInterface.call(new RemoveElementByIDMethod("ahsdow"));
//            }
//        },1000);
    }


}
