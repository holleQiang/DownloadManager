package com.zhangqiang.downloadmanager.task.http;

import androidx.annotation.NonNull;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.manager.DownloadExecutors;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-02
 */
public class HttpDownloadTask2 extends DownloadTask {

    public static final String TAG = HttpDownloadTask2.class.getSimpleName();

    private final String saveDir;
    private final String saveFileName;
    private final long contentLength;
    @NonNull
    private final List<HttpDownloadPartTask> mPartTasks;
    private final AtomicInteger mRunningPartTaskSize = new AtomicInteger(0);
    private final AtomicInteger mFinishedPartTaskSize = new AtomicInteger(0);
    private Future<?> mCancelFuture;

    public HttpDownloadTask2(String saveDir, String fileName, long contentLength, List<HttpDownloadPartTask> partTasks) {
        this.saveDir = saveDir;
        saveFileName = fileName;
        this.contentLength = contentLength;
        if (partTasks == null || partTasks.isEmpty()) {
            throw new IllegalArgumentException("partTasks cannot be null");
        }
        this.mPartTasks = partTasks;
        for (int i = 0; i < mPartTasks.size(); i++) {
            initPartTask(mPartTasks.get(i));
        }
    }

    @Override
    protected void onStart() {

        mCancelFuture = DownloadExecutors.executor.submit(new Runnable() {
            @Override
            public void run() {

                LogUtils.i(TAG, "开始下载。。。");
                mFinishedPartTaskSize.set(0);
                for (int i = 0; i < mPartTasks.size(); i++) {
                    mPartTasks.get(i).start();
                }
            }
        });
    }

    private void initPartTask(HttpDownloadPartTask task) {
        task.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onStart() {
                incrementRunningPartTask();
            }

            @Override
            public void onComplete() {
                decrementRunningPartTask();
                if (mFinishedPartTaskSize.incrementAndGet() == mPartTasks.size()) {
                    handAllTaskFinish();
                }
            }

            @Override
            public void onFail(DownloadException e) {
                decrementRunningPartTask();
                cancelAllRunningPartTasks();
                dispatchFail(new DownloadException(DownloadException.PART_FAIL, e));
            }

            @Override
            public void onCancel() {
                decrementRunningPartTask();
            }
        });
    }

    @Override
    protected void onCancel() {
        if (mCancelFuture != null && !mCancelFuture.isCancelled()) {
            mCancelFuture.cancel(true);
            mCancelFuture = null;
        }
        cancelAllRunningPartTasks();
    }

    @Override
    public long getCurrentLength() {
        long length = 0;
        for (int i = 0; i < mPartTasks.size(); i++) {
            HttpDownloadPartTask task = mPartTasks.get(i);
            length += task.getCurrentLength();
        }
        return length;
    }

    private void cancelAllRunningPartTasks() {
        for (int i = 0; i < mPartTasks.size(); i++) {
            HttpDownloadPartTask task = mPartTasks.get(i);
            if (task.isStarted()) {
                task.cancel();
            }
        }
    }

    private void handAllTaskFinish() {
        try {
            LogUtils.i(TAG, "合并临时文件" + saveDir);
            mergePartFile();
            LogUtils.i(TAG, "下载完成" + saveDir);
            dispatchComplete();
        } catch (Throwable e) {
            dispatchFail(new DownloadException(DownloadException.MERGE_PART_FAIL, e));
        }
    }


    private void mergePartFile() throws IOException {

        File dir = new File(saveDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("create dir fail:" + saveDir);
            }
        }
        File saveFile = new File(dir, saveFileName);
        FileUtils.deleteFileIfExists(saveFile);
        RandomAccessFile raf = new RandomAccessFile(saveFile, "rw");
        raf.setLength(contentLength);
        try {

            for (HttpDownloadPartTask partTask : mPartTasks) {
                raf.seek(partTask.getFromPosition());
                FileInputStream fis = new FileInputStream(partTask.getSavePath());
                try {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                    }
                    if (!isStarted()) {
                        throw new InterruptedIOException();
                    }
                } finally {
                    fis.close();
                }
            }
            LogUtils.i(TAG, "删除临时文件....");
            for (HttpDownloadPartTask partTask : mPartTasks) {
                File file = new File(partTask.getSavePath());
                if (!file.delete()) {
                    throw new IOException("delete part file fail:" + file.getAbsolutePath());
                }
            }
        } finally {
            raf.close();
        }
    }

    private void decrementRunningPartTask() {
        int runningPartTaskSize = mRunningPartTaskSize.decrementAndGet();
        if (runningPartTaskSize < 0) {
            throw new IllegalStateException("running task is smaller than 0");
        }
    }

    private void incrementRunningPartTask() {
        if (mRunningPartTaskSize.incrementAndGet() > mPartTasks.size()) {
            throw new IllegalStateException("running task is bigger than 0");
        }
    }

}
