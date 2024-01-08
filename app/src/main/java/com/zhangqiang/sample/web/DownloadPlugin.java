package com.zhangqiang.sample.web;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.RequiresApi;

import com.zhangqiang.sample.ui.dialog.TaskCreateByLinkDialog;
import com.zhangqiang.web.activity.WebActivityContext;
import com.zhangqiang.web.context.OnStateChangeListener;
import com.zhangqiang.web.context.State;
import com.zhangqiang.web.manager.OnOpenWebViewActivityListener;
import com.zhangqiang.web.plugin.PluginContext;
import com.zhangqiang.web.plugin.WebPlugin;

public class DownloadPlugin implements WebPlugin {
    @Override
    public void apply(PluginContext pluginContext) {
        pluginContext.addOnOpenWebViewActivityListener(new OnOpenWebViewActivityListener() {
            float x = 0;
            float y = 0;

            @Override
            public void onOpenWebActivity(WebActivityContext webContext) {
                webContext.addOnStateChangeListener(new OnStateChangeListener() {
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public void onStateChange(State state, State oldState) {

                        if (state == State.WEB_VIEW_CREATE) {
                            WebView webView = webContext.getWebView();
                            webView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    WebView.HitTestResult hitTestResult = webView.getHitTestResult();
                                    if (hitTestResult != null) {
                                        int hitType = hitTestResult.getType();
                                        if (hitType == WebView.HitTestResult.IMAGE_TYPE
                                                || hitType == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
                                            String extra = hitTestResult.getExtra();
                                            TaskCreateByLinkDialog.newInstance(extra)
                                                    .show(webContext.getActivity().getSupportFragmentManager(),
                                                            "task_create");
                                            return true;
                                        }
                                    }
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        getVideoSrcAt(webView, (int) x, (int) y, new ValueCallback<String>() {
                                            @Override
                                            public void onReceiveValue(String value) {
                                                if (TextUtils.isEmpty(value)) {
                                                    return;
                                                }
                                                String videoUrl = value.replaceAll("\"", "");
                                                if (TextUtils.isEmpty(videoUrl) || "null".equalsIgnoreCase(videoUrl)) {
                                                    return;
                                                }
                                                Uri uri = Uri.parse(videoUrl);
                                                String scheme = uri.getScheme();
                                                if ("http".equalsIgnoreCase(scheme) ||
                                                        "https".equalsIgnoreCase(scheme) ||
                                                        "ftp".equalsIgnoreCase(scheme)) {
                                                    TaskCreateByLinkDialog.newInstance(videoUrl)
                                                            .show(webContext.getActivity().getSupportFragmentManager(),
                                                                    "task_create");
                                                }
                                            }
                                        });
                                    }
                                    return false;
                                }
                            });
                            webView.setOnTouchListener(new View.OnTouchListener() {
                                @Override
                                public boolean onTouch(View v, MotionEvent event) {
                                    x = event.getX();
                                    y = event.getY();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static void getVideoSrcAt(WebView webView, int x, int y, ValueCallback<String> valueCallback) {
        float xRatio = (float) x / webView.getWidth();
        float yRatio = (float) y / webView.getHeight();
        webView.evaluateJavascript("(function () {\n" +
                "  const isElementInRegion = (element, x, y) => {\n" +
                "    let targetElement = element;\n" +
                "    while (targetElement.clientWidth <= 0 || targetElement.clientHeight <= 0) {\n" +
                "      targetElement = targetElement.parentElement;\n" +
                "    }\n" +
                "    const clientLeft = targetElement.clientLeft;\n" +
                "    const clientTop = targetElement.clientTop;\n" +
                "    return (\n" +
                "      clientLeft < x &&\n" +
                "      x < clientLeft + targetElement.clientWidth &&\n" +
                "      clientTop < y &&\n" +
                "      y < clientTop + targetElement.clientHeight\n" +
                "    );\n" +
                "  };\n" +
                "  const getVideoElementIn = (x, y) => {\n" +
                "    const videos = document.getElementsByTagName('video');\n" +
                "    if (videos && videos.length) {\n" +
                "      for (const video of videos) {\n" +
                "        if (isElementInRegion(video, x, y)) {\n" +
                "          return video;\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "    return null;\n" +
                "  };\n" +
                "  const xRatio = " + xRatio + ";\n" +
                "  const yRatio = " + yRatio + ";\n" +
                "  const x = xRatio * window.innerWidth;\n" +
                "  const y = yRatio * window.innerHeight;\n" +
                "  const video = getVideoElementIn(x, y);\n" +
                "  if (video) {\n" +
                "    return video.src;\n" +
                "  }\n" +
                "  return null;\n" +
                "})();", valueCallback);
    }
}
