package com.zhangqiang.sample;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Intent;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.sample.service.DownloadService;

import java.util.List;

public class DMApplication extends Application {

    private static DMApplication application;

    public static DMApplication get() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
    }
}
