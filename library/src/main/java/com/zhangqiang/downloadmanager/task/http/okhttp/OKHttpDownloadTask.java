package com.zhangqiang.downloadmanager.task.http.okhttp;

import android.content.Context;
import androidx.annotation.NonNull;

import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.http.HttpDownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpUtils;
import com.zhangqiang.downloadmanager.task.http.ResponseReadyCallback;
import com.zhangqiang.downloadmanager.utils.OKHttpUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpDownloadTask extends HttpDownloadTask {

    private final Context context;
    private Call mCall;

    public OKHttpDownloadTask(Context context,DownloadRequest request, PartTaskFactory factory) {
        super(request, factory);
        this.context = context;
    }

    @Override
    protected void execute(final ResponseReadyCallback callback) {
        final Request.Builder builder = new Request.Builder()
                .get()
                .url(getRequest().getUrl());
        HttpUtils.setRangeParams(new OkHttpFiledSetter(builder), 0);
        Request request = builder.build();
        mCall = OKHttpClients.getDefault(context).newCall(request);
        mCall.enqueue(new Callback() {
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
        OKHttpUtils.cancelCall(mCall);
        mCall = null;
    }
}
