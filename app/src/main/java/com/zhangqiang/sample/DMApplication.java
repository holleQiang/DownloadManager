package com.zhangqiang.sample;

import androidx.multidex.MultiDexApplication;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.ftp.FtpDownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.http.HttpDownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.restart.RestartWhenNetworkAvailablePlugin;
import com.zhangqiang.sample.business.container.ContainerProcessorManager;
import com.zhangqiang.sample.business.container.processor.FtpProtocolProcessor;
import com.zhangqiang.sample.business.container.processor.HttpProcessor;
import com.zhangqiang.sample.business.container.processor.QRCodeProcessor;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.utils.ProcessUtils;
import com.zhangqiang.sample.web.DownloadPlugin;
import com.zhangqiang.web.manager.WebManager;

public class DMApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        SettingsManager.getInstance().init(this);
        SettingsManager.getInstance().getMaxRunningTaskCountOption().toObservable()
                .subscribe(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
//                        getDownloadManager().setMaxRunningTaskCount(integer);
                    }
                });


        String processName = getApplicationContext().getApplicationInfo().processName;

        if(ProcessUtils.isMainProcess(this)){
            //从网络可用中恢复插件
            DownloadManager.getInstance().registerPlugin(new RestartWhenNetworkAvailablePlugin(this));
            DownloadManager.getInstance().registerPlugin(new HttpDownloadPlugin(this));
            DownloadManager.getInstance().registerPlugin(new FtpDownloadPlugin(this));
        }

        ContainerProcessorManager.getInstance().registerProcessor(new QRCodeProcessor());
        ContainerProcessorManager.getInstance().registerProcessor(new HttpProcessor());
        ContainerProcessorManager.getInstance().registerProcessor(new FtpProtocolProcessor());

        WebManager.getInstance().registerPlugin(new DownloadPlugin());
    }
}
