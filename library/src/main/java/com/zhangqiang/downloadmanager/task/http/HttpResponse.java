package com.zhangqiang.downloadmanager.task.http;

import com.zhangqiang.downloadmanager.utils.HttpUtils;

import java.io.IOException;
import java.io.InputStream;

public interface HttpResponse extends HttpUtils.HeaderFieldOwner{

    String getContentType();

    int getResponseCode() throws IOException;

    long getContentLength();

    InputStream getInputStream() throws IOException;

    void close();
}
