package com.zhangqiang.sample;

import androidx.multidex.MultiDexApplication;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.manager.SettingsManager;

public class DMApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        getSettingsManager().init(this);
        getSettingsManager().getMaxRunningTaskCountOption().toObservable()
                .subscribe(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        getDownloadManager().setMaxRunningTaskCount(integer);
                    }
                });
    }

    private SettingsManager getSettingsManager() {
        return SettingsManager.getInstance();
    }

    private DownloadManager getDownloadManager() {
        return DownloadManager.getInstance(this);
    }
}
