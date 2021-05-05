package com.zhangqiang.downloadmanager.task.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public interface HttpResponse extends FieldGetter, Closeable {

    String getContentType();

    int getResponseCode() throws IOException;

    long getContentLength();

    InputStream getInputStream() throws IOException;

}
