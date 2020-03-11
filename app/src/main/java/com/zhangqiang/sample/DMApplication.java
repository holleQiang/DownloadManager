package com.zhangqiang.sample;

import android.app.Application;
import android.content.Intent;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.keystore.Options;
import com.zhangqiang.sample.service.DownloadService;

public class DMApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.getInstance().init(this);
        Options.init(this);
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
    }
}
