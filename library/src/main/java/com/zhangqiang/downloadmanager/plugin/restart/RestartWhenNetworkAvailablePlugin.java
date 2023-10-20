package com.zhangqiang.downloadmanager.plugin.restart;

import android.content.Context;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.manager.network.NetWorkManager;
import com.zhangqiang.downloadmanager.manager.network.OnAvailableChangedListener;
import com.zhangqiang.downloadmanager.plugin.SimpleDownloadPlugin;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.utils.LogUtils;

public class RestartWhenNetworkAvailablePlugin extends SimpleDownloadPlugin {

    public static final String TAG = RestartWhenNetworkAvailablePlugin.class.getSimpleName();

    private final Context context;

    public RestartWhenNetworkAvailablePlugin(Context context) {
        this.context = context;
    }

    @Override
    public void onApply(DownloadManager downloadManager) {
        super.onApply(downloadManager);
        NetWorkManager.getInstance(context).addOnAvailableChangedListener(onAvailableChangedListener);
    }

    @Override
    public void onDrop(DownloadManager downloadManager) {
        super.onDrop(downloadManager);
        NetWorkManager.getInstance(context).removeOnAvailableChangedListener(onAvailableChangedListener);
    }

    @Override
    public String getName() {
        return "从网络可用中恢复插件";
    }

    private final OnAvailableChangedListener onAvailableChangedListener = new OnAvailableChangedListener() {
        @Override
        public void onAvailableChanged(boolean available) {
            if (available) {
                DownloadManager downloadManager = getDownloadManager();
                if (downloadManager == null) {
                    return;
                }
                int taskCount = downloadManager.getTaskCount();
                for (int i = 0; i < taskCount; i++) {
                    DownloadTask task = downloadManager.getTask(i);
                    if (task.getStatus() == Status.FAIL) {
                        LogUtils.i(TAG, "=====start from net available=============");
                        task.start();
                    }
                }
            }
        }
    };
}
