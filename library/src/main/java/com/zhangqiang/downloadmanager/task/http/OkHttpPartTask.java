package com.zhangqiang.downloadmanager.task.http;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zhangqiang.downloadmanager.task.part.PartTask;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.HttpUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.OkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpPartTask extends PartTask {

    public static final String TAG = "OkHttpPartTask";
    private Context context;
    private Call call;

    public OkHttpPartTask(String url, String savePath, Context context) {
        super(url, savePath);
        this.context = context;
    }

    @Override
    protected void onStart() {

        final Request.Builder builder = new Request.Builder()
                .get()
                .url(getUrl());
        setRangeParams(builder);
        Request request = builder.build();
        call = OkHttpUtils.getOkHttpClient(context).newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (!call.isCanceled()) {
                    notifyFail(e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {

                try {
                    HttpResponse httpResponse = new OkHttpResponse(response);

                    int responseCode = httpResponse.getResponseCode();
                    if (responseCode != 206) {
                        throw new IllegalArgumentException("unSupport response code:" + responseCode);
                    }

                    HttpUtils.RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
                    LogUtils.i(TAG, getSavePath() + "============" + rangePart);

                    InputStream inputStream = httpResponse.getInputStream();
                    FileUtils.writeToFileFrom(inputStream, new File(getSavePath()), getCurrent(), new FileUtils.WriteFileListener() {

                        @Override
                        public void onWriteFile(byte[] buffer, int offset, int len) {
                            notifyProgress(getCurrent() + len);
                        }
                    });
                    notifyComplete();
                    LogUtils.i(TAG,"下载完成" + getSavePath());
                } catch (IOException e) {
                    if (!call.isCanceled()) {
                        notifyFail(e);
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    @Override
    protected void onPause() {
        LogUtils.i(TAG, "=========pause=========" + getSavePath() + "====" + call);
        if (call != null) {
            call.cancel();
            call = null;
        }
    }

    private void setRangeParams(final Request.Builder builder) {
        long start = getCurrent() + getStart();
        long end = getEnd();
        LogUtils.i(TAG, "====setRangeParams======start====" + start + "====end=======" + end);
        HttpUtils.setRangeParams(new OkHttpRequestPropertySetter(builder), start, end);
    }
}
