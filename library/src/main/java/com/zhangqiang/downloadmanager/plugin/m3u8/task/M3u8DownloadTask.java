package com.zhangqiang.downloadmanager.plugin.m3u8.task;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.zhangqiang.downloadmanager.manager.ExecutorManager;
import com.zhangqiang.downloadmanager.plugin.http.okhttp.OKHttpClients;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.M3u8FileEncoder;
import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.KeyInfo;
import com.zhangqiang.downloadmanager.plugin.m3u8.utils.Utils;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
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
    private static final int MAX_ACTIVE_TS_TASK_COUNT = 3;
    private final AtomicInteger activeTSTaskCount = new AtomicInteger(0);
    private M3u8ResourceInfo resourceInfo;
    private final List<OnResourceInfoReadyListener> onResourceInfoReadyListeners = new ArrayList<>();
    private final List<OnTSDownloadBundlesReadyListener> onTSDownloadBundlesReadyListeners = new ArrayList<>();
    private float currentDuration;
    private final AtomicBoolean tsDownloadTaskFail = new AtomicBoolean(false);

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
                            float currentDuration) {
        super(id, saveDir, targetFileName, createTime, status, errorMessage, currentLength);
        this.context = context;
        this.url = url;
        this.factory = factory;
        this.tsDownloadBundles = tsDownloadBundles;
        this.resourceInfo = resourceInfo;
        this.currentDuration = currentDuration;
        configTSDownloadBundles(tsDownloadBundles);
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

                try {
                    ResponseBody responseBody = response.body();
                    if (!response.isSuccessful() || responseBody == null) {
                        LogUtils.i(TAG, "====dispatchFail===3====");
                        dispatchFail(new RuntimeException("http response fail with code:" + response.code()));
                        return;
                    }
                    //解析m3u8
                    M3u8File m3u8File = new M3u8FileParser().parse(responseBody.byteStream());
                    LogUtils.i(TAG, "m3u8File 解析成功：" + m3u8File);
                    if (getStatus() == Status.CANCELED) {
                        return;
                    }
                    List<StreamInfo> streamInfoList = m3u8File.getStreamInfoList();
                    if (streamInfoList != null && streamInfoList.size() > 0) {
                        //多码率资源，默认下载最大码率
                        long maxBandWidth = -1;
                        StreamInfo targetStreamInfo = null;
                        for (StreamInfo streamInfo : streamInfoList) {
                            long bandWidth = streamInfo.getBandWidth();
                            if (bandWidth > maxBandWidth) {
                                maxBandWidth = bandWidth;
                                targetStreamInfo = streamInfo;
                            }
                        }
                        if (targetStreamInfo == null) {
                            dispatchFail(new RuntimeException("cannot choose stream"));
                            return;
                        }
                        //获取码率信息
                        m3u8File = downloadM3u8File(targetStreamInfo);
                        LogUtils.i(TAG, "获取码率信息：" + targetStreamInfo + ";info:" + m3u8File);
                    }

                    //搜集ts链接
                    List<TSInfo> tsInfoList = m3u8File.getInfoList();
                    LogUtils.i(TAG, "获取ts文件成功，文件数量：" + tsInfoList.size());
                    if (getStatus() == Status.CANCELED) {
                        return;
                    }
                    //资源信息ready
                    float totalDuration = 0;
                    for (TSInfo info : tsInfoList) {
                        totalDuration += info.getDuration();
                    }
                    M3u8ResourceInfo resourceInfo = new M3u8ResourceInfo(totalDuration, m3u8File);
                    LogUtils.i(TAG, "资源信息就绪：" + resourceInfo);
                    dispatchResourceInfoReady(resourceInfo);
                    if (getStatus() == Status.CANCELED) {
                        return;
                    }
                    //下载key文件
                    KeyInfo keyInfo = m3u8File.getKeyInfo();
                    if (keyInfo != null) {
                        downloadKeyFile(keyInfo);
                        LogUtils.i(TAG, "key文件下载成功：" + keyInfo);
                    }

                    //创建临时目录，命名为filename的隐藏文件
                    File dir = getTempDir();
                    //创建ts下载任务
                    tsDownloadBundles = new ArrayList<>();
                    for (TSInfo info : tsInfoList) {
                        String tsUrl = Utils.buildResourceUrl(getUrl(), info.getUri());
                        String fileName = Uri.parse(tsUrl).getLastPathSegment();
                        TSDownloadBundle downloadBundle = factory.createTSDownloadBundle(dir.getAbsolutePath(), fileName, tsUrl, 0, -1, info);
                        tsDownloadBundles.add(downloadBundle);
                    }
                    configTSDownloadBundles(tsDownloadBundles);
                    dispatchTSDownloadBundlesReady(tsDownloadBundles);
                    startTSDownloadTasks();
                } catch (Throwable e) {
                    e.printStackTrace();
                    dispatchFail(e);
                }
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                LogUtils.i(TAG, "====dispatchFail===5====");
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

    private void cancelAllDownloadingTSDownloadTasks() {
        if (tsDownloadBundles != null) {
            for (TSDownloadBundle downloadBundle : tsDownloadBundles) {
                HttpPartDownloadTask downloadTask = downloadBundle.getDownloadTask();
                downloadTask.cancel();
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
        tsDownloadTaskFail.set(false);
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

    private void tryStartIdleTSDownloadTasks() {

        for (TSDownloadBundle downloadBundle : tsDownloadBundles) {
            if (this.activeTSTaskCount.get() >= MAX_ACTIVE_TS_TASK_COUNT || isCanceled()) {
                return;
            }
            HttpPartDownloadTask downloadTask = downloadBundle.getDownloadTask();
            Status status = downloadTask.getStatus();
            if (status == Status.IDLE) {
                downloadTask.start();
            }
        }
    }

    private void configTSDownloadBundles(List<TSDownloadBundle> tsDownloadBundles) {

        if (tsDownloadBundles == null) {
            return;
        }
        int successCount = 0;
        int activeTaskCount = 0;
        for (TSDownloadBundle downloadBundle : tsDownloadBundles) {
            HttpPartDownloadTask downloadTask = downloadBundle.getDownloadTask();
            Status status = downloadTask.getStatus();
            if (status == Status.SUCCESS) {
                successCount++;
                continue;
            } else if (status == Status.DOWNLOADING) {
                activeTaskCount++;
            }
            downloadTask.addStatusChangeListener(new OnStatusChangeListener() {
                @Override
                public void onStatusChange(Status newStatus, Status oldStatus) {
                    if (newStatus == Status.DOWNLOADING) {
                        activeTSTaskCount.incrementAndGet();
                    } else {
                        activeTSTaskCount.decrementAndGet();
                    }
                    if (newStatus == Status.SUCCESS) {
                        currentDuration += downloadBundle.getInfo().getDuration();
                        dispatchProgressChange();

                        if (successTSDownloadTaskCount.incrementAndGet() == tsDownloadBundles.size()) {
                            LogUtils.i(TAG, "206 所有子任务完成");
                            //保证100%进度回调
                            dispatchProgressChange();
                            handleAllTSDownloadTasksSuccess();
                        }
                        tryStartIdleTSDownloadTasks();
                    }
                }
            });
            downloadTask.addTaskFailListener(new OnTaskFailListener() {
                @Override
                public void onTaskFail(Throwable e) {
                    if (!tsDownloadTaskFail.getAndSet(true)) {
                        LogUtils.i(TAG, "206 子任务失败: " + downloadTask.getSaveFileName());
                        cancelAllDownloadingTSDownloadTasks();
                        LogUtils.i(TAG, "====dispatchFail===1====");
                        dispatchFail(new RuntimeException("子任务失败: " + e.getMessage(), e));
                    }
                }
            });
            downloadTask.addOnProgressChangeListener(new OnProgressChangeListener() {
                @Override
                public void onProgressChange() {
                    long totalLength = 0;
                    for (TSDownloadBundle downloadBundle : tsDownloadBundles) {
                        totalLength += downloadBundle.getDownloadTask().getCurrentLength();
                    }
                    dispatchCurrentLength(totalLength);
                }
            });
        }
        this.successTSDownloadTaskCount.set(successCount);
        this.activeTSTaskCount.set(activeTaskCount);
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
            LogUtils.i(TAG, "====dispatchFail===2====");
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
        File tempFile = new File(getTempDir(), "index.m3u8");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            M3u8File originM3u8File = getResourceInfo().getM3u8File();
            List<TSInfo> tsInfoList = new ArrayList<>();
            for (TSDownloadBundle bundle : tsDownloadBundles) {
                TSInfo originTSInfo = bundle.getInfo();
                String saveFileName = bundle.getDownloadTask().getSaveFileName();
                TSInfo tsInfo = new TSInfo(originTSInfo.getDuration(), saveFileName);
                tsInfoList.add(tsInfo);
            }
            KeyInfo newKeyInfo = null;
            KeyInfo originKeyInfo = originM3u8File.getKeyInfo();
            if (originKeyInfo != null) {
                newKeyInfo = new KeyInfo(originKeyInfo.getMethod(), getKeyFileName());
            }
            new M3u8FileEncoder().encode(new M3u8File(originM3u8File.getVersion(),
                    originM3u8File.getMediaSequence(),
                    originM3u8File.getTargetDuration(),
                    originM3u8File.getPlayListType(),
                    tsInfoList,
                    null,
                    newKeyInfo), fos);
        }
        File outputFile = new File(getSaveDir(), getSaveFileName());
        String cmd = String.format("-allowed_extensions ALL -protocol_whitelist \"file,http,crypto,tcp\" -i %s -c copy %s", tempFile.getAbsolutePath(), outputFile.getAbsolutePath());
        LogUtils.i(TAG, "=======cmd========" + cmd);
        int returnCode = FFmpeg.execute(cmd);
        if (returnCode != Config.RETURN_CODE_SUCCESS) {
            throw new IOException("merge ts file fail with code:" + returnCode);
        }
        LogUtils.i(TAG, "=======Success========");
    }

    private String getKeyFileName() throws IOException {
        return "key.key";
    }

    private void downloadKeyFile(KeyInfo keyInfo) throws Throwable {
        String url = Utils.buildResourceUrl(getUrl(), keyInfo.getUri().replaceAll("\"", ""));
        LogUtils.i(TAG, "key file url: " + url);
        Call call = OKHttpClients.getDefault(context)
                .newCall(new Request.Builder()
                        .url(url).get()
                        .build());
        Response response = call.execute();
        ResponseBody body = response.body();
        if (!response.isSuccessful() || body == null) {
            throw new RuntimeException("download key file fail with code:" + response.code());
        }
        FileUtils.writeToFileFrom(body.byteStream(), new File(getTempDir(), getKeyFileName()), 0, null);
    }

    private M3u8File downloadM3u8File(StreamInfo streamInfo) throws Throwable {
        String uri = streamInfo.getUri();
        String url = Utils.buildResourceUrl(getUrl(), uri);
        Call call = OKHttpClients.getDefault(context)
                .newCall(new Request.Builder()
                        .url(url).get()
                        .build());
        Response response = call.execute();
        ResponseBody body = response.body();
        if (!response.isSuccessful() || body == null) {
            throw new RuntimeException("http response fail with code:" + response.code());
        }
        M3u8File m3u8File = new M3u8FileParser().parse(body.byteStream());
        LogUtils.i(TAG, "m3u8File===========" + m3u8File);
        return m3u8File;
    }

    private File getTempDir() throws IOException {
        String saveFileName = getSaveFileName();
        String dirName = saveFileName.substring(0, saveFileName.indexOf("."));
        File dir = new File(getSaveDir(), dirName);
        FileUtils.createDirIfNotExists(dir);
        return dir;
    }

    public float getCurrentDuration() {
        return currentDuration;
    }

    public String getUrl() {
        return url;
    }

    public void addOnResourceInfoReadyListener(OnResourceInfoReadyListener listener) {
        if (onResourceInfoReadyListeners.contains(listener)) {
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
        if (onTSDownloadBundlesReadyListeners.contains(listener)) {
            return;
        }
        onTSDownloadBundlesReadyListeners.add(listener);
    }

    public void removeOnTSDownloadBundlesReadyListener(OnTSDownloadBundlesReadyListener listener) {
        onTSDownloadBundlesReadyListeners.remove(listener);
    }

    protected void dispatchTSDownloadBundlesReady(List<TSDownloadBundle> bundles) {
        for (int i = onTSDownloadBundlesReadyListeners.size() - 1; i >= 0; i--) {
            onTSDownloadBundlesReadyListeners.get(i).onTSBundlesReady(bundles);
        }
    }
}
