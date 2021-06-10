package com.zhangqiang.downloadmanager.task.http;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.manager.DownloadExecutors;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.IOUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-02
 */
public abstract class HttpDownloadTask3 extends DownloadTask {

    public static final String TAG = HttpDownloadTask.class.getSimpleName();

    private final String url;
    private final String saveDir;
    private final String saveFileName;
    private long currentLength;
    private Future<?> mCancelFuture;

    public HttpDownloadTask3(String url, String saveDir, String saveFileName,long currentLength) {
        this.url = url;
        this.saveDir = saveDir;
        this.saveFileName = saveFileName;
        this.currentLength = currentLength;
    }

    @Override
    protected void onStart() {

        mCancelFuture = DownloadExecutors.executor.submit(new Runnable() {
            @Override
            public void run() {

                LogUtils.i(TAG, "开始下载。。。");
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
                if (responseCode == 206) {
                    dispatchFail(new DownloadException(DownloadException.RANGE_CHANGED, "url support range download"));
                    return;
                }
                if (responseCode == 200) {
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
    }

    protected abstract void execute(ResponseReadyCallback callback);

    private void doSingleThreadDownload(HttpResponse httpResponse) {
        try {
            currentLength = 0;
            File saveDirFile = new File(saveDir);
            FileUtils.createDirIfNotExists(saveDirFile);
            File saveFile = new File(saveDirFile, saveFileName);
            FileUtils.deleteFileIfExists(saveFile);
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


    @Override
    protected void onCancel() {
        if (mCancelFuture != null && !mCancelFuture.isCancelled()) {
            mCancelFuture.cancel(true);
            mCancelFuture = null;
        }
    }

    @Override
    public long getCurrentLength() {
        return currentLength;
    }

    public String getUrl() {
        return url;
    }
}
