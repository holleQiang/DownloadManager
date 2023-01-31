package com.zhangqiang.downloadmanager.task.http.part;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.engine.Callback;
import com.zhangqiang.downloadmanager.task.http.engine.Cancelable;
import com.zhangqiang.downloadmanager.task.http.engine.HttpEngine;
import com.zhangqiang.downloadmanager.task.http.engine.HttpRequest;
import com.zhangqiang.downloadmanager.task.http.engine.HttpResponse;
import com.zhangqiang.downloadmanager.task.http.range.RangePart;
import com.zhangqiang.downloadmanager.task.http.utils.HttpUtils;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.IOUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-01
 */
public class HttpDownloadPartTask extends DownloadTask {

    public static final String TAG = HttpDownloadPartTask.class.getSimpleName();

    private final String url;
    private final long fromPosition;
    private long currentPosition;
    private final long toPosition;
    private final String filePath;
    private Cancelable mHttpTask;
    private final HttpEngine mHttpEngine;

    public HttpDownloadPartTask(HttpEngine httpEngine,
                                String url,
                                long fromPosition,
                                long currentPosition,
                                long toPosition,
                                String filePath) {
        this.mHttpEngine = httpEngine;
        this.url = url;
        this.fromPosition = fromPosition;
        this.currentPosition = currentPosition;
        this.toPosition = toPosition;
        this.filePath = filePath;
        if (fromPosition < 0 || toPosition < 0 || toPosition < fromPosition) {
            throw new IllegalArgumentException("illegal param for position:from-" + fromPosition + ",to-" + toPosition);
        }
    }

    @Override
    protected void onStart() {

        if(currentPosition >= toPosition){
            dispatchComplete();
            return;
        }
        HttpRequest.Builder builder = new HttpRequest.Builder().setUrl(url);
        HttpUtils.setRangeParams(builder,currentPosition,toPosition);
        mHttpTask = mHttpEngine.get(builder.build(), new Callback() {
            @Override
            public void onResponse(HttpResponse httpResponse) {
                try {
                    int responseCode = httpResponse.getResponseCode();
                    if (responseCode == 206) {
                        doRangeWrite(httpResponse);
                        LogUtils.i(TAG, "下载完成" + filePath);
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

            @Override
            public void onFail(Throwable e) {
                dispatchFail(new DownloadException(DownloadException.UNKNOWN,e));
            }
        });
    }

    @Override
    protected void onCancel() {
        if (mHttpTask != null && !mHttpTask.isCancelled()) {
            mHttpTask.cancel();
            mHttpTask = null;
        }
    }

    @Override
    public long getCurrentLength() {
        return currentPosition - fromPosition;
    }

    private void doRangeWrite(HttpResponse httpResponse) throws IOException {

        RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
        if (rangePart == null) {
            throw new IOException("parse range part fail");
        }
        LogUtils.i(TAG, filePath + "============" + rangePart);
        final long start = rangePart.getStart();
        final long end = rangePart.getEnd();
        if (start != currentPosition && end != toPosition) {
            dispatchFail(new DownloadException(DownloadException.RANGE_CHANGED, "range has changed"));
            return;
        }
        long fileSeek = currentPosition-fromPosition;
        InputStream inputStream = httpResponse.getInputStream();
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if (parentFile != null && !parentFile.exists()) {
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

    public long getContentLength() {
        return toPosition - fromPosition + 1;
    }

    public long getFromPosition() {
        return fromPosition;
    }

    public long getToPosition() {
        return toPosition;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getUrl() {
        return url;
    }
}
