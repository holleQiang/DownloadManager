package com.zhangqiang.sample;

import android.app.Application;
import android.content.Intent;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.keystore.Options;
import com.zhangqiang.sample.service.DownloadService;

public class DMApplication extends Application {

    private static DMApplication application;

    public static DMApplication get() {
        return application;
    }

    @Override
    public void onCreate() {
        application = this;
        super.onCreate();
        DownloadManager.getInstance().init(this);
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
    }
}
