package com.zhangqiang.web.webchromeclient;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.zhangqiang.web.context.WebContext;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class WebChromeClientImpl extends WebChromeClient {

    private final WebContext webContext;
    private final ProgressBar progressBar;
    private final TextView tvTitle;

    public WebChromeClientImpl(WebContext webContext, ProgressBar progressBar, TextView tvTitle) {
        this.webContext = webContext;
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
        webContext.dispatchReceiveTitle(title);
    }

    @Override
    public void onReceivedIcon(WebView view, Bitmap icon) {
        super.onReceivedIcon(view, icon);
        webContext.dispatchReceiveIcon(icon);
    }
}
