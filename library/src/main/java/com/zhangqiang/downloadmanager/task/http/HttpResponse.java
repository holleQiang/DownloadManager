package com.zhangqiang.downloadmanager.task.http;

import java.io.IOException;
import java.io.InputStream;

public interface HttpResponse extends FieldGetter{

    String getContentType();

    int getResponseCode() throws IOException;

    long getContentLength();

    InputStream getInputStream() throws IOException;

    void close();
}
