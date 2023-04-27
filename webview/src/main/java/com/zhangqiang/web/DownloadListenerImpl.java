package com.zhangqiang.web;

import android.webkit.DownloadListener;

import androidx.fragment.app.FragmentManager;

import com.zhangqiang.web.export.WebInterface;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-11
 */
public class DownloadListenerImpl implements DownloadListener {

    private FragmentManager fragmentManager;

    public DownloadListenerImpl(FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        WebInterface.downloadListeners.dispatchDownloadStart(url,userAgent,contentDisposition,mimetype,contentLength);
//        CreateTaskDialog.createAndShow(fragmentManager,url);
    }
}
