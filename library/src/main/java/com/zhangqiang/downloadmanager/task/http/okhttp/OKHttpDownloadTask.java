package com.zhangqiang.downloadmanager.task.http.okhttp;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.manager.DownloadExecutors;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpDownloadPartTask;
import com.zhangqiang.downloadmanager.task.http.HttpDownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpResponse;
import com.zhangqiang.downloadmanager.task.http.HttpUtils;
import com.zhangqiang.downloadmanager.task.http.ResponseReadyCallback;
import com.zhangqiang.downloadmanager.task.http.range.RangePart;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.IOUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.MD5Utils;
import com.zhangqiang.downloadmanager.utils.OKHttpUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpDownloadTask extends HttpDownloadTask {

    private final Context context;
    private Call call;

    public OKHttpDownloadTask(Context context, String url, String saveDir, int threadSize, String saveFileName, PartTaskFactory factory) {
        super(url, saveDir, threadSize, saveFileName, factory);
        this.context = context;
    }

    public OKHttpDownloadTask(Context context, String url, String saveDir, int threadSize, String fileName, long contentLength, List<HttpDownloadPartTask> partTasks) {
        super(url, saveDir, threadSize, fileName, contentLength, partTasks);
        this.context = context;
    }

    @Override
    protected void execute(final ResponseReadyCallback callback) {
        final Request.Builder builder = new Request.Builder()
                .get()
                .url(getUrl());
        HttpUtils.setRangeParams(new OkHttpFiledSetter(builder), 0);
        Request request = builder.build();
        call = OKHttpUtils.getOkHttpClient(context).newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                dispatchFail(new DownloadException(DownloadException.HTTP_CONNECT_FAIL, e));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                callback.onResponseReady(new OkHttpResponse(response));
            }
        });
    }

    @Override
    protected void onCancel() {
        super.onCancel();
        if (call != null && !call.isCanceled()) {
            call.cancel();
            call = null;
        }
    }
}
