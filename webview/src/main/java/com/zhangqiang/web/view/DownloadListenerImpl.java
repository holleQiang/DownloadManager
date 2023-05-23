package com.zhangqiang.web.view;

import android.webkit.DownloadListener;

import com.zhangqiang.web.WebContext;
import com.zhangqiang.web.export.WebInterface;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class DownloadListenerImpl implements DownloadListener {

    private WebContext webContext;

    public DownloadListenerImpl(WebContext webContext) {
        this.webContext = webContext;
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        WebInterface.downloadListeners.dispatchDownloadStart(webContext,url,userAgent,contentDisposition,mimetype,contentLength);
//        CreateTaskDialog.createAndShow(fragmentManager,url);
    }
}
