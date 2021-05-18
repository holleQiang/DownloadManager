package com.zhangqiang.downloadmanager.task.http.okhttp;

import android.content.Context;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.manager.DownloadExecutors;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpResponse;
import com.zhangqiang.downloadmanager.task.http.HttpUtils;
import com.zhangqiang.downloadmanager.task.http.range.RangePart;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.IOUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.OKHttpUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Future;

import okhttp3.Call;
import okhttp3.Request;

public class OKHttpDownloadPartTask extends DownloadTask {

    public static final String TAG = OKHttpDownloadPartTask.class.getSimpleName();

    private final Context context;
    private final String url;
    private final long fromPosition;
    private long currentLength;
    private final long toPosition;
    private final String savePath;
    private Call mCall;
    private Future<?> mCancelFuture;

    public OKHttpDownloadPartTask(Context context, String url, long fromPosition, long currentLength, long toPosition, String savePath) {
        this.context = context;
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
            final Request.Builder builder = new Request.Builder()
                    .get()
                    .url(url);
            builder.header("Range", "bytes=" + (fromPosition + currentLength) +
                    "-" +
                    toPosition);
            Request request = builder.build();
            Call call = OKHttpUtils.getOkHttpClient(context).newCall(request);
            mCall = call;
            HttpResponse httpResponse = null;
            try {
                httpResponse = new OkHttpResponse(call.execute());
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
                if (!call.isCanceled()) {
                    dispatchFail(new DownloadException(DownloadException.WRITE_FILE_FAIL, e));
                }
            } finally {
                IOUtils.closeSilently(httpResponse);
            }
        }
    }

    @Override
    protected void dispatchFail(DownloadException e) {
        super.dispatchFail(e);
        cancelHttpRequest();
    }

    @Override
    protected void onCancel() {
        if (mCancelFuture != null && !mCancelFuture.isCancelled()) {
            mCancelFuture.cancel(true);
            mCancelFuture = null;
        }
        cancelHttpRequest();
    }

    private void cancelHttpRequest() {
        if (mCall != null  ) {
            if (!mCall.isCanceled()) {
                mCall.cancel();
            }
            mCall = null;
        }
    }

    @Override
    public long getCurrentLength() {
        return currentLength;
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
}
