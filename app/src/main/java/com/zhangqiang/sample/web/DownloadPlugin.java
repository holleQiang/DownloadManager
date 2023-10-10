package com.zhangqiang.sample.web;

import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.WebView;

import com.zhangqiang.sample.ui.dialog.TaskCreateByLinkDialog;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.context.OnStateChangeListener;
import com.zhangqiang.web.context.State;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.web.manager.WebPlugin;

public class DownloadPlugin implements WebPlugin {
    @Override
    public void apply(WebManager webManager) {
        webManager.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addOnStateChangeListener(new OnStateChangeListener() {
                    @Override
                    public void onStateChange(State state, State oldState) {
                        if (state == State.WEB_VIEW_CREATE) {
                            WebView webView = webContext.getWebView();
                            webView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                                    if (hitTestResult != null) {
                                        if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE
                                                || hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                                            String extra = hitTestResult.getExtra();
                                            TaskCreateByLinkDialog.newInstance(extra)
                                                    .show(webContext.getActivity().getSupportFragmentManager(),
                                                            "task_create");
                                        }
                                    }
                                    return false;
                                }
                            });
                            webView.setDownloadListener(new DownloadListener() {
                                @Override
                                public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                                    TaskCreateByLinkDialog.newInstance(url)
                                            .show(webContext.getActivity().getSupportFragmentManager(),
                                                    "task_create");
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
