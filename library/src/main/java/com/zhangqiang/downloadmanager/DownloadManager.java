package com.zhangqiang.downloadmanager;

import android.content.Context;
import android.net.Uri;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.TaskDao;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.OkHttpDownloadTask;

import java.util.ArrayList;
import java.util.List;

public class DownloadManager {

    private static volatile DownloadManager instance;
    private final List<DownloadTask> downloadTasks = new ArrayList<>();
    private Context mContext;
    private int partSize = 1;



    public static DownloadManager getInstance() {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                if (instance == null) {
                    instance = new DownloadManager();
                }
            }
        }
        return instance;
    }

    private DownloadManager() {

    }

    public void init(Context context) {
        mContext = context = context.getApplicationContext();
        DBManager.init(context);
        TaskDao taskDao = DBManager.getInstance().getTaskDao();
        List<TaskEntity> taskEntities = taskDao.queryAll();
        if (taskEntities == null || taskEntities.isEmpty()) {
            return;
        }
        for (int i = 0; i < taskEntities.size(); i++) {
            TaskEntity taskEntity = taskEntities.get(i);
            downloadTasks.add(createDownloadTask(taskEntity.getUrl(), taskEntity.getSaveDir()));
        }
        for (int i = 0; i < downloadTasks.size(); i++) {
            DownloadTask downloadTask = downloadTasks.get(i);
            if (downloadTask.getState() == DownloadTask.STATE_DOWNLOADING) {
                downloadTask.pause();
            }
        }
    }

    public DownloadTask download(final String url, final String saveDir) {

        final DownloadTask downloadTask = createDownloadTask(url, saveDir);
        for (int i = 0; i < downloadTasks.size(); i++) {
            DownloadTask task = downloadTasks.get(i);
            if (task.equals(downloadTask)) {
                task.start();
                return task;
            }
        }
        downloadTask.start();
        downloadTasks.add(downloadTask);
        return downloadTask;
    }

    public List<DownloadTask> getAllTask() {
        return downloadTasks;
    }

    public void deleteTask(DownloadTask downloadTask) {
        downloadTask.delete();
        downloadTasks.remove(downloadTask);
    }

    private DownloadTask createDownloadTask(final String url, String saveDir) {
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            return new OkHttpDownloadTask(url, saveDir, partSize, mContext);
        }
        return new DownloadTask(url, saveDir) {
            @Override
            protected void onStart() {
                notifyFail(new RuntimeException("unSupport url : " + url));
            }

            @Override
            protected void onPause() {

            }
        };
    }

    public int getPartSize() {
        return partSize;
    }

    public void setPartSize(int partSize) {
        this.partSize = partSize;
    }
}
