package com.zhangqiang.downloadmanager.task.http.okhttp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.manager.DownloadExecutors;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpDownloadPartTask;
import com.zhangqiang.downloadmanager.task.http.HttpResponse;
import com.zhangqiang.downloadmanager.task.http.HttpUtils;
import com.zhangqiang.downloadmanager.task.http.ResponseReadyCallback;
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
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpDownloadPartTask extends HttpDownloadPartTask {

    public static final String TAG = OKHttpDownloadPartTask.class.getSimpleName();

    private final Context context;
    private Call mCall;

    public OKHttpDownloadPartTask(Context context, String url, long fromPosition, long currentLength, long toPosition, String savePath) {
        super(url, fromPosition, currentLength, toPosition, savePath);
        this.context = context;
    }

    @Override
    protected void execute(@NonNull final ResponseReadyCallback callback) {
        final Request.Builder builder = new Request.Builder()
                .get()
                .url(getUrl());
        builder.header("Range", "bytes=" + (getFromPosition() + getCurrentLength()) +
                "-" +
                getToPosition());
        Request request = builder.build();
        final Call call = OKHttpClients.getDefault(context).newCall(request);
        mCall = call;
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                dispatchFail(new DownloadException(DownloadException.HTTP_CONNECT_FAIL,e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                callback.onResponseReady(new OkHttpResponse(response));
            }
        });
    }

    @Override
    protected void dispatchFail(DownloadException e) {
        super.dispatchFail(e);
        cancelHttpRequest();
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        cancelHttpRequest();
    }

    private void cancelHttpRequest() {
        OKHttpUtils.cancelCall(mCall);
        mCall = null;
    }
}
