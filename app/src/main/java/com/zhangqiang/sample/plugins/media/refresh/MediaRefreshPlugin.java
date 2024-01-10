package com.zhangqiang.sample.plugins.media.refresh;

import android.content.Context;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.manager.OnTaskAddedListener;
import com.zhangqiang.downloadmanager.plugin.SimpleDownloadPlugin;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.utils.FileUtils;

import java.io.File;
import java.util.List;

public class MediaRefreshPlugin extends SimpleDownloadPlugin {

    private final Context context;

    public MediaRefreshPlugin(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String getName() {
        return context.getResources().getString(R.string.media_file_refresh);
    }

    @Override
    protected void onApply(DownloadManager downloadManager) {
        super.onApply(downloadManager);
        downloadManager.addOnTaskAddedListener(new OnTaskAddedListener() {
            @Override
            public void onTaskAdded(List<DownloadTask> tasks) {
                for (DownloadTask task : tasks) {
                    task.addStatusChangeListener(new OnStatusChangeListener() {
                        @Override
                        public void onStatusChange(Status newStatus, Status oldStatus) {
                            if (oldStatus == Status.DOWNLOADING && newStatus == Status.SUCCESS) {
                                String saveDir = task.getSaveDir();
                                String saveFileName = task.getSaveFileName();
                                File file = new File(saveDir, saveFileName);
                                if (file.exists()) {
                                    FileUtils.refreshMediaFile(context, file);
                                }
                            }
                        }
                    });
                }
            }
        });
    }
}
