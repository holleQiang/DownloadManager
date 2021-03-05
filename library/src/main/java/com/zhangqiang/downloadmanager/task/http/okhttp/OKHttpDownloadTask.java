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
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpDownloadTask extends DownloadTask {

    public static final String TAG = OKHttpPartDownloadTask.class.getSimpleName();

    private final Context context;
    private final String url;
    private final String saveDir;
    private final int threadSize;
    private Call call;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private List<OKHttpPartDownloadTask> runningPartTasks = null;
    private List<OnProgressChangeListener> onProgressChangeListeners;
    private List<OnResponseReadyListener> onResponseReadyListeners;
    private String fileName;

    public OKHttpDownloadTask(Context context, String url, String saveDir, int threadSize) {
        this.context = context;
        this.url = url;
        this.saveDir = saveDir;
        this.threadSize = threadSize;
    }

    @Override
    protected void onStart() {

        if (mRunning.getAndSet(true)) {
            return;
        }
        final Request.Builder builder = new Request.Builder()
                .get()
                .url(url);
        Request request = builder.build();
        call = OkHttpUtils.getOkHttpClient(context).newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                setRunningFalse();
                if (!call.isCanceled()) {
                    notifyFail(new DownloadException(DownloadException.HTTP_CONNECT_FAIL,e));
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {

                HttpResponse httpResponse = new OkHttpResponse(response);
                fileName = makeFileName(httpResponse);
                notifyResponseReady(httpResponse);
                try {
                    int responseCode = httpResponse.getResponseCode();
                    if (responseCode == 206) {
                        if(threadSize > 1){
                            doMultiThreadDownload(httpResponse);
                        }else {
                            doSingleThreadDownload(httpResponse);
                        }
                    }else if(responseCode == 200){
                       doSingleThreadDownload(httpResponse);
                    }else {
                        notifyFail(new DownloadException(DownloadException.HTTP_RESPONSE_ERROR,"http response error:code:" + responseCode));
                    }
                } catch (IOException e) {
                    if (!call.isCanceled()) {
                        notifyFail(new DownloadException(DownloadException.WRITE_FILE_FAIL,e));
                    }
                    setRunningFalse();
                }finally {
                    httpResponse.close();
                }
            }
        });
    }

    private void doSingleThreadDownload(HttpResponse httpResponse) throws IOException {
        final long contentLength = httpResponse.getContentLength();
        InputStream inputStream = httpResponse.getInputStream();
        FileUtils.writeToFileFrom(inputStream, new File(saveDir,fileName),0, new FileUtils.WriteFileListener() {
            long current = 0;
            @Override
            public void onWriteFile(byte[] buffer, int offset, int len) {
                current += len;
                notifyProgress(0,1,current,0,contentLength,contentLength);
            }
        });
        LogUtils.i(TAG,"下载完成" + saveDir);
        notifyComplete();
        setRunningFalse();
    }

    private void notifyProgress(int threadIndex,int threadSize,long current,long start,long end,long total) {
        if (onProgressChangeListeners != null) {
            for (int i = onProgressChangeListeners.size() - 1; i >= 0; i--) {
                onProgressChangeListeners.get(i).onProgressChange(threadIndex, threadSize, current, start, end, total);
            }
        }
    }

    private void doMultiThreadDownload(HttpResponse httpResponse) {
        RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
        LogUtils.i(TAG, saveDir + "============" + rangePart);
        final long total = rangePart.getTotal();
        long eachDownload = total / threadSize;
        long resetDownload = total % threadSize;
        final List<PartFile> partFiles = new ArrayList<>();
        for (int i = 0; i < threadSize; i++) {
            final long start = i * eachDownload;
            long end = start + eachDownload;
            if(i == threadSize - 1){
                end += resetDownload;
            }
            final int threadIndex = i;
            final String savePath = this.saveDir +"_"+i+threadSize;
            partFiles.add(new PartFile(rangePart,savePath));
            final OKHttpPartDownloadTask task = new OKHttpPartDownloadTask(context, url, start, end, savePath);
            task.addDownloadListener(new DownloadListener() {
                @Override
                public void onStart() {

                }

                @Override
                public void onComplete() {
                    runningPartTasks.remove(task);
                    if (runningPartTasks.isEmpty()) {
                        try {
                            mergePartFile(partFiles);
                            LogUtils.i(TAG,"下载完成" + savePath);
                            notifyComplete();
                        } catch (IOException e) {
                           notifyFail(new DownloadException(DownloadException.MERGE_PART_FAIL,e));
                        }
                        setRunningFalse();
                    }
                }

                @Override
                public void onFail(DownloadException e) {
                    runningPartTasks.remove(task);
                    cancelAllRunningPartTasks();
                    setRunningFalse();
                }

                @Override
                public void onCancel() {

                }
            });
            task.setOnProgressChangeListener(new OKHttpPartDownloadTask.OnProgressChangeListener() {
                @Override
                public void onProgressChange(long current, long from, long to) {
                    notifyProgress(threadIndex,threadSize,current,from,to,total);
                }
            });
            if (runningPartTasks == null) {
                runningPartTasks = Collections.synchronizedList(new ArrayList<OKHttpPartDownloadTask>());
            }
            runningPartTasks.add(task);
        }
        for (int i = runningPartTasks.size() - 1; i >= 0; i--) {
            runningPartTasks.get(i).start();
        }
    }

    @Override
    protected void onCancel() {
        setRunningFalse();
        if (call != null && !call.isCanceled()) {
            call.cancel();
            call = null;
        }
        cancelAllRunningPartTasks();
    }

    @Override
    public boolean isRunning() {
        return mRunning.get();
    }

    private void setRunningFalse(){
        mRunning.set(false);
    }

    private void cancelAllRunningPartTasks(){
        if (runningPartTasks == null) {
            return;
        }
        for (int i = runningPartTasks.size() - 1; i >= 0; i--) {
            runningPartTasks.get(i).cancel();
        }
    }

    public interface OnProgressChangeListener{
        void onProgressChange(int threadIndex,int threadSize,long current,long start,long end,long total);
    }

    public void addOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        if (onProgressChangeListeners == null) {
            onProgressChangeListeners = new ArrayList<>();
        }
        if (onProgressChangeListeners.contains(onProgressChangeListener)) {
            return;
        }
        onProgressChangeListeners.add(onProgressChangeListener);
    }

    public void removeOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener){
        if (onProgressChangeListeners == null) {
            return;
        }
        onProgressChangeListeners.remove(onProgressChangeListener);
    }

    private static class PartFile{
        private final RangePart rangePart;
        private final String filePath;

        public PartFile(RangePart rangePart, String filePath) {
            this.rangePart = rangePart;
            this.filePath = filePath;
        }
    }

    private void mergePartFile(List<PartFile> partFiles) throws IOException {

        RandomAccessFile raf = new RandomAccessFile(new File(saveDir,fileName),"rw");
        try {

            for (PartFile partFile : partFiles) {
                RangePart rangePart = partFile.rangePart;
                raf.seek(rangePart.getStart());
                FileInputStream fis = new FileInputStream(partFile.filePath);
                try {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer))!=-1){
                        raf.write(buffer,0,len);
                    }
                }finally {
                    fis.close();
                }
            }
        }finally {
            raf.close();
        }
    }

    public interface OnResponseReadyListener{
        void onResponseReady(HttpResponse response);
    }

    public void addOnResponseReadyListener(OnResponseReadyListener onResponseReadyListener){
        if (onResponseReadyListeners == null) {
            onResponseReadyListeners = new ArrayList<>();
        }
        if (onResponseReadyListeners.contains(onResponseReadyListener)) {
            return;
        }
        onResponseReadyListeners.add(onResponseReadyListener);
    }

    public void removeOnResponseReadyListener(OnResponseReadyListener onResponseReadyListener){
        if (onResponseReadyListeners == null) {
            return;
        }
        onResponseReadyListeners.remove(onResponseReadyListener);
    }

    private void notifyResponseReady(HttpResponse response){
        if (onResponseReadyListeners== null) {
            return;
        }
        for (int i = onResponseReadyListeners.size() - 1; i >= 0; i--) {
            onResponseReadyListeners.get(i).onResponseReady(response);
        }
    }

    private String makeFileName(HttpResponse httpResponse){
        String fileName = HttpUtils.parseFileName(httpResponse);
        if (TextUtils.isEmpty(fileName)) {
            fileName = MD5Utils.getMD5(url);
        }
        return FileUtils.getDistinctFileName(saveDir, fileName);
    }

    public String getFileName() {
        return fileName;
    }
}
