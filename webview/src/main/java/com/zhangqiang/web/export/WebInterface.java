package com.zhangqiang.web.export;

import android.content.Context;

import com.zhangqiang.web.hybrid.JavaScriptInterface;
import com.zhangqiang.web.utils.WebViewUtils;


public class WebInterface {

    public static final JavaScriptInterface javaScriptInterface = new JavaScriptInterface();
    public static final DownloadListeners downloadListeners = new DownloadListeners();
    public static final OnImageClickListeners onImageClickListeners = new OnImageClickListeners();

    public static void open(Context context, String url){
        WebViewUtils.open(context,url);
    }
}
