package com.zhangqiang.sample.business.web;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class WebChromeClientImpl extends WebChromeClient {

    private final ProgressBar progressBar;

    public WebChromeClientImpl(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        progressBar.setProgress(newProgress);
        progressBar.setVisibility(newProgress != 100 ? View.VISIBLE : View.GONE);
    }
}
