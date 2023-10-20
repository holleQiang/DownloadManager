package com.zhangqiang.downloadmanager.plugin.http.task;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.zhangqiang.downloadmanager.manager.ExecutorManager;
import com.zhangqiang.downloadmanager.plugin.http.okhttp.OKHttpClients;
import com.zhangqiang.downloadmanager.plugin.http.response.OkHttpResponse;
import com.zhangqiang.downloadmanager.plugin.http.part.PartInfo;
import com.zhangqiang.downloadmanager.plugin.http.range.RangePart;
import com.zhangqiang.downloadmanager.plugin.http.utils.FiledSetter;
import com.zhangqiang.downloadmanager.plugin.http.utils.HttpUtils;
import com.zhangqiang.downloadmanager.speed.OnSpeedChangeListener;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.IOUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.MD5Utils;
import com.zhangqiang.downloadmanager.utils.RangePartUtils;
import com.zhangqiang.downloadmanager.task.OnSaveFileNameChangeListener;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.OnTaskFailListener;
import com.zhangqiang.downloadmanager.task.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpDownloadTask extends AbstractHttpDownloadTask {

    public static final String TAG = HttpDownloadTask.class.getSimpleName();

    private final Context context;
    private final int threadSize;
    private final HttpPartDownloadTaskFactory httpPartDownloadTaskFactory;
    private List<HttpPartDownloadTask> partDownloadTasks;
    private final AtomicInteger successPartDownloadTaskCount = new AtomicInteger(0);
    private Call downloadCall;
    private String saveFileName;
    private final List<OnHttpPartDownloadTasksReadyListener> onHttpPartDownloadTasksReadyListeners = new ArrayList<>();
    private final List<OnSaveFileNameChangeListener> onSaveFileNameChangeListeners = new ArrayList<>();
    private Future<?> handleAllTaskSuccessFuture;

    public HttpDownloadTask(String id,
                            String saveDir,
                            String targetFileName,
                            long createTime,
                            int priority,
                            String url,
                            Context context,
                            int threadSize, HttpPartDownloadTaskFactory httpPartDownloadTaskFactory) {
        super(id, saveDir, targetFileName, createTime,priority, url);
        this.context = context;
        this.threadSize = threadSize;
        this.httpPartDownloadTaskFactory = httpPartDownloadTaskFactory;
        this.saveFileName = makeSaveFileName(targetFileName, null, url, saveDir);
    }

    public HttpDownloadTask(String id,
                            String saveDir,
                            String targetFileName,
                            long createTime,
                            int priority,
                            Status status,
                            String errorMessage,
                            String url,
                            ResourceInfo resourceInfo,
                            long currentLength,
                            Context context,
                            int threadSize,
                            HttpPartDownloadTaskFactory httpPartDownloadTaskFactory,
                            List<HttpPartDownloadTask> partDownloadTasks,
                            String saveFileName) {
        super(id, saveDir, targetFileName, createTime,priority, status, errorMessage, currentLength, url, resourceInfo);
        this.context = context;
        this.threadSize = threadSize;
        this.httpPartDownloadTaskFactory = httpPartDownloadTaskFactory;
        this.partDownloadTasks = partDownloadTasks;
        this.saveFileName = saveFileName;
        if (TextUtils.isEmpty(this.saveFileName)) {
            this.saveFileName = makeSaveFileName(targetFileName, resourceInfo, url, saveDir);
        }
        configPartDownloadTasks(partDownloadTasks);
    }

    @Override
    public void forceStart() {
        LogUtils.i(TAG, "force start");
        super.forceStart();
    }

    @Override
    protected void onStart() {
        LogUtils.i(TAG, "开始下载");
        if (partDownloadTasks != null) {
            if (this.successPartDownloadTaskCount.get() == partDownloadTasks.size()) {
                handleAllTaskSuccessFuture = ExecutorManager.getInstance().submit(new Runnable() {
                    @Override
                    public void run() {
                        handleAllPartDownloadTasksSuccess();
                    }
                });
            } else {
                startPartDownloadTasks();
            }
            return;
        }
        Request.Builder builder = new Request.Builder().url(getUrl());
        HttpUtils.setRangeParams(new FiledSetter() {
            @Override
            public void setField(String key, String value) {
                builder.header(key, value);
            }
        }, getInitialLength());
        downloadCall = OKHttpClients.getDefault(context).newCall(builder.build());
        downloadCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (call.isCanceled()) {
                    return;
                }
                dispatchFail(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }
                try {
                    int code = response.code();
                    ResponseBody responseBody = response.body();
                    if ((code == 200 || code == 206) && responseBody != null) {

                        OkHttpResponse okHttpResponse = new OkHttpResponse(response);
                        String fileName = HttpUtils.parseFileName(okHttpResponse);
                        long contentLength = responseBody.contentLength();
                        MediaType mediaType = responseBody.contentType();
                        ResourceInfo resourceInfo = new ResourceInfo(fileName,
                                contentLength,
                                mediaType != null ? mediaType.toString() : null,
                                okHttpResponse.getResponseCode());
                        dispatchResourceInfoReady(resourceInfo);
                        LogUtils.i(TAG, "信息就绪..." + resourceInfo);

                        //计算文件保存名称
                        String oldSaveFileName = getSaveFileName();
                        String saveFileName = makeSaveFileName(getTargetFileName(), resourceInfo, getUrl(), getSaveDir());
                        if (!saveFileName.equals(oldSaveFileName)) {
                            dispatchSaveFileNameChange(saveFileName, oldSaveFileName);
                        }

                        if (code == 200) {
                            LogUtils.i(TAG, "200 写入文件开始");
                            performSaveFile(responseBody.byteStream());

                            //保证100%进度回调
                            dispatchProgressChange();
                            dispatchSuccess();
                            LogUtils.i(TAG, "200 下载完成");
                        } else {
                            LogUtils.i(TAG, "206 开始多线程下载,线程数量:" + threadSize);
                            RangePart rangePart = HttpUtils.parseRangePart(okHttpResponse);
                            if (rangePart == null) {
                                throw new IllegalArgumentException("parse range part fail");
                            }
                            List<PartInfo> partInfoList = RangePartUtils.toPartInfoList(rangePart, threadSize);
                            partDownloadTasks = new ArrayList<>();
                            for (PartInfo partInfo : partInfoList) {
                                File saveDir = new File(getSaveDir(), "." + getSaveFileName());
                                FileUtils.createDirIfNotExists(saveDir);
                                HttpPartDownloadTask partDownloadTask = httpPartDownloadTaskFactory.createHttpPartDownloadTask(saveDir.getAbsolutePath(),
                                        partInfo.getStart() + "_" + partInfo.getEnd() + ".tmp",
                                        getUrl(),
                                        partInfo.getStart(),
                                        partInfo.getEnd());
                                partDownloadTasks.add(partDownloadTask);
                            }
                            dispatchHttpPartDownloadTasksReady();
                            configPartDownloadTasks(partDownloadTasks);
                            startPartDownloadTasks();
                        }
                    } else {
                        dispatchFail(new IllegalStateException("http response error with code" + code + ";body null:" + (responseBody == null)));
                    }
                } catch (Throwable e) {
                    if (getStatus() != Status.CANCELED) {
                        dispatchFail(e);
                    } else {
                        throw e;
                    }
                } finally {
                    response.close();
                }
            }
        });
    }


    @Override
    protected void onCancel() {
        if (downloadCall != null && !downloadCall.isCanceled()) {
            downloadCall.cancel();
            downloadCall = null;
        }
        if (partDownloadTasks != null) {
            for (HttpPartDownloadTask partDownloadTask : partDownloadTasks) {
                if (partDownloadTask.getStatus() == Status.DOWNLOADING) {
                    partDownloadTask.cancel();
                }
            }
        }
        if (handleAllTaskSuccessFuture != null) {
            handleAllTaskSuccessFuture.cancel(true);
            handleAllTaskSuccessFuture = null;
        }
    }

    @Override
    public String getSaveFileName() {
        return saveFileName;
    }

    private static String makeSaveFileName(String targetFileName, ResourceInfo resourceInfo, String url, String saveDir) {
        String fileName = null;
        if (!TextUtils.isEmpty(targetFileName)) {
            fileName = targetFileName;
        } else {
            String resourceInfoFileName = resourceInfo != null ? resourceInfo.getFileName() : null;
            if (!TextUtils.isEmpty(resourceInfoFileName)) {
                fileName = resourceInfoFileName;
            } else {
                Uri uri = Uri.parse(url);
                if (uri != null) {
                    String lastPathSegment = uri.getLastPathSegment();
                    if (lastPathSegment != null && lastPathSegment.contains(".")) {
                        fileName = lastPathSegment;
                    }
                }
                if (TextUtils.isEmpty(fileName)) {
                    fileName = MD5Utils.getMD5(url);
                    if (resourceInfo != null) {
                        String fileNameSuffix = null;
                        String contentType = resourceInfo.getContentType();
                        if ("image/jpeg".equals(contentType)) {
                            fileNameSuffix = ".jpg";
                        } else if ("image/png".equals(contentType)) {
                            fileNameSuffix = ".png";
                        }
                        if (fileNameSuffix != null) {
                            fileName += fileNameSuffix;
                        }
                    }
                }
            }
        }
        return FileUtils.getDistinctFileName(saveDir, fileName);
    }

    public int getThreadSize() {
        return threadSize;
    }


    private void mergePartFile() throws IOException {
        List<HttpPartDownloadTask> partTasks = partDownloadTasks;
        if (partTasks == null) {
            return;
        }
        File dir = new File(getSaveDir());
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("create dir fail:" + dir.getAbsolutePath());
            }
        }
        File saveFile = new File(dir, getSaveFileName());
        FileUtils.deleteFileOrThrow(saveFile);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(saveFile, "rw");
            for (HttpPartDownloadTask partTask : partTasks) {
                raf.seek(partTask.getStartPosition());
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(new File(partTask.getSaveDir(), partTask.getSaveFileName()));
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        raf.write(buffer, 0, len);
                    }
                    if (getStatus() != Status.DOWNLOADING) {
                        throw new InterruptedIOException();
                    }
                } finally {
                    IOUtils.close(fis);
                }
            }
        } finally {
            IOUtils.close(raf);
        }
    }

    private void deletePartFiles() {
        LogUtils.i(TAG, "删除临时文件....");
        String dir = "";
        for (HttpPartDownloadTask partTask : partDownloadTasks) {
            File file = new File(partTask.getSaveDir(), partTask.getSaveFileName());
            if (!file.delete()) {
                LogUtils.i(TAG, "删除临时文件失败");
            }
            dir = partTask.getSaveDir();
        }
        if (!TextUtils.isEmpty(dir)) {
            try {
                FileUtils.deleteFileOrThrow(new File(dir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public int getPartDownloadTaskCount() {
        if (partDownloadTasks == null) {
            return 0;
        }
        return partDownloadTasks.size();
    }

    public HttpPartDownloadTask getPartDownloadTaskAt(int index) {
        if (partDownloadTasks == null || index >= partDownloadTasks.size()) {
            return null;
        }
        return partDownloadTasks.get(index);
    }

    private void configPartDownloadTasks(List<HttpPartDownloadTask> partDownloadTasks) {

        if (partDownloadTasks == null) {
            return;
        }
        int successCount = 0;
        for (HttpPartDownloadTask partDownloadTask : partDownloadTasks) {
            Status status = partDownloadTask.getStatus();
            if (status == Status.SUCCESS) {
                successCount++;
                continue;
            }
            partDownloadTask.addStatusChangeListener(new OnStatusChangeListener() {
                @Override
                public void onStatusChange(Status newStatus, Status oldStatus) {
                    if (newStatus == Status.SUCCESS) {
                        LogUtils.i(TAG, "206 子任务完成下载");

                        if (successPartDownloadTaskCount.incrementAndGet() == partDownloadTasks.size()) {
                            LogUtils.i(TAG, "206 所有子任务完成");
                            //保证100%进度回调
                            dispatchProgressChange();
                            handleAllPartDownloadTasksSuccess();
                        }
                    } else if (newStatus == Status.FAIL) {
                        for (HttpPartDownloadTask downloadTask : partDownloadTasks) {
                            if (downloadTask.getStatus() == Status.DOWNLOADING) {
                                LogUtils.i(TAG, "=====cancel downloading task====" + downloadTask.getSaveFileName());
                                downloadTask.cancel();
                            }
                        }
                    }
                }
            });
            partDownloadTask.addTaskFailListener(new OnTaskFailListener() {
                @Override
                public void onTaskFail(Throwable e) {
                    LogUtils.i(TAG, "206 子任务失败: " + partDownloadTask.getSaveFileName());
                    dispatchFail(new RuntimeException("子任务失败: " + e.getMessage(), e));
                }
            });
            partDownloadTask.addOnProgressChangeListener(new OnProgressChangeListener() {
                @Override
                public void onProgressChange() {
                    long totalLength = 0;
                    for (HttpPartDownloadTask downloadTask : partDownloadTasks) {
                        totalLength += downloadTask.getCurrentLength();
                    }
                    LogUtils.i(TAG, "onProgressChange:" + (float) totalLength / getResourceInfo().getContentLength());
                    dispatchCurrentLength(totalLength);
                }
            });
        }
        this.successPartDownloadTaskCount.set(successCount);
    }

    private void startPartDownloadTasks() {
        for (HttpPartDownloadTask partDownloadTask : partDownloadTasks) {
            Status status = partDownloadTask.getStatus();
            if (status == Status.SUCCESS) {
                continue;
            }
            if (status == Status.FAIL || status == Status.IDLE || status == Status.CANCELED) {
                partDownloadTask.start();
            } else if (status == Status.DOWNLOADING) {
                partDownloadTask.forceStart();
            }
        }
    }

    private void handleAllPartDownloadTasksSuccess() {
        try {
            LogUtils.i(TAG, "206 合并文件");
            //合并文件
            mergePartFile();
            LogUtils.i(TAG, "206 下载成功");
            //下载成功
            dispatchSuccess();
            LogUtils.i(TAG, "206 删除临时文件");
            //删除临时文件
            deletePartFiles();
        } catch (IOException e) {
            if (getStatus() == Status.CANCELED) {
                return;
            }
            dispatchFail(e);
            LogUtils.i(TAG, "206 出错了：" + e.getMessage());
        }
    }

    public void addOnHttpPartDownloadTasksReadyListener(OnHttpPartDownloadTasksReadyListener listener) {
        this.onHttpPartDownloadTasksReadyListeners.add(listener);
    }

    public void removeOnHttpPartDownloadTasksReadyListener(OnHttpPartDownloadTasksReadyListener listener) {
        this.onHttpPartDownloadTasksReadyListeners.remove(listener);
    }

    public void dispatchHttpPartDownloadTasksReady() {
        for (int i = onHttpPartDownloadTasksReadyListeners.size() - 1; i >= 0; i--) {
            onHttpPartDownloadTasksReadyListeners.get(i).onHttpPartDownloadTasksReady(new ArrayList<>(partDownloadTasks));
        }
    }

    public void addOnSaveFileNameChangeListener(OnSaveFileNameChangeListener listener) {
        onSaveFileNameChangeListeners.add(listener);
    }

    public void removeOnSaveFileNameChangeListener(OnSaveFileNameChangeListener listener) {
        onSaveFileNameChangeListeners.remove(listener);
    }

    public void dispatchSaveFileNameChange(String name, String oldName) {
        this.saveFileName = name;
        for (int i = onSaveFileNameChangeListeners.size() - 1; i >= 0; i--) {
            onSaveFileNameChangeListeners.get(i).onSaveFileNameChange(name, oldName);
        }
    }
}
