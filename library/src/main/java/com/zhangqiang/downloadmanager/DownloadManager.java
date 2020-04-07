package com.zhangqiang.downloadmanager;

import android.content.Context;
import android.net.Uri;

import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.db.dao.TaskDao;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.OkHttpDownloadTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DownloadManager {

    private static volatile DownloadManager instance;
    private final Map<Long, DownloadTask> downloadTasks = new HashMap<>();
    private Context mContext;
    private int partSize = 1;
    private int maxRunningTaskCount = 2;


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
            DownloadTask downloadTask = createDownloadTask(taskEntity);
            downloadTasks.put(downloadTask.getId(), downloadTask);
        }
        for (Map.Entry<Long, DownloadTask> entry : downloadTasks.entrySet()) {
            DownloadTask downloadTask = entry.getValue();
            if (downloadTask.getState() == DownloadTask.STATE_DOWNLOADING) {
                downloadTask.pause();
            }
        }
    }

    public DownloadTask download(final String url, final String saveDir) {

        final DownloadTask downloadTask = createDownloadTask(url, saveDir);
        downloadTask.start();
        downloadTasks.put(downloadTask.getId(), downloadTask);
        return downloadTask;
    }

    public List<DownloadTask> getAllTask() {
        List<DownloadTask> downloadTaskList = new ArrayList<>();
        for (Map.Entry<Long, DownloadTask> entry : downloadTasks.entrySet()) {
            downloadTaskList.add(entry.getValue());
        }
        return downloadTaskList;
    }

    public void deleteTask(DownloadTask downloadTask, boolean deleteFile) {
        downloadTask.delete(deleteFile);
        downloadTasks.remove(downloadTask.getId());
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

    private DownloadTask createDownloadTask(final TaskEntity taskEntity) {
        String url = taskEntity.getUrl();
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
            return new OkHttpDownloadTask(taskEntity, partSize, mContext);
        }
        return new DownloadTask(taskEntity) {
            @Override
            protected void onStart() {
                notifyFail(new RuntimeException("unSupport url : " + taskEntity.getUrl()));
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
