package com.zhangqiang.downloadmanager.task.http.okhttp;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.http.HttpResponse;
import com.zhangqiang.downloadmanager.task.http.HttpUtils;
import com.zhangqiang.downloadmanager.task.http.RangePart;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.OkHttpUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OKHttpPartDownloadTask extends DownloadTask {

    public static final String TAG = OKHttpPartDownloadTask.class.getSimpleName();

    private final Context context;
    private final String url;
    private final long fromPosition;
    private final long toPosition;
    private final String savePath;
    private Call call;
    private final AtomicBoolean mRunning = new AtomicBoolean(false);
    private OnProgressChangeListener onProgressChangeListener;

    public OKHttpPartDownloadTask(Context context, String url, long fromPosition, long toPosition, String savePath) {
        this.context = context;
        this.url = url;
        this.fromPosition = fromPosition;
        this.toPosition = toPosition;
        this.savePath = savePath;
        if(fromPosition < 0 || toPosition < 0){
            throw new IllegalArgumentException("illegal param for position:from-" + fromPosition + ",to-"+toPosition);
        }
    }

    @Override
    protected void onStart() {

        if (mRunning.getAndSet(true)) {
            return;
        }
        notifyStart();
        final Request.Builder builder = new Request.Builder()
                .get()
                .url(url);
        if (fromPosition >=0) {
            StringBuilder  sb = new StringBuilder("bytes=");
            sb.append(fromPosition);
            sb.append("-");
            if (toPosition >= 0) {
                sb.append(toPosition);
            }
            builder.header("Range", sb.toString());
        }
        Request request = builder.build();
        call = OkHttpUtils.getOkHttpClient(context).newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                setStartFalse();
                if (!call.isCanceled()) {
                    notifyFail(new DownloadException(DownloadException.HTTP_CONNECT_FAIL,e));
                }
            }

            @Override
            public void onResponse(@NonNull final Call call, @NonNull final Response response) throws IOException {

                HttpResponse httpResponse = null;
                try {

                     httpResponse = new OkHttpResponse(response);

                    int responseCode = httpResponse.getResponseCode();
                    if (responseCode == 206) {
                        doRangeWrite(httpResponse);
                        LogUtils.i(TAG,"下载完成" + savePath);
                        notifyComplete();
                    }else if(responseCode == 200){
                        notifyFail(new DownloadException(DownloadException.HTTP_RESPONSE_ERROR,"try use range download to a not support resource:" + url));
                    }else {
                        notifyFail(new DownloadException(DownloadException.HTTP_RESPONSE_ERROR,"http response error:code:" + responseCode));
                    }
                } catch (IOException e) {
                    if (!call.isCanceled()) {
                        notifyFail(new DownloadException(DownloadException.WRITE_FILE_FAIL,e));
                    }
                }finally {
                    httpResponse.close();
                    setStartFalse();
                }
            }
        });
    }

    private void doRangeWrite(HttpResponse httpResponse) throws IOException {

        RangePart rangePart = HttpUtils.parseRangePart(httpResponse);
        LogUtils.i(TAG, savePath + "============" + rangePart);
        final long start;
        final long end;
        start = rangePart.getStart();
        end = rangePart.getEnd();
        InputStream inputStream = httpResponse.getInputStream();
        FileUtils.writeToFileFrom(inputStream, new File(savePath),start, new FileUtils.WriteFileListener() {
            long current = start;
            @Override
            public void onWriteFile(byte[] buffer, int offset, int len) {
                current += len;
                if (onProgressChangeListener != null) {
                    onProgressChangeListener.onProgressChange(current,start,end);
                }
            }
        });

    }

    @Override
    protected void onCancel() {
        setStartFalse();
        if (call != null && !call.isCanceled()) {
            call.cancel();
            call = null;
            notifyCancel();
        }
    }

    @Override
    public boolean isRunning() {
        return mRunning.get();
    }

    private void setStartFalse(){
        mRunning.getAndSet(false);
    }

    public interface OnProgressChangeListener{
        void onProgressChange(long current,long from,long to);
    }

    public void setOnProgressChangeListener(OnProgressChangeListener onProgressChangeListener) {
        this.onProgressChangeListener = onProgressChangeListener;
    }
}
