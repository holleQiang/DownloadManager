package com.zhangqiang.downloadmanager.task.http.okhttp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpResponse;
import com.zhangqiang.downloadmanager.task.http.HttpUtils;
import com.zhangqiang.downloadmanager.task.http.RangePart;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.MD5Utils;
import com.zhangqiang.downloadmanager.utils.OkHttpUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpDownloadTask extends DownloadTask {

    public static final String TAG = OKHttpDownloadTask.class.getSimpleName();

    private final Context context;
    private final String url;
    private final String saveDir;
    private long contentLength;
    private final int threadSize;
    private Call call;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private String fileName;
    private OnResourceInfoReadyListener onResourceInfoReadyListener;
    private long currentLength;
    private OnPartTaskCreateListener onPartTaskCreateListener;
    private List<OKHttpDownloadPartTask> mPartTasks;
    private final AtomicInteger mRunningPartTaskSize = new AtomicInteger(0);

    public OKHttpDownloadTask(Context context, String url, String saveDir, int threadSize) {
        this.context = context;
        this.url = url;
        this.saveDir = saveDir;
        this.threadSize = threadSize;
    }

    public OKHttpDownloadTask(Context context, String url, String saveDir,int threadSize,String fileName,long contentLength, List<OKHttpDownloadPartTask> partTasks) {
        this.context = context;
        this.url = url;
        this.saveDir = saveDir;
        this.threadSize = threadSize;
        this.fileName = fileName;
        this.contentLength = contentLength;
        this.mPartTasks = partTasks;
        if (mPartTasks != null && !mPartTasks.isEmpty()) {
            for (int i = 0; i < mPartTasks.size(); i++) {
                initPartTask(mPartTasks.get(i));
            }
        }
    }

    @Override
    protected void onStart() {

        if (mRunning.getAndSet(true)) {
            return;
        }
        LogUtils.i(TAG, "开始下载。。。");
        notifyStart();
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
        call = OkHttpUtils.getOkHttpClient(context).newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                setRunningFalse();
                if (!call.isCanceled()) {
                    notifyFail(new DownloadException(DownloadException.HTTP_CONNECT_FAIL, e));
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {

                HttpResponse httpResponse = new OkHttpResponse(response);
                try {
                    int responseCode = httpResponse.getResponseCode();
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
                        try {
                            doMultiThreadDownload(httpResponse);
                        } catch (DownloadException e) {
                            notifyFail(e);
                        }
                    } else if (responseCode == 200) {
                        LogUtils.i(TAG, "开始单线程下载");
                        doSingleThreadDownload(httpResponse);
                    } else {
                        notifyFail(new DownloadException(DownloadException.HTTP_RESPONSE_ERROR, "http response error:code:" + responseCode));
                    }
                } catch (IOException e) {
                    if (!call.isCanceled()) {
                        notifyFail(new DownloadException(DownloadException.WRITE_FILE_FAIL, e));
                    }
                    setRunningFalse();
                } finally {
                    httpResponse.close();
                }
            }
        });
    }

    private void doSingleThreadDownload(HttpResponse httpResponse) throws IOException {
        InputStream inputStream = httpResponse.getInputStream();
        FileUtils.writeToFileFrom(inputStream, new File(saveDir, fileName), 0, new FileUtils.WriteFileListener() {

            @Override
            public void onWriteFile(byte[] buffer, int offset, int len) {
                currentLength += len;
            }
        });
        LogUtils.i(TAG, "单线程下载完成" + saveDir);
        notifyComplete();
        setRunningFalse();
    }

    private void doMultiThreadDownload(HttpResponse httpResponse) throws DownloadException {
        RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
        LogUtils.i(TAG, saveDir + "============" + rangePart);
        if (rangePart == null) {
            throw new DownloadException(DownloadException.PARSE_PART_FAIL, "cannot parse range part");
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
                end += resetDownload;
            }
            final String savePath = new File(this.saveDir, fileName + "_" + i + "_" + threadSize).getAbsolutePath();
            OKHttpDownloadPartTask task = new OKHttpDownloadPartTask(context, url, start, start, end, savePath);
            initPartTask(task);
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

    private void initPartTask(OKHttpDownloadPartTask task) {
        task.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onStart() {
                incrementRunningPartTask();
            }

            @Override
            public void onComplete() {
                if (decrementRunningPartTask() == 0) {
                    handAllTaskFinish();
                }
            }

            @Override
            public void onFail(DownloadException e) {
                decrementRunningPartTask();
                cancelAllRunningPartTasks();
                setRunningFalse();
                notifyFail(new DownloadException(DownloadException.PART_FAIL, e));
            }

            @Override
            public void onCancel() {
                decrementRunningPartTask();
            }
        });
    }

    @Override
    protected void onCancel() {
        setRunningFalse();
        if (call != null && !call.isCanceled()) {
            call.cancel();
            call = null;
        }
        cancelAllRunningPartTasks();
        notifyCancel();
    }

    @Override
    public boolean isRunning() {
        return mRunning.get();
    }


    private void setRunningFalse() {
        mRunning.set(false);
    }

    private String makeFileName(HttpResponse httpResponse) {
        String fileName = HttpUtils.parseFileName(httpResponse);
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
        if (mPartTasks != null) {
            long length = 0;
            for (int i = 0; i < mPartTasks.size(); i++) {
                OKHttpDownloadPartTask task = mPartTasks.get(i);
                length += (task.getCurrentLength() - task.getFromPosition());
            }
            return length;
        }
        return currentLength;
    }

    public void setOnPartTaskCreateListener(OnPartTaskCreateListener onPartTaskCreateListener) {
        this.onPartTaskCreateListener = onPartTaskCreateListener;
    }

    private void cancelAllRunningPartTasks() {
        for (int i = 0; i < mPartTasks.size(); i++) {
            OKHttpDownloadPartTask task = mPartTasks.get(i);
            if (task.isRunning()) {
                task.cancel();
            }
        }
    }

    private void handAllTaskFinish() {
        try {
            mergePartFile();
            LogUtils.i(TAG, "下载完成" + saveDir);
            setRunningFalse();
            notifyComplete();
        } catch (IOException e) {
            setRunningFalse();
            notifyFail(new DownloadException(DownloadException.MERGE_PART_FAIL, e));
        }
    }


    private void mergePartFile() throws IOException {

        RandomAccessFile raf = new RandomAccessFile(new File(saveDir, fileName), "rw");
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
                } finally {
                    fis.close();
                }
            }
        } finally {
            raf.close();
        }
    }

    private int decrementRunningPartTask(){
        int runningPartTaskSize = mRunningPartTaskSize.decrementAndGet();
        if (runningPartTaskSize < 0) {
            throw new IllegalStateException("running task is smaller than 0");
        }
        return runningPartTaskSize;
    }

    private void incrementRunningPartTask(){
        if (mRunningPartTaskSize.incrementAndGet() > mPartTasks.size()) {
            throw new IllegalStateException("running task is bigger than 0");
        }
    }
}
