package com.zhangqiang.downloadmanager.task.http.part;

import java.util.List;

public interface HttpPartTaskFactory {

    List<HttpDownloadPartTask> onCreateHttpPartTask(String url, PartInfo partInfo);
}
