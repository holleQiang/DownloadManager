package com.zhangqiang.downloadmanager.task.http.okhttp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zhangqiang.downloadmanager.exception.DownloadException;
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
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpDownloadPartTask extends DownloadTask {

    public static final String TAG = OKHttpDownloadPartTask.class.getSimpleName();

    private final Context context;
    private final String url;
    private final long fromPosition;
    private final long initialCurrentPosition;
    private long currentPosition;
    private final long toPosition;
    private final String savePath;
    private Call call;

    public OKHttpDownloadPartTask(Context context, String url, long fromPosition, long currentPosition, long toPosition, String savePath) {
        this.context = context;
        this.url = url;
        this.fromPosition = fromPosition;
        this.currentPosition = this.initialCurrentPosition = currentPosition;
        this.toPosition = toPosition;
        this.savePath = savePath;
        if (fromPosition < 0 || toPosition < 0 || currentPosition < fromPosition) {
            throw new IllegalArgumentException("illegal param for position:from-" + fromPosition + ",to-" + toPosition);
        }
    }

    @Override
    protected void onStart() {

        if (isFinished()) {
            dispatchComplete();
            return;
        }
        final Request.Builder builder = new Request.Builder()
                .get()
                .url(url);
        builder.header("Range", "bytes=" + currentPosition +
                "-" +
                toPosition);
        Request request = builder.build();
        call = OKHttpUtils.getOkHttpClient(context).newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!call.isCanceled()) {
                    dispatchFail(new DownloadException(DownloadException.HTTP_CONNECT_FAIL, e));
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) {

                HttpResponse httpResponse = null;
                try {

                    httpResponse = new OkHttpResponse(response);

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
        });
    }

    private void doRangeWrite(HttpResponse httpResponse) throws IOException {

        RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
        if (rangePart == null) {
            throw new IOException("parse range part fail");
        }
        LogUtils.i(TAG, savePath + "============" + rangePart);
        final long start = rangePart.getStart();
        final long end = rangePart.getEnd();
        if (start != currentPosition && end != toPosition) {
            dispatchFail(new DownloadException(DownloadException.RANGE_CHANGED, "range has changed"));
            return;
        }
        long fileSeek = initialCurrentPosition - fromPosition;
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
                currentPosition += len;
            }
        });
        LogUtils.i(TAG, "======write finished===start=" + fromPosition + "=currentPosition=" + currentPosition + "==end=" + toPosition);
    }

    @Override
    protected void onCancel() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
            call = null;
        }
    }

    @Override
    public long getCurrentLength() {
        return currentPosition;
    }

    public boolean isFinished() {
        return currentPosition >= toPosition;
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
