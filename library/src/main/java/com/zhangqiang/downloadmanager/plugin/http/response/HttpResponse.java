package com.zhangqiang.downloadmanager.plugin.http.response;

import com.zhangqiang.downloadmanager.plugin.http.utils.FieldGetter;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface HttpResponse extends Closeable, FieldGetter {

    boolean isSuccess();

    String getContentType();

    int getResponseCode();

    long getContentLength();

    InputStream getInputStream() throws IOException;
}
