package com.zhangqiang.sample.business.web.image;

import android.annotation.SuppressLint;
import androidx.fragment.app.FragmentManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.zhangqiang.sample.ui.dialog.CreateTaskDialog;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class ImageClickJSI {

    @SuppressLint("AddJavascriptInterface")
    public static void attachToWebView(FragmentManager fragmentManager,WebView webView) {
        webView.addJavascriptInterface(new ImageClickJSI(fragmentManager), "imageListener");
    }

    private final FragmentManager fragmentManager;

    public ImageClickJSI(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @JavascriptInterface
    public void openImage(String src) {
        CreateTaskDialog.createAndShow(fragmentManager,src);
    }


    public static void addImageClickListener(WebView webView) {
        webView.loadUrl("javascript:(function(){" +
                "var objs = document.getElementsByTagName(\"img\"); " +
                "for(var i=0;i<objs.length;i++)  " +
                "{"
                + "    objs[i].onclick=function()  " +
                "    {  "
                + "        window.imagelistener.openImage(this.src);  " +//通过js代码找到标签为img的代码块，设置点击的监听方法与本地的openImage方法进行连接
                "    }  " +
                "}" +
                "})()");
    }
}
