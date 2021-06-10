package com.zhangqiang.downloadmanager.task.http;

import android.text.TextUtils;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.manager.DownloadExecutors;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.okhttp.ResourceInfo;
import com.zhangqiang.downloadmanager.task.http.range.RangePart;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.IOUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.MD5Utils;
import com.zhangqiang.downloadmanager.utils.URLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-01
 */
public abstract class HttpDownloadTask extends DownloadTask {

    public static final String TAG = HttpDownloadTask.class.getSimpleName();

    private final DownloadRequest request;
    private OnResourceInfoReadyListener onResourceInfoReadyListener;
    private long currentLength;
    private OnPartTaskCreateListener onPartTaskCreateListener;
    private List<HttpDownloadPartTask> mPartTasks;
    private final AtomicInteger mRunningPartTaskSize = new AtomicInteger(0);
    private final AtomicInteger mFinishedPartTaskSize = new AtomicInteger(0);
    private final PartTaskFactory mPartTaskFactory;
    private Future<?> mCancelFuture;

    public HttpDownloadTask(DownloadRequest request, PartTaskFactory factory) {
        this.request = request;
        this.mPartTaskFactory = factory;
    }

    @Override
    protected void onStart() {

        mCancelFuture = DownloadExecutors.executor.submit(new Runnable() {
            @Override
            public void run() {

                LogUtils.i(TAG, "开始下载。。。");
                if (mPartTasks != null && !mPartTasks.isEmpty()) {
                    mFinishedPartTaskSize.set(0);
                    for (HttpDownloadPartTask partTask : mPartTasks) {
                        partTask.start();
                    }
                    return;
                }
                try {
                    execute(new HttpResponseHandler());
                } catch (Throwable e) {
                    dispatchFail(new DownloadException(DownloadException.UNKNOWN, e));
                }
            }
        });
    }

    private class HttpResponseHandler implements ResponseReadyCallback {

        @Override
        public void onResponseReady(HttpResponse httpResponse) {
            try {
                int responseCode = httpResponse.getResponseCode();
                String fileName = null;
                if (responseCode == 206 || responseCode == 200) {
                    fileName = makeFileName(httpResponse);
                    if (onResourceInfoReadyListener != null) {
                        ResourceInfo info = new ResourceInfo();
                        info.setFileName(fileName);
                        info.setContentLength(httpResponse.getContentLength());
                        info.setContentType(httpResponse.getContentType());
                        info.setETag(HttpUtils.parseETag(httpResponse));
                        info.setLastModified(HttpUtils.parseLastModified(httpResponse));
                        onResourceInfoReadyListener.onResourceInfoReady(info);

                        LogUtils.i(TAG, "资源信息就绪。。。" + info.toString());
                    }
                }
                if (responseCode == 206) {
                    LogUtils.i(TAG, "开始多线程下载");
                    doMultiThreadDownload(httpResponse, fileName);
                } else if (responseCode == 200) {
                    LogUtils.i(TAG, "开始单线程下载");
                    doSingleThreadDownload(httpResponse, fileName);
                } else {
                    dispatchFail(new DownloadException(DownloadException.HTTP_RESPONSE_ERROR, "http response error:code:" + responseCode));
                }
            } catch (Throwable e) {
                dispatchFail(new DownloadException(DownloadException.UNKNOWN, e));
            } finally {
                IOUtils.closeSilently(httpResponse);
            }
        }
    }

    protected abstract void execute(ResponseReadyCallback callback);

    private void doSingleThreadDownload(HttpResponse httpResponse, String fileName) {
        try {
            File saveFile = new File(FileUtils.createDirIfNotExists(new File(request.getSaveDir())), fileName);
            FileUtils.deleteFileIfExists(saveFile);
            InputStream inputStream = httpResponse.getInputStream();
            FileUtils.writeToFileFrom(inputStream, saveFile, 0, new FileUtils.WriteFileListener() {

                @Override
                public void onWriteFile(byte[] buffer, int offset, int len) {
                    currentLength += len;
                }
            });
            LogUtils.i(TAG, "单线程下载完成" + request.getSaveDir());
            dispatchComplete();
        } catch (IOException e) {
            dispatchFail(new DownloadException(DownloadException.WRITE_FILE_FAIL, e));
        }
    }

    private void doMultiThreadDownload(HttpResponse httpResponse, String fileName) {
        String saveDir = request.getSaveDir();
        RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
        LogUtils.i(TAG, saveDir + "============" + rangePart);
        if (rangePart == null) {
            dispatchFail(new DownloadException(DownloadException.PARSE_PART_FAIL, "cannot parse range part"));
            return;
        }
        if (mPartTasks == null) {
            mPartTasks = new ArrayList<>();
        }
        int threadSize = request.getThreadCount();
        final long total = rangePart.getTotal();
        long eachDownload = total / threadSize;
        long resetDownload = total % threadSize;
        for (int i = 0; i < threadSize; i++) {
            final long start = i * eachDownload;
            long end = start + eachDownload;
            if (i == threadSize - 1) {
                end += resetDownload - 1;
            }
            HttpDownloadPartTask task = mPartTaskFactory.createPartTask(request.getUrl(), start, end,i,threadSize);
            initPartTask(task, fileName);
            if (onPartTaskCreateListener != null) {
                onPartTaskCreateListener.onPartTaskCreate(i, threadSize, task);
            }
            mPartTasks.add(task);
        }
        if (mPartTasks.isEmpty()) {
            dispatchFail(new DownloadException(DownloadException.PARAM_ERROR, "part task empty by error param"));
            return;
        }
        mFinishedPartTaskSize.set(0);
        for (int i = 0; i < mPartTasks.size(); i++) {
            HttpDownloadPartTask partTask = mPartTasks.get(i);
            partTask.start();
        }
    }

    private void initPartTask(HttpDownloadPartTask task, final String fileName) {
        task.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onStart() {
                incrementRunningPartTask();
            }

            @Override
            public void onComplete() {
                decrementRunningPartTask();
                if (mFinishedPartTaskSize.incrementAndGet() == mPartTasks.size()) {
                    handAllTaskFinish(fileName);
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

    private String makeFileName(HttpResponse httpResponse) {
        String fileName = request.getFileName();
        if (TextUtils.isEmpty(fileName)) {
            fileName = HttpUtils.parseFileName(httpResponse);
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = URLUtils.getFileName(request.getUrl());
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = MD5Utils.getMD5(request.getUrl());
        }
        return FileUtils.getDistinctFileName(request.getSaveDir(), fileName);
    }

    public interface OnResourceInfoReadyListener {

        void onResourceInfoReady(ResourceInfo info);
    }

    public void setOnResourceInfoReadyListener(OnResourceInfoReadyListener onResourceInfoReadyListener) {
        this.onResourceInfoReadyListener = onResourceInfoReadyListener;
    }

    public interface OnPartTaskCreateListener {

        void onPartTaskCreate(int threadIndex, int threadSize, HttpDownloadPartTask task);
    }

    @Override
    public long getCurrentLength() {
        if (mPartTasks != null && !mPartTasks.isEmpty()) {
            long length = 0;
            for (int i = 0; i < mPartTasks.size(); i++) {
                HttpDownloadPartTask task = mPartTasks.get(i);
                length += task.getCurrentLength();
            }
            return length;
        }
        return currentLength;
    }

    public void setOnPartTaskCreateListener(OnPartTaskCreateListener onPartTaskCreateListener) {
        this.onPartTaskCreateListener = onPartTaskCreateListener;
    }

    private void cancelAllRunningPartTasks() {
        if (mPartTasks == null) {
            return;
        }
        for (int i = 0; i < mPartTasks.size(); i++) {
            HttpDownloadPartTask task = mPartTasks.get(i);
            if (task.isStarted()) {
                task.cancel();
            }
        }
    }

    private void handAllTaskFinish(String fileName) {
        try {
            LogUtils.i(TAG, "合并临时文件" + request.getSaveDir());
            mergePartFile(fileName);
            LogUtils.i(TAG, "下载完成" + request.getSaveDir());
            dispatchComplete();
        } catch (IOException e) {
            dispatchFail(new DownloadException(DownloadException.MERGE_PART_FAIL, e));
        }
    }


    private void mergePartFile(String fileName) throws IOException {
        String saveDir = request.getSaveDir();
        File dir = new File(saveDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("create dir fail:" + saveDir);
            }
        }
        File saveFile = new File(dir, fileName);
        deleteFileIfExists(saveFile);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(saveFile, "rw");
            for (HttpDownloadPartTask partTask : mPartTasks) {
                raf.seek(partTask.getFromPosition());
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(partTask.getSavePath());
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                    }
                    if (!isStarted()) {
                        throw new InterruptedIOException();
                    }
                } finally {
                    IOUtils.closeSilently(fis);
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
            IOUtils.closeSilently(raf);
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

    private void deleteFileIfExists(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("delete file fail:" + file.getAbsolutePath());
            }
        }
    }

    public interface PartTaskFactory {

        HttpDownloadPartTask createPartTask(String url, long start, long end,int partIndex,int partCount);
    }

    public DownloadRequest getRequest() {
        return request;
    }
}