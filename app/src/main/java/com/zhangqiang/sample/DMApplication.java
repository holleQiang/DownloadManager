package com.zhangqiang.sample;

import android.content.Intent;
import android.os.Environment;

import androidx.multidex.MultiDexApplication;

import com.zhangqiang.common.utils.BaseObserver;
import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.ftp.FtpDownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.http.HttpDownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.limit.ActiveTaskCountLimitPlugin;
import com.zhangqiang.downloadmanager.plugin.restart.RestartWhenNetworkAvailablePlugin;
import com.zhangqiang.sample.business.container.ContainerProcessorManager;
import com.zhangqiang.sample.business.container.processor.FtpProtocolProcessor;
import com.zhangqiang.sample.business.container.processor.HttpProcessor;
import com.zhangqiang.sample.business.container.processor.QRCodeProcessor;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.plugins.media.refresh.MediaRefreshPlugin;
import com.zhangqiang.sample.service.DownloadService;
import com.zhangqiang.sample.utils.ProcessUtils;
import com.zhangqiang.sample.web.DownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.m3u8.M3u8DownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.m3u8.request.M3u8DownloadRequest;
import com.zhangqiang.web.hybrid.plugins.M3u8PickPlugin;
import com.zhangqiang.web.manager.WebManager;

import java.io.File;

public class DMApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();


        if (ProcessUtils.isMainProcess(this)) {

            ActiveTaskCountLimitPlugin activeTaskCountLimitPlugin = new ActiveTaskCountLimitPlugin();
            SettingsManager.getInstance().init(this);
            SettingsManager.getInstance().getMaxRunningTaskCountOption().toObservable()
                    .subscribe(new BaseObserver<Integer>() {
                        @Override
                        public void onNext(Integer integer) {
                            activeTaskCountLimitPlugin.setMaxActiveTaskCount(integer);
                        }
                    });

            //从网络可用中恢复插件
            DownloadManager.getInstance().registerPlugin(activeTaskCountLimitPlugin);
            DownloadManager.getInstance().registerPlugin(new RestartWhenNetworkAvailablePlugin(this));
            DownloadManager.getInstance().registerPlugin(new HttpDownloadPlugin(this));
            DownloadManager.getInstance().registerPlugin(new FtpDownloadPlugin(this));
            DownloadManager.getInstance().registerPlugin(new MediaRefreshPlugin(this));
            DownloadManager.getInstance().registerPlugin(new M3u8DownloadPlugin(this));

            try {
                Intent intent = new Intent(this, DownloadService.class);
                startService(intent);
            } catch (Throwable e) {
                e.printStackTrace();
            }


            WebManager.getInstance().registerPlugin(new DownloadPlugin());
            WebManager.getInstance().registerPlugin(new M3u8PickPlugin(new M3u8PickPlugin.Callback() {
                @Override
                public void onReceiveM3u8Resource(String url) {
                    File saveDir = new File(Environment.getExternalStorageDirectory(), SettingsManager.getInstance().getSaveDir());
                    DownloadManager.getInstance().enqueue(new M3u8DownloadRequest(saveDir.getAbsolutePath(), null, url));
                }
            }));
            WebManager.getInstance().applyPlugins();
        }

        ContainerProcessorManager.getInstance().registerProcessor(new QRCodeProcessor());
        ContainerProcessorManager.getInstance().registerProcessor(new HttpProcessor());
        ContainerProcessorManager.getInstance().registerProcessor(new FtpProtocolProcessor());
    }
}
