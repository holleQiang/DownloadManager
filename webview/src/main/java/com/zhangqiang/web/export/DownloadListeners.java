package com.zhangqiang.web.export;

import com.zhangqiang.web.WebContext;

public class DownloadListeners extends BaseListeners<DownloadListener>{


    public void dispatchDownloadStart(WebContext webContext,
                                      String url,
                                      String userAgent,
                                      String contentDisposition,
                                      String mimetype,
                                      long contentLength) {
        doTraversal(new TraversalFunc<DownloadListener>() {
            @Override
            public void run(DownloadListener downloadListener) {
                downloadListener.onDownloadStart(webContext,
                        url,
                        userAgent,
                        contentDisposition,
                        mimetype,
                        contentLength);
            }
        });
    }
}
