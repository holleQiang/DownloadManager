package com.zhangqiang.downloadmanager.task.http.okhttp;

import com.zhangqiang.downloadmanager.task.http.HttpResponse;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpResponse implements HttpResponse {

    private final Response response;

    public OkHttpResponse(Response response) {
        this.response = response;
    }

    @Override
    public String getContentType() {
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        }
        MediaType mediaType = body.contentType();
        if (mediaType != null) {
            return mediaType.toString();
        }
        return null;
    }

    @Override
    public int getResponseCode() throws IOException {
        return response.code();
    }

    @Override
    public long getContentLength() {
        ResponseBody body = response.body();
        if (body == null) {
            return 0;
        }
        return body.contentLength();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            return null;
        }
        return body.byteStream();
    }

    @Override
    public String getField(String key) {
        return response.header(key);
    }

    @Override
    public void close() {
        response.close();
    }
}
