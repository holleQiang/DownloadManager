package com.zhangqiang.downloadmanager.task.http.engine.okhttp;

import android.content.Context;

import androidx.annotation.NonNull;

import com.zhangqiang.downloadmanager.task.http.engine.Callback;
import com.zhangqiang.downloadmanager.task.http.engine.Cancelable;
import com.zhangqiang.downloadmanager.task.http.engine.HttpEngine;
import com.zhangqiang.downloadmanager.task.http.engine.HttpRequest;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpEngine implements HttpEngine {

    private final Context context;

    public OkHttpEngine(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public Cancelable get(HttpRequest request, Callback callback) {
        Request.Builder builder = new Request.Builder().url(request.getUrl());
        Map<String, String> headers = request.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.addHeader(entry.getKey(),entry.getValue());
            }
        }
        Call call = OKHttpClients.getDefault(context).newCall(builder.build());
        call.enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onFail(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                callback.onResponse(new OkHttpResponse(response));
            }
        });
        return new Cancelable() {
            @Override
            public void cancel() throws RuntimeException {
                call.cancel();
            }

            @Override
            public boolean isCancelled() {
                return call.isCanceled();
            }
        };
    }
}
