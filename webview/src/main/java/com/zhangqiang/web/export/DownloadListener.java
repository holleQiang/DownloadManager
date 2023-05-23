package com.zhangqiang.web.export;

import com.zhangqiang.web.WebContext;

public interface DownloadListener {

    void onDownloadStart(WebContext webContext, String url, String userAgent, String contentDisposition, String mimetype, long contentLength);
}
