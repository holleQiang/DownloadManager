package com.zhangqiang.web.context;

import android.graphics.Bitmap;
import android.webkit.WebView;

public interface PageLoadListener {

    void onPageStarted(WebView view, String url, Bitmap favicon);

    void onPageFinished(WebView view, String url);
}
