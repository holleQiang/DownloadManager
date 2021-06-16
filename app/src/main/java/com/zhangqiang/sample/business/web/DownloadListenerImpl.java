package com.zhangqiang.sample.business.web;

import androidx.fragment.app.FragmentManager;
import android.webkit.DownloadListener;

import com.zhangqiang.sample.ui.dialog.CreateTaskDialog;

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
        CreateTaskDialog.createAndShow(fragmentManager,url);
    }
}
