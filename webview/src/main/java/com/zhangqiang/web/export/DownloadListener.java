package com.zhangqiang.web.export;

public interface DownloadListener {

    void onDownloadStart(WebContext webContext, String url, String userAgent, String contentDisposition, String mimetype, long contentLength);
}
