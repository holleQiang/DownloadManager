package com.zhangqiang.sample;

import android.app.Application;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.keystore.Options;

public class DMApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DownloadManager.getInstance().init(this);
        Options.init(this);
    }
}
