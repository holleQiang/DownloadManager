package com.zhangqiang.sample;

import androidx.multidex.MultiDexApplication;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.sample.business.container.ContainerProcessorManager;
import com.zhangqiang.sample.business.container.processor.HttpProcessor;
import com.zhangqiang.sample.business.container.processor.QRCodeProcessor;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.ui.dialog.CreateTaskDialog;
import com.zhangqiang.web.WebContext;
import com.zhangqiang.web.export.DownloadListener;
import com.zhangqiang.web.export.OnImageClickListener;
import com.zhangqiang.web.export.WebInterface;

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
        ContainerProcessorManager.getInstance().registerProcessor(new QRCodeProcessor());
        ContainerProcessorManager.getInstance().registerProcessor(new HttpProcessor());

        WebInterface.onImageClickListeners.add(new OnImageClickListener() {
            @Override
            public void onImageClick(WebContext webContext, String src) {
                CreateTaskDialog.createAndShow(webContext.getFragmentManager(),src);
            }
        });
        WebInterface.downloadListeners.add(new DownloadListener() {
            @Override
            public void onDownloadStart(WebContext webContext, String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                CreateTaskDialog.createAndShow(webContext.getFragmentManager(),url);
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
