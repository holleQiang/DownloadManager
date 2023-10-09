package com.zhangqiang.downloadmanager.plugin.http.task;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zhangqiang.downloadmanager.plugin.http.okhttp.OKHttpClients;
import com.zhangqiang.downloadmanager.plugin.http.response.OkHttpResponse;
import com.zhangqiang.downloadmanager.plugin.http.utils.FiledSetter;
import com.zhangqiang.downloadmanager.plugin.http.utils.HttpUtils;
import com.zhangqiang.downloadmanager.task.Status;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class HttpPartDownloadTask extends AbstractHttpDownloadTask {

    private final Context context;
    private final long startPosition;
    private final long endPosition;
    private Call downloadCall;

    public HttpPartDownloadTask(String saveDir, String targetFileName, long createTime, String url, Context context, long startPosition, long endPosition) {
        super(saveDir, targetFileName, createTime, url);
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    public HttpPartDownloadTask(String saveDir, String targetFileName, long createTime, Status status, String errorMessage, String url, ResourceInfo resourceInfo, long currentLength, Context context, long startPosition, long endPosition) {
        super(saveDir, targetFileName, createTime, status, errorMessage, currentLength, url, resourceInfo);
        this.context = context;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    @Override
    protected void onStart() {
        Request.Builder builder = new Request.Builder().url(getUrl());
        HttpUtils.setRangeParams(new FiledSetter() {
            @Override
            public void setField(String key, String value) {
                builder.header(key, value);
            }
        }, getStartPosition() + getCurrentLength(), getEndPosition());
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
                    if (code == 200) {
                        throw new RuntimeException("resource does not support range download");
                    }
                    ResponseBody responseBody = response.body();
                    if (code == 206 && responseBody != null) {

                        OkHttpResponse okHttpResponse = new OkHttpResponse(response);
                        String fileName = HttpUtils.parseFileName(okHttpResponse);
                        long contentLength = responseBody.contentLength();
                        MediaType mediaType = responseBody.contentType();
                        ResourceInfo resourceInfo = new ResourceInfo(fileName,
                                contentLength,
                                mediaType != null ? mediaType.toString() : null,
                                okHttpResponse.getResponseCode());
                        dispatchResourceInfoReady(resourceInfo);

                        performSaveFile(responseBody.byteStream());

                        //保证100%进度回调
                        dispatchProgressChange();
                        dispatchSuccess();
                    } else {
                        dispatchFail(new IllegalStateException("http response error with code" + code + ";body null:" + (responseBody == null)));
                    }
                } catch (Throwable e) {
                    if (getStatus() != Status.CANCELED) {
                        dispatchFail(e);
                    } else {
                        throw e;
                    }
                }
            }
        });
    }

    @Override
    protected void onCancel() {
        if (downloadCall != null && !downloadCall.isCanceled()) {
            downloadCall.cancel();
        }
    }

    @Override
    public String getSaveFileName() {
        return getTargetFileName();
    }

    public long getStartPosition() {
        return startPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }
}
