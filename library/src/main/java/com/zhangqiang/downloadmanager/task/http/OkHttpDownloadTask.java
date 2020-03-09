package com.zhangqiang.downloadmanager.task.http;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.part.PartTask;
import com.zhangqiang.downloadmanager.task.part.PartTaskSync;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.HttpUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.OkHttpUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

public class OkHttpDownloadTask extends DownloadTask {

    private static final String TAG = "OkHttpDownloadTask";
    private final int partCount;
    private final List<PartTask> partTasks = new ArrayList<>();
    private Call call;
    private Context context;

    public OkHttpDownloadTask(String url, String saveDir, int partCount, Context context) {
        super(url, saveDir);
        this.partCount = partCount;
        this.context = context;
    }

    @Override
    protected void onStart() {
        partTasks.clear();
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
            public void onResponse(@NonNull Call call, @NonNull final Response response) {
                try {
                    processResponse(new OkHttpResponse(response),call);
                } catch (Throwable e) {
                    if (!call.isCanceled()) {
                        notifyFail(e);
                        e.printStackTrace();
                    }
                }finally {
                    response.close();
                }
            }
        });
    }

    private void processResponse(final HttpResponse response, Call call) throws IOException, InterruptedException {

        String eTag = HttpUtils.parseETag(response);
        if (!TextUtils.isEmpty(eTag)) {
            setETag(eTag);
        }
        LogUtils.i(TAG, "eTag = " + eTag);

        String lastModified = HttpUtils.parseLastModified(response);
        if (!TextUtils.isEmpty(lastModified)) {
            setLastModified(lastModified);
        }
        LogUtils.i(TAG, "lastModified = " + lastModified);

        String fileName = HttpUtils.parseFileName(response);
        if (!TextUtils.isEmpty(fileName)) {
            setFileName(fileName);
        }

        setContentType(response.getContentType());

        File dir = new File(getSaveDir());
        if (!dir.exists() && !dir.mkdirs()) {
            throw new RuntimeException("cannot create dir : " + dir.getAbsolutePath());
        }
        final File file = new File(dir, getFileName());


        int responseCode = response.getResponseCode();
        if (responseCode == 200) {
            LogUtils.i(TAG, "单线程下载===========");
            long total = response.getContentLength();
            setTotalLength(total);
            FileUtils.writeToFileFrom(response.getInputStream(), file, 0, new FileUtils.WriteFileListener() {

                long current = getCurrentLength();

                @Override
                public void onWriteFile(byte[] buffer, int offset, int len) {
                    current += len;
                    notifyProgress(current);
                }
            });
            notifyComplete();
        } else if (responseCode == 206) {
            LogUtils.i(TAG, "多线程下载===========");

            HttpUtils.RangePart rangePart = HttpUtils.parseRangePart(response);
            long total = rangePart != null ? rangePart.getTotal() : 0;
            setTotalLength(total);

            response.getInputStream().close();

            if (call.isCanceled()) {
                return;
            }
            List<Part> parts = splitParts(total, partCount);
            for (int i = 0; i < parts.size(); i++) {
                Part part = parts.get(i);
                partTasks.add(createPartTask(i, part.start, part.end));
            }

            new PartTaskSync(partTasks) {

                @Override
                protected void onProgress(long current) {
                    notifyProgress(current);
                }

                @Override
                protected void onFail(Throwable e) {
                    notifyFail(e);
                }

                @Override
                protected void onComplete() throws IOException {
                    for (PartTask partTask : partTasks) {
                        File tempFile = new File(partTask.getSavePath());
                        LogUtils.i(TAG, "==========tempFile===" + tempFile.length());
                        FileUtils.writeToFileFrom(new FileInputStream(tempFile), file, partTask.getStart(), null);
                    }
                    LogUtils.i(TAG, "=========file====" + file.length() + "======getTotalLength====" + getTotalLength());
                    notifyComplete();
                }

            }.start();

        } else {
            throw new IllegalArgumentException("response code error : " + responseCode);
        }
    }

    @Override
    protected void onPause() {
        if (call != null) {
            call.cancel();
            call = null;
        }
        for (int i = 0; i < partTasks.size(); i++) {
            partTasks.get(i).pause();
        }
    }

    @Override
    protected void onDelete() {
        super.onDelete();
        for (int i = 0; i < partTasks.size(); i++) {
            partTasks.get(i).delete();
        }
    }


    private void setRangeParams(Request.Builder builder) {
        HttpUtils.setRangeParams(new OkHttpRequestPropertySetter(builder), 0);
    }

    private PartTask createPartTask(int index, long start, long end) {
        String savePath = getSaveDir() + "/" + getFileName() + ".part" + partCount + "_" + index;
        return new OkHttpPartTask(getUrl(), savePath, start, end, context);
    }


    private static List<Part> splitParts(long total, int partCount) {
        List<Part> parts = new ArrayList<>();
        long start;
        long end;
        if (total % partCount == 0) {
            long eachLength = total / partCount;
            for (int i = 0; i < partCount; i++) {
                start = eachLength * i;
                end = start + eachLength;
                LogUtils.i(TAG, "===========part===start=" + start + ";end=" + end);

                parts.add(new Part(start, end));
            }
        } else {

            long eachLength = total / partCount;
            for (int i = 0; i < partCount; i++) {
                start = eachLength * i;
                if (i == partCount - 1) {
                    end = total - 1;
                } else {
                    end = start + eachLength;
                }
                LogUtils.i(TAG, "===========part===start=" + start + ";end=" + end);

                parts.add(new Part(start, end));
            }
        }
        return parts;
    }

    static class Part {

        private long start;
        private long end;

        Part(long start, long end) {
            this.start = start;
            this.end = end;
        }
    }
}
