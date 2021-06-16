package com.zhangqiang.downloadmanager.task.http.okhttp;

import android.content.Context;
import androidx.annotation.NonNull;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.http.HttpDownloadTask3;
import com.zhangqiang.downloadmanager.task.http.ResponseReadyCallback;
import com.zhangqiang.downloadmanager.utils.OKHttpUtils;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-02
 */
public class OKHttpDownloadTask3 extends HttpDownloadTask3 {

    private final Context context;
    private Call mCall;

    public OKHttpDownloadTask3(Context context,String url, String saveDir, String saveFileName, long currentLength) {
        super(url, saveDir, saveFileName, currentLength);
        this.context = context;
    }


    @Override
    protected void execute(final ResponseReadyCallback callback) {
        final Request.Builder builder = new Request.Builder()
                .get()
                .url(getUrl());
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
