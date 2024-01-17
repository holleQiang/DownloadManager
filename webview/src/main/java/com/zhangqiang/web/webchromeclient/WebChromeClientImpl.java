package com.zhangqiang.web.webchromeclient;

import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class WebChromeClientImpl extends WebChromeClient {

    private final ProgressBar progressBar;
    private final TextView tvTitle;

    public WebChromeClientImpl(ProgressBar progressBar, TextView tvTitle) {
        this.progressBar = progressBar;
        this.tvTitle = tvTitle;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        super.onProgressChanged(view, newProgress);
        progressBar.setProgress(newProgress);
        progressBar.setVisibility(newProgress != 100 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onReceivedTitle(WebView view, String title) {
        super.onReceivedTitle(view, title);
        tvTitle.setText(title);
    }
}
