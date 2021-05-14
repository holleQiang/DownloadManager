package com.zhangqiang.downloadmanager.task.ftp;

import com.zhangqiang.downloadmanager.task.DownloadTask;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-05-13
 */
public class FTPDownloadTask extends DownloadTask {
    @Override
    protected void onStart() {

    }

    @Override
    protected void onCancel() {

    }

    @Override
    public long getCurrentLength() {
        return 0;
    }
}
