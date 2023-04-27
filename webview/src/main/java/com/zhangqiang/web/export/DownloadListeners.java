package com.zhangqiang.web.export;

import java.util.ArrayList;
import java.util.List;

public class DownloadListeners {

    private List<DownloadListener> downloadListeners = null;

    public synchronized void add(DownloadListener downloadListener){
        if(downloadListeners == null){
            downloadListeners = new ArrayList<>();
        }
        if (downloadListeners.contains(downloadListener)) {
            return;
        }
        downloadListeners.add(downloadListener);
    }

    public synchronized void remove(DownloadListener downloadListener){
        if (downloadListeners == null) {
            return;
        }
        downloadListeners.remove(downloadListener);
    }

    public void dispatchDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
        for (int i = downloadListeners.size() - 1; i >= 0; i--) {
            downloadListeners.get(i).onDownloadStart(url,userAgent,contentDisposition,mimetype,contentLength);
        }
    }
}
