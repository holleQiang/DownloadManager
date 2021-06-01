package com.zhangqiang.downloadmanager.task.http.okhttp;

import android.content.Context;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.manager.DownloadExecutors;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpResponse;
import com.zhangqiang.downloadmanager.task.http.HttpUtils;
import com.zhangqiang.downloadmanager.task.http.range.RangePart;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.IOUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.MD5Utils;
import com.zhangqiang.downloadmanager.utils.OKHttpUtils;

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

import okhttp3.Call;
import okhttp3.Request;

public class OKHttpDownloadTask extends DownloadTask {

    public static final String TAG = OKHttpDownloadTask.class.getSimpleName();

    private final Context context;
    private final String url;
    private final String saveDir;
    private long contentLength;
    private final int threadSize;
    private Call call;
    private final String saveFileName;
    private OnResourceInfoReadyListener onResourceInfoReadyListener;
    private long currentLength;
    private OnPartTaskCreateListener onPartTaskCreateListener;
    private List<OKHttpDownloadPartTask> mPartTasks;
    private final AtomicInteger mRunningPartTaskSize = new AtomicInteger(0);
    private final AtomicInteger mFinishedPartTaskSize = new AtomicInteger(0);
    private Future<?> mCancelFuture;

    public OKHttpDownloadTask(Context context, String url, String saveDir, int threadSize, String saveFileName) {
        this.context = context;
        this.url = url;
        this.saveDir = saveDir;
        this.threadSize = threadSize;
        this.saveFileName = saveFileName;
    }

    public OKHttpDownloadTask(Context context, String url, String saveDir, int threadSize, String fileName, long contentLength, List<OKHttpDownloadPartTask> partTasks) {
        this.context = context;
        this.url = url;
        this.saveDir = saveDir;
        this.threadSize = threadSize;
        this.saveFileName = fileName;
        this.contentLength = contentLength;
        this.mPartTasks = partTasks;
        if (mPartTasks != null && !mPartTasks.isEmpty()) {
            for (int i = 0; i < mPartTasks.size(); i++) {
                initPartTask(mPartTasks.get(i), fileName);
            }
        }
    }

    @Override
    protected void onStart() {
        mCancelFuture = DownloadExecutors.executor.submit(new RealTask());
    }

    private class RealTask implements Runnable {

        @Override
        public void run() {
            mFinishedPartTaskSize.set(0);
            LogUtils.i(TAG, "开始下载。。。");
            if (mPartTasks != null && !mPartTasks.isEmpty()) {
                for (int i = 0; i < mPartTasks.size(); i++) {
                    mPartTasks.get(i).start();
                }
                return;
            }
            final Request.Builder builder = new Request.Builder()
                    .get()
                    .url(url);
            HttpUtils.setRangeParams(new OkHttpFiledSetter(builder), 0);
            Request request = builder.build();
            call = OKHttpUtils.getOkHttpClient(context).newCall(request);
            HttpResponse httpResponse;
            try {
                httpResponse = new OkHttpResponse(call.execute());
            } catch (IOException e) {
                dispatchFail(new DownloadException(DownloadException.HTTP_CONNECT_FAIL, e));
                return;
            }
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

    private void doSingleThreadDownload(HttpResponse httpResponse, String fileName) {
        try {
            currentLength = 0;
            File saveFile = new File(saveDir, fileName);
            if (saveFile.exists()) {
                if (!saveFile.delete()) {
                    throw new IOException("delete file fail:" + saveFile.getAbsolutePath());
                }
            }
            InputStream inputStream = httpResponse.getInputStream();
            FileUtils.writeToFileFrom(inputStream, saveFile, 0, new FileUtils.WriteFileListener() {

                @Override
                public void onWriteFile(byte[] buffer, int offset, int len) {
                    currentLength += len;
                }
            });
            LogUtils.i(TAG, "单线程下载完成" + saveDir);
            dispatchComplete();
        } catch (IOException e) {
            dispatchFail(new DownloadException(DownloadException.WRITE_FILE_FAIL, e));
        }
    }

    private void doMultiThreadDownload(HttpResponse httpResponse, String fileName) {
        RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
        LogUtils.i(TAG, saveDir + "============" + rangePart);
        if (rangePart == null) {
            dispatchFail(new DownloadException(DownloadException.PARSE_PART_FAIL, "cannot parse range part"));
            return;
        }
        if (mPartTasks == null) {
            mPartTasks = new ArrayList<>();
        }
        final long total = rangePart.getTotal();
        long eachDownload = total / threadSize;
        long resetDownload = total % threadSize;
        for (int i = 0; i < threadSize; i++) {
            final long start = i * eachDownload;
            long end = start + eachDownload;
            if (i == threadSize - 1) {
                end += resetDownload - 1;
            }
            final String savePath = new File(this.saveDir, fileName + "_" + i + "_" + threadSize).getAbsolutePath();
            OKHttpDownloadPartTask task = new OKHttpDownloadPartTask(context, url, start, 0, end, savePath);
            initPartTask(task, fileName);
            if (onPartTaskCreateListener != null) {
                onPartTaskCreateListener.onPartTaskCreate(i, threadSize, task);
            }
            mPartTasks.add(task);
        }
        for (int i = 0; i < mPartTasks.size(); i++) {
            OKHttpDownloadPartTask partTask = mPartTasks.get(i);
            partTask.start();
        }
    }

    private void initPartTask(OKHttpDownloadPartTask task, final String fileName) {
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
        if (call != null && !call.isCanceled()) {
            call.cancel();
            call = null;
        }
        cancelAllRunningPartTasks();
    }

    private String makeFileName(HttpResponse httpResponse) {
        String fileName = saveFileName;
        if (TextUtils.isEmpty(fileName)) {
            fileName = HttpUtils.parseFileName(httpResponse);
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = MD5Utils.getMD5(url);
        }
        return FileUtils.getDistinctFileName(saveDir, fileName);
    }

    public interface OnResourceInfoReadyListener {

        void onResourceInfoReady(ResourceInfo info);
    }

    public void setOnResourceInfoReadyListener(OnResourceInfoReadyListener onResourceInfoReadyListener) {
        this.onResourceInfoReadyListener = onResourceInfoReadyListener;
    }

    public interface OnPartTaskCreateListener {

        void onPartTaskCreate(int threadIndex, int threadSize, OKHttpDownloadPartTask task);
    }

    @Override
    public long getCurrentLength() {
        if (mPartTasks != null && !mPartTasks.isEmpty()) {
            long length = 0;
            for (int i = 0; i < mPartTasks.size(); i++) {
                OKHttpDownloadPartTask task = mPartTasks.get(i);
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
            OKHttpDownloadPartTask task = mPartTasks.get(i);
            if (task.isStarted()) {
                task.cancel();
            }
        }
    }

    private void handAllTaskFinish(String fileName) {
        try {
            LogUtils.i(TAG, "合并临时文件" + saveDir);
            mergePartFile(fileName);
            LogUtils.i(TAG, "下载完成" + saveDir);
            dispatchComplete();
        } catch (IOException e) {
            dispatchFail(new DownloadException(DownloadException.MERGE_PART_FAIL, e));
        }
    }


    private void mergePartFile(String fileName) throws IOException {

        File dir = new File(saveDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("create dir fail:" + saveDir);
            }
        }
        File saveFile = new File(dir, fileName);
        deleteFileIfExists(saveFile);
        RandomAccessFile raf = new RandomAccessFile(saveFile, "rw");
        raf.setLength(contentLength);
        try {

            for (OKHttpDownloadPartTask partTask : mPartTasks) {
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
            for (OKHttpDownloadPartTask partTask : mPartTasks) {
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

    private void deleteFileIfExists(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("delete file fail:" + file.getAbsolutePath());
            }
        }
    }
}
