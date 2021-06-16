package com.zhangqiang.downloadmanager.task.http;

import androidx.annotation.NonNull;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.manager.DownloadExecutors;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.range.RangePart;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.IOUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-01
 */
public abstract class HttpDownloadPartTask extends DownloadTask {

    public static final String TAG = HttpDownloadPartTask.class.getSimpleName();

    private final String url;
    private final long fromPosition;
    private long currentLength;
    private final long toPosition;
    private final String savePath;
    private Future<?> mCancelFuture;

    public HttpDownloadPartTask(String url, long fromPosition, long currentLength, long toPosition, String savePath) {
        this.url = url;
        this.fromPosition = fromPosition;
        this.currentLength = currentLength;
        this.toPosition = toPosition;
        this.savePath = savePath;
        if (fromPosition < 0 || toPosition < 0 || toPosition < fromPosition) {
            throw new IllegalArgumentException("illegal param for position:from-" + fromPosition + ",to-" + toPosition);
        }
    }

    @Override
    protected void onStart() {

        mCancelFuture = DownloadExecutors.executor.submit(new RealTask());
    }

    private class RealTask implements Runnable {

        @Override
        public void run() {

            if (isFinished()) {
                dispatchComplete();
                return;
            }
            try {
                execute(new HttpResponseHandler());
            }catch (Throwable e){
                dispatchFail(new DownloadException(DownloadException.UNKNOWN,e));
            }
        }
    }

    protected abstract void execute(@NonNull ResponseReadyCallback callback);

    @Override
    protected void dispatchFail(DownloadException e) {
        super.dispatchFail(e);
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

    private class HttpResponseHandler implements ResponseReadyCallback {

        @Override
        public void onResponseReady(HttpResponse httpResponse) {
            try {
                int responseCode = httpResponse.getResponseCode();
                if (responseCode == 206) {
                    doRangeWrite(httpResponse);
                    LogUtils.i(TAG, "下载完成" + savePath);
                    dispatchComplete();
                } else if (responseCode == 200) {
                    dispatchFail(new DownloadException(DownloadException.HTTP_RESPONSE_ERROR, "try use range download to a not support resource:" + url));
                } else {
                    dispatchFail(new DownloadException(DownloadException.HTTP_RESPONSE_ERROR, "http response error:code:" + responseCode));
                }
            } catch (IOException e) {
                dispatchFail(new DownloadException(DownloadException.WRITE_FILE_FAIL, e));
            } finally {
                IOUtils.closeSilently(httpResponse);
            }
        }
    }

    private void doRangeWrite(HttpResponse httpResponse) throws IOException {

        RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
        if (rangePart == null) {
            throw new IOException("parse range part fail");
        }
        LogUtils.i(TAG, savePath + "============" + rangePart);
        final long start = rangePart.getStart();
        final long end = rangePart.getEnd();
        if (start != fromPosition + currentLength && end != toPosition) {
            dispatchFail(new DownloadException(DownloadException.RANGE_CHANGED, "range has changed"));
            return;
        }
        long fileSeek = currentLength;
        InputStream inputStream = httpResponse.getInputStream();
        File file = new File(savePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            if (!parentFile.mkdirs()) {
                throw new FileNotFoundException("dir create fail for:" + parentFile.getAbsolutePath());
            }
        }
        FileUtils.writeToFileFrom(inputStream, file, fileSeek, new FileUtils.WriteFileListener() {

            @Override
            public void onWriteFile(byte[] buffer, int offset, int len) {
                currentLength += len;
            }
        });
        LogUtils.i(TAG, "======write finished===start=" + fromPosition + "=currentPosition=" + currentLength + "==end=" + toPosition);
    }

    public boolean isFinished() {
        return getCurrentLength() >= getContentLength();
    }

    public long getContentLength() {
        return toPosition - fromPosition + 1;
    }

    public long getFromPosition() {
        return fromPosition;
    }

    public long getToPosition() {
        return toPosition;
    }

    public String getSavePath() {
        return savePath;
    }

    public String getUrl() {
        return url;
    }
}
