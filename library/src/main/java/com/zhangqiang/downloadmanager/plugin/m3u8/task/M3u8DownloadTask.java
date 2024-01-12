package com.zhangqiang.downloadmanager.plugin.m3u8.task;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.zhangqiang.downloadmanager.manager.ExecutorManager;
import com.zhangqiang.downloadmanager.plugin.http.okhttp.OKHttpClients;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.OnTaskFailListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.MD5Utils;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.TSInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.M3u8File;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.M3u8FileParser;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.StreamInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class M3u8DownloadTask extends DownloadTask {
    public static final String TAG = M3u8DownloadTask.class.getSimpleName();

    private final Context context;
    private final String url;
    private Call call;
    private final TSDownloadBundleFactory factory;
    private final AtomicInteger successTSDownloadTaskCount = new AtomicInteger(0);
    private List<TSDownloadBundle> tsDownloadBundles;
    private Future<?> handleAllTaskSuccessFuture;
    private static final int MAX_ACTIVE_TS_TASK_COUNT = 5;
    private final AtomicInteger activeTSTaskCount = new AtomicInteger(0);
    private M3u8ResourceInfo resourceInfo;
    private final List<OnResourceInfoReadyListener> onResourceInfoReadyListeners = new ArrayList<>();
    private final List<OnTSDownloadBundlesReadyListener> onTSDownloadBundlesReadyListeners = new ArrayList<>();
    private long currentDuration;

    public M3u8DownloadTask(String id,
                            String saveDir,
                            String targetFileName,
                            long createTime,
                            Context context,
                            String url,
                            TSDownloadBundleFactory factory) {
        super(id, saveDir, targetFileName, createTime);
        this.context = context.getApplicationContext();
        this.url = url;
        this.factory = factory;
    }

    public M3u8DownloadTask(String id,
                            String saveDir,
                            String targetFileName,
                            long createTime,
                            Status status,
                            String errorMessage,
                            long currentLength,
                            Context context,
                            String url,
                            TSDownloadBundleFactory factory,
                            List<TSDownloadBundle> tsDownloadBundles,
                            M3u8ResourceInfo resourceInfo,
                            long currentDuration) {
        super(id, saveDir, targetFileName, createTime, status, errorMessage, currentLength);
        this.context = context;
        this.url = url;
        this.factory = factory;
        this.tsDownloadBundles = tsDownloadBundles;
        this.resourceInfo = resourceInfo;
        this.currentDuration = currentDuration;
    }

    @Override
    protected void onStart() {

        if (tsDownloadBundles != null) {
            if (this.successTSDownloadTaskCount.get() == tsDownloadBundles.size()) {
                handleAllTaskSuccessFuture = ExecutorManager.getInstance().submit(new Runnable() {
                    @Override
                    public void run() {
                        handleAllTSDownloadTasksSuccess();
                    }
                });
            } else {
                startTSDownloadTasks();
            }
            return;
        }

        call = OKHttpClients.getDefault(context)
                .newCall(new Request.Builder()
                        .url(url)
                        .get()
                        .build());
        call.enqueue(new Callback() {

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody responseBody = response.body();
                if (!response.isSuccessful() || responseBody == null) {
                    dispatchFail(new RuntimeException("http response fail with code:" + response.code()));
                    return;
                }
                //解析m3u8
                M3u8File m3u8File = new M3u8FileParser().parse(responseBody.byteStream());
                List<TSInfo> tsInfoList = new ArrayList<>();
                LogUtils.i(TAG, "m3u8File===========" + m3u8File);
                //搜集ts链接
                try {
                    handleM3u8File(m3u8File, tsInfoList);
                } catch (Throwable e) {
                    dispatchFail(e);
                    return;
                }
                long totalDuration = 0;
                for (TSInfo info : tsInfoList) {
                    totalDuration += info.getDuration() * 1000;
                }
                dispatchResourceInfoReady(new M3u8ResourceInfo(totalDuration));
                if (getStatus() == Status.CANCELED) {
                    return;
                }
                //创建临时目录，命名为filename的隐藏文件
                File dir = getTempDir();
                //创建ts下载任务
                tsDownloadBundles = new ArrayList<>();
                for (TSInfo info : tsInfoList) {
                    String tsUrl = buildUrl(info.getUri());
                    String fileName = Uri.parse(tsUrl).getLastPathSegment();
                    TSDownloadBundle downloadBundle = factory.createTSDownloadBundle(dir.getAbsolutePath(), fileName, tsUrl, 0, -1, info);
                    tsDownloadBundles.add(downloadBundle);
                }
                configTSDownloadBundles(tsDownloadBundles);
                dispatchResourceInfoReady(tsDownloadBundles);
                startTSDownloadTasks();
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (getStatus() == Status.CANCELED) {
                    return;
                }
                dispatchFail(e);
            }
        });
    }

    @Override
    protected void onCancel() {
        if (call != null) {
            call.cancel();
            call = null;
        }
        cancelAllDownloadingTSDownloadTasks();
        if (handleAllTaskSuccessFuture != null) {
            handleAllTaskSuccessFuture.cancel(true);
            handleAllTaskSuccessFuture = null;
        }
    }

    private synchronized void cancelAllDownloadingTSDownloadTasks() {
        if (tsDownloadBundles != null) {
            for (TSDownloadBundle downloadBundle : tsDownloadBundles) {
                HttpPartDownloadTask downloadTask = downloadBundle.getDownloadTask();
                if (downloadTask.getStatus() == Status.DOWNLOADING) {
                    downloadTask.cancel();
                }
            }
        }
    }

    @Override
    public String getSaveFileName() {
        String saveFileName = getTargetFileName();
        if (TextUtils.isEmpty(saveFileName)) {
            saveFileName = MD5Utils.getMD5(url) + ".mp4";
        }
        return saveFileName;
    }


    private void startTSDownloadTasks() {
        if (this.activeTSTaskCount.get() != 0) {
            throw new IllegalArgumentException("activeTSTaskCount are expected zero");
        }
        for (TSDownloadBundle downloadBundle : tsDownloadBundles) {
            HttpPartDownloadTask downloadTask = downloadBundle.getDownloadTask();
            Status status = downloadTask.getStatus();
            if (status == Status.SUCCESS) {
                continue;
            }
            if (this.activeTSTaskCount.get() >= MAX_ACTIVE_TS_TASK_COUNT) {
                return;
            }
            if (status == Status.FAIL || status == Status.IDLE || status == Status.CANCELED) {
                downloadTask.start();
            } else if (status == Status.DOWNLOADING) {
                downloadTask.forceStart();
            }
        }
    }

    private synchronized void tryStartIdleTSDownloadTasks() {

        for (TSDownloadBundle downloadBundle : tsDownloadBundles) {
            if (this.activeTSTaskCount.get() >= MAX_ACTIVE_TS_TASK_COUNT) {
                return;
            }
            HttpPartDownloadTask downloadTask = downloadBundle.getDownloadTask();
            Status status = downloadTask.getStatus();
            if (status == Status.IDLE) {
                downloadTask.start();
            }
        }
    }

    private void configTSDownloadBundles(List<TSDownloadBundle> tsDownloadTasks) {

        if (tsDownloadTasks == null) {
            return;
        }
        int successCount = 0;
        for (TSDownloadBundle downloadBundle : tsDownloadTasks) {
            HttpPartDownloadTask downloadTask = downloadBundle.getDownloadTask();
            Status status = downloadTask.getStatus();
            if (status == Status.SUCCESS) {
                successCount++;
                continue;
            }
            downloadTask.addStatusChangeListener(new OnStatusChangeListener() {
                @Override
                public void onStatusChange(Status newStatus, Status oldStatus) {
                    if (newStatus == Status.SUCCESS) {
                        currentDuration += downloadBundle.getInfo().getDuration() * 1000;
                        dispatchProgressChange();

                        if (successTSDownloadTaskCount.incrementAndGet() == tsDownloadTasks.size()) {
                            LogUtils.i(TAG, "206 所有子任务完成");
                            //保证100%进度回调
                            dispatchProgressChange();
                            handleAllTSDownloadTasksSuccess();
                        }
                        tryStartIdleTSDownloadTasks();
                    } else if (newStatus == Status.FAIL) {
                        cancelAllDownloadingTSDownloadTasks();
                    }
                    if (newStatus == Status.DOWNLOADING) {
                        activeTSTaskCount.incrementAndGet();
                    } else {
                        activeTSTaskCount.decrementAndGet();
                    }
                }
            });
            downloadTask.addTaskFailListener(new OnTaskFailListener() {
                @Override
                public void onTaskFail(Throwable e) {
                    LogUtils.i(TAG, "206 子任务失败: " + downloadTask.getSaveFileName());
                    dispatchFail(new RuntimeException("子任务失败: " + e.getMessage(), e));
                }
            });
            downloadTask.addOnProgressChangeListener(new OnProgressChangeListener() {
                @Override
                public void onProgressChange() {
                    long totalLength = 0;
                    for (TSDownloadBundle downloadBundle : tsDownloadTasks) {
                        totalLength += downloadBundle.getDownloadTask().getCurrentLength();
                    }
                    dispatchCurrentLength(totalLength);
                }
            });
        }
        this.successTSDownloadTaskCount.set(successCount);
    }

    private void handleAllTSDownloadTasksSuccess() {
        try {
            LogUtils.i(TAG, "206 合并文件");
            //合并文件
            mergeTSFile();
            LogUtils.i(TAG, "206 下载成功");
            //下载成功
            dispatchSuccess();
            LogUtils.i(TAG, "206 删除临时文件");
            //删除临时文件
            deleteTempFiles();
        } catch (IOException e) {
            if (getStatus() == Status.CANCELED) {
                return;
            }
            dispatchFail(e);
            LogUtils.i(TAG, "206 出错了：" + e.getMessage());
        }
    }

    private void deleteTempFiles() {
        LogUtils.i(TAG, "删除临时文件....");
        String dir = "";
        for (TSDownloadBundle downloadBundle : tsDownloadBundles) {
            HttpPartDownloadTask downloadTask = downloadBundle.getDownloadTask();
            File file = new File(downloadTask.getSaveDir(), downloadTask.getSaveFileName());
            if (!file.delete()) {
                LogUtils.i(TAG, "删除临时文件失败");
            }
            dir = downloadTask.getSaveDir();
        }
        if (!TextUtils.isEmpty(dir)) {
            try {
                FileUtils.deleteFileOrThrow(new File(dir));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void mergeTSFile() throws IOException {
        //生成一个临时文件
        File tempFile = new File(getTempDir(), "tsFiles.txt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            for (TSDownloadBundle downloadBundle : tsDownloadBundles) {
                HttpPartDownloadTask downloadTask = downloadBundle.getDownloadTask();
                String absolutePath = new File(downloadTask.getSaveDir(), downloadTask.getSaveFileName()).getAbsolutePath();
                fos.write(String.format("file '%s'\n", absolutePath).getBytes("utf-8"));
            }
            fos.flush();
        }
        File outputFile = new File(getSaveDir(), getSaveFileName());
        String cmd = String.format("-f concat -safe 0 -i %s -c copy %s", tempFile.getAbsolutePath(), outputFile.getAbsolutePath());
        LogUtils.i(TAG, "=======cmd========" + cmd);
        int returnCode = FFmpeg.execute(cmd);
        if(returnCode != Config.RETURN_CODE_SUCCESS){
            throw new RuntimeException("merge ts file fail with code:"+returnCode);
        }
        LogUtils.i(TAG, "=======Success========");
    }

    private M3u8File downloadM3u8File(String url) throws Throwable {
        Call call = OKHttpClients.getDefault(context)
                .newCall(new Request.Builder()
                        .url(url).get()
                        .build());
        Response response = call.execute();
        ResponseBody body = response.body();
        if (!response.isSuccessful() || body == null) {
            throw new RuntimeException("http response fail with code:" + response.code());
        }
        Headers headers = response.headers();
        for (int i = 0; i < headers.size(); i++) {
            String name = headers.name(i);
            String value = headers.get(name);
            LogUtils.i(TAG, "M3u8Header===========" + name + "====" + value);
        }
        M3u8File m3u8File = new M3u8FileParser().parse(body.byteStream());
        LogUtils.i(TAG, "m3u8File===========" + m3u8File);
        return m3u8File;
    }

    private String buildUrl(String uri) {
        Uri itemUri = Uri.parse(uri);
        if (!TextUtils.isEmpty(itemUri.getScheme())) {
            return uri;
        }
        try {
            return new URL(new URL(url), uri).toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return uri;
    }

    private void handleM3u8File(M3u8File m3u8File, List<TSInfo> tsInfoList) throws Throwable {
        if (getStatus() == Status.CANCELED) {
            return;
        }
        List<StreamInfo> streamInfoList = m3u8File.getStreamInfoList();
        if (streamInfoList != null && streamInfoList.size() > 0) {
            for (StreamInfo streamInfo : streamInfoList) {
                String streamUrl = buildUrl(streamInfo.getUri());
                M3u8File childFile = downloadM3u8File(streamUrl);
                if (getStatus() == Status.CANCELED) {
                    return;
                }
                handleM3u8File(childFile, tsInfoList);
            }
        }
        List<TSInfo> infoList = m3u8File.getInfoList();
        if (infoList != null && infoList.size() > 0) {
            tsInfoList.addAll(infoList);
        }
    }

    private File getTempDir() throws IOException {
        String saveFileName = getSaveFileName();
        String dirName = saveFileName.substring(0, saveFileName.indexOf("."));
        File dir = new File(getSaveDir(), dirName);
        FileUtils.createDirIfNotExists(dir);
        return dir;
    }

    public long getCurrentDuration() {
        return currentDuration;
    }

    public String getUrl() {
        return url;
    }

    public void addOnResourceInfoReadyListener(OnResourceInfoReadyListener listener) {
        if(onResourceInfoReadyListeners.contains(listener)){
            return;
        }
        onResourceInfoReadyListeners.add(listener);
    }

    public void removeOnResourceInfoReadyListener(OnResourceInfoReadyListener listener) {
        onResourceInfoReadyListeners.remove(listener);
    }

    protected void dispatchResourceInfoReady(M3u8ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
        for (int i = onResourceInfoReadyListeners.size() - 1; i >= 0; i--) {
            onResourceInfoReadyListeners.get(i).onResourceInfoReady(resourceInfo);
        }
    }

    public M3u8ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public void addOnTSDownloadBundlesReadyListener(OnTSDownloadBundlesReadyListener listener) {
        if(onTSDownloadBundlesReadyListeners.contains(listener)){
            return;
        }
        onTSDownloadBundlesReadyListeners.add(listener);
    }

    public void removeOnTSDownloadBundlesReadyListener(OnTSDownloadBundlesReadyListener listener) {
        onTSDownloadBundlesReadyListeners.remove(listener);
    }

    protected void dispatchResourceInfoReady(List<TSDownloadBundle> bundles) {
        for (int i = onTSDownloadBundlesReadyListeners.size() - 1; i >= 0; i--) {
            onTSDownloadBundlesReadyListeners.get(i).onTSBundlesReady(bundles);
        }
    }
}
