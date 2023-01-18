package com.zhangqiang.downloadmanager.task.http;

import android.text.TextUtils;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.callback.Callbacks;
import com.zhangqiang.downloadmanager.task.http.callback.ResourceInfo;
import com.zhangqiang.downloadmanager.task.http.engine.Callback;
import com.zhangqiang.downloadmanager.task.http.engine.Cancelable;
import com.zhangqiang.downloadmanager.task.http.engine.HttpEngine;
import com.zhangqiang.downloadmanager.task.http.engine.HttpRequest;
import com.zhangqiang.downloadmanager.task.http.engine.HttpResponse;
import com.zhangqiang.downloadmanager.task.http.part.HttpDownloadPartTask;
import com.zhangqiang.downloadmanager.task.http.part.HttpPartTaskFactory;
import com.zhangqiang.downloadmanager.task.http.range.RangePart;
import com.zhangqiang.downloadmanager.task.http.utils.HttpUtils;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-01
 */
public class HttpDownloadTask extends DownloadTask{

    public static final String TAG = HttpDownloadTask.class.getSimpleName();
    private final String mUrl;
    private final String mSaveDir;
    private final String mTargetFileName;
    private final int mThreadSize;

    private long currentLength;
    private List<HttpDownloadPartTask> mPartTasks;
    private final AtomicInteger mFinishedPartTaskSize = new AtomicInteger(0);
    private final HttpEngine mHttpEngine;
    private final HttpPartTaskFactory mHttpPartTaskFactory;
    private final Callbacks mCallbacks = new Callbacks();
    private Cancelable mGetInfoTask;
    private String mFileName = null;

    public HttpDownloadTask(String id,
                            HttpEngine httpEngine,
                            String url,
                            String saveDir,
                            String targetFileName,
                            int threadSize,
                            HttpPartTaskFactory httpPartTaskFactory) {
        super(id);
        this.mUrl = url;
        this.mSaveDir = saveDir;
        this.mTargetFileName = targetFileName;
        this.mThreadSize = threadSize;
        this.mHttpEngine = httpEngine;
        this.mHttpPartTaskFactory = httpPartTaskFactory;
    }

    @Override
    protected void onStart() {

        LogUtils.i(TAG, "开始获取资源信息。。。");
        getCallbacks().notifyStartGenerateInfo();
        HttpRequest.Builder builder = new HttpRequest.Builder()
                .setUrl(mUrl);
        HttpUtils.setRangeParams(builder,0);
        mGetInfoTask = mHttpEngine.get(builder
                .build(), new Callback() {
            @Override
            public void onResponse(HttpResponse httpResponse) {
                try {
                    int responseCode = httpResponse.getResponseCode();

                    if (httpResponse.isSuccess()) {
                        mFileName = makeFileName(httpResponse);
                        ResourceInfo info = new ResourceInfo();
                        info.setFileName(mFileName);
                        info.setContentLength(httpResponse.getContentLength());
                        info.setContentType(httpResponse.getContentType());
                        info.setETag(HttpUtils.parseETag(httpResponse));
                        info.setLastModified(HttpUtils.parseLastModified(httpResponse));
                        getCallbacks().notifyResourceInfoReady(info);
                        LogUtils.i(TAG, "资源信息就绪。。。" + info.toString());
                    }
                    if (responseCode == 206) {
                        getCallbacks().notifyStartPartDownload();
                        LogUtils.i(TAG, "开始多线程下载");
                        doMultiThreadDownload(httpResponse);
                    } else if (responseCode == 200) {
                        getCallbacks().notifyStartDefaultDownload();
                        LogUtils.i(TAG, "开始单线程下载");
                        doSingleThreadDownload(httpResponse);
                    } else {
                        dispatchFail(new DownloadException(DownloadException.HTTP_RESPONSE_ERROR, "http response error:code:" + responseCode));
                    }
                } catch (Throwable e) {
                    dispatchFail(new DownloadException(DownloadException.UNKNOWN, e));
                } finally {
                    IOUtils.closeSilently(httpResponse);
                }
            }

            @Override
            public void onFail(Throwable e) {
                dispatchFail(new DownloadException(DownloadException.UNKNOWN, e));
            }
        });
    }

    private void doSingleThreadDownload(HttpResponse httpResponse) {
        try {
            File saveFile = new File(FileUtils.createDirIfNotExists(new File(mSaveDir)), mFileName);
            FileUtils.deleteFileIfExists(saveFile);
            InputStream inputStream = httpResponse.getInputStream();
            FileUtils.writeToFileFrom(inputStream, saveFile, 0, new FileUtils.WriteFileListener() {

                @Override
                public void onWriteFile(byte[] buffer, int offset, int len) {
                    currentLength += len;
                }
            });
            LogUtils.i(TAG, "单线程下载完成" + mSaveDir);
            dispatchComplete();
        } catch (IOException e) {
            dispatchFail(new DownloadException(DownloadException.WRITE_FILE_FAIL, e));
        }
    }

    private void doMultiThreadDownload(HttpResponse httpResponse) {
        String saveDir = mSaveDir;
        RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
        LogUtils.i(TAG, saveDir + "============" + rangePart);
        if (rangePart == null) {
            dispatchFail(new DownloadException(DownloadException.PARSE_PART_FAIL, "cannot parse range part"));
            return;
        }
        List<HttpDownloadPartTask> partTasks = new ArrayList<>();
        int threadSize = mThreadSize;
        final long total = rangePart.getTotal();
        long eachDownload = total / threadSize;
        long resetDownload = total % threadSize;
        for (int i = 0; i < threadSize; i++) {
            final long start = i * eachDownload;
            long end = start + eachDownload;
            if (i == threadSize - 1) {
                end += resetDownload - 1;
            }
            File saveFile = new File(saveDir, mFileName+"_"+threadSize+"_"+i);

            HttpDownloadPartTask task = mHttpPartTaskFactory.onCreateHttpPartTask(mUrl,start,end,saveFile.getAbsolutePath());
            initPartTask(task);
            partTasks.add(task);
        }
        if (partTasks.isEmpty()) {
            dispatchFail(new DownloadException(DownloadException.PARAM_ERROR, "part task empty by error param"));
            return;
        }
        LogUtils.i(TAG, "多任务创建完成。。。");
        getCallbacks().notifyPartTasksCreate(partTasks);
        if(mPartTasks != null){
            throw new IllegalArgumentException("---------------");
        }
        if(isStarted()){
            mPartTasks = partTasks;
            LogUtils.i(TAG, "开始启动多任务。。。");
            for (int i = 0; i < partTasks.size(); i++) {
                HttpDownloadPartTask partTask = partTasks.get(i);
                partTask.start();
            }
            LogUtils.i(TAG, "启动多任务成功。。。");
        }
    }

    private void initPartTask(HttpDownloadPartTask task) {
        task.addDownloadListener(new DownloadTask.DownloadListener() {
            @Override
            public void onIdle() {

            }

            @Override
            public void onStart() {
            }

            @Override
            public void onComplete() {
                if (mFinishedPartTaskSize.incrementAndGet() == mPartTasks.size()) {
                    handAllTaskFinish();
                }
            }

            @Override
            public void onFail(DownloadException e) {
                cancelAllRunningPartTasks();
                dispatchFail(new DownloadException(DownloadException.PART_FAIL, "子任务失败："+task.getFilePath()));
            }

            @Override
            public void onCancel() {
            }
        });
    }

    @Override
    protected void onCancel() {
        if (mGetInfoTask != null && !mGetInfoTask.isCancelled()) {
            mGetInfoTask.cancel();
            mGetInfoTask = null;
        }
        cancelAllRunningPartTasks();
    }

    @Override
    public List<? extends DownloadTask> getChildTasks() {
        return mPartTasks;
    }

    private String makeFileName(HttpResponse httpResponse) {
        String fileName = mTargetFileName;
        if (TextUtils.isEmpty(fileName)) {
            fileName = HttpUtils.parseFileName(httpResponse);
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = URLUtils.getFileName(mUrl);
        }
        if (TextUtils.isEmpty(fileName)) {
            fileName = MD5Utils.getMD5(mUrl)
                    + "." + HttpUtils.getFileNameSuffixByContentType(httpResponse);
        }
        return FileUtils.getDistinctFileName(mSaveDir, fileName);
    }

    @Override
    public long getCurrentLength() {
        if (mPartTasks != null) {
            int totalLength = 0;
            for (HttpDownloadPartTask mPartTask : mPartTasks) {
                totalLength+= mPartTask.getCurrentLength();
            }
            return totalLength;
        }
        return currentLength;
    }

    private void cancelAllRunningPartTasks() {
        List<HttpDownloadPartTask> partTasks = mPartTasks;
        if (partTasks == null) {
            return;
        }
        long l = System.currentTimeMillis();
        for (int i = 0; i < partTasks.size(); i++) {
            HttpDownloadPartTask task = partTasks.get(i);
            task.cancel();
        }
        mPartTasks = null;
        LogUtils.i(TAG,"==cancel耗时======="+(System.currentTimeMillis()-l));
    }

    private void handAllTaskFinish() {
        try {
            LogUtils.i(TAG, "合并临时文件" + mSaveDir);
            mergePartFile();
            LogUtils.i(TAG, "下载完成" + mSaveDir);
            dispatchComplete();
        } catch (IOException e) {
            dispatchFail(new DownloadException(DownloadException.MERGE_PART_FAIL, e));
        }
    }


    private void mergePartFile() throws IOException {
        List<HttpDownloadPartTask> partTasks = mPartTasks;
        if (partTasks == null) {
            return;
        }
        String saveDir = mSaveDir;
        File dir = new File(saveDir);
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("create dir fail:" + saveDir);
            }
        }
        File saveFile = new File(dir, mFileName);
        deleteFileIfExists(saveFile);
        RandomAccessFile raf = null;
        try {
            raf = new RandomAccessFile(saveFile, "rw");
            for (HttpDownloadPartTask partTask : partTasks) {
                raf.seek(partTask.getFromPosition());
                FileInputStream fis = null;
                try {
                    fis = new FileInputStream(partTask.getFilePath());
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
            for (HttpDownloadPartTask partTask : partTasks) {
                File file = new File(partTask.getFilePath());
                if (!file.delete()) {
                    throw new IOException("delete part file fail:" + file.getAbsolutePath());
                }
            }
        } finally {
            IOUtils.closeSilently(raf);
        }
    }


    private void deleteFileIfExists(File file) throws IOException {
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("delete file fail:" + file.getAbsolutePath());
            }
        }
    }

    public Callbacks getCallbacks() {
        return mCallbacks;
    }
}