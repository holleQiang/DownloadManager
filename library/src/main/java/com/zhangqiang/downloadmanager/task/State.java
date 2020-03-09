package com.zhangqiang.downloadmanager.task;

import android.support.annotation.IntDef;

@IntDef({DownloadTask.STATE_IDLE,
        DownloadTask.STATE_DOWNLOADING,
        DownloadTask.STATE_PAUSE,
        DownloadTask.STATE_FAIL,
        DownloadTask.STATE_COMPLETE})
public @interface State {
}
