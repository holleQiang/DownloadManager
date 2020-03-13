package com.zhangqiang.sample.config;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.keystore.Option;
import com.zhangqiang.keystore.Options;
import com.zhangqiang.keystore.store.ValueStore;
import com.zhangqiang.keystore.store.mmkv.MMKVValueStore;
import com.zhangqiang.sample.DMApplication;
import com.zhangqiang.sample.impl.BaseObserver;

public class Configs {

    public static final String CONFIG_FILE_NAME = "config";
//    private static final ValueStore valueStore= new SharedValueStore(DMApplication.get(),CONFIG_FILE_NAME);
    private static final ValueStore valueStore= new MMKVValueStore(DMApplication.get());
    public static final Option<Integer> threadNum = Options.ofInt("thread_count", 1,valueStore);
    public static final Option<String> saveDir = Options.ofString("save_dir", null,valueStore);

    static {

        threadNum.toObservable().subscribe(new BaseObserver<Integer>(){
            @Override
            public void onNext(Integer value) {
                super.onNext(value);
                DownloadManager.getInstance().setPartSize(value);
            }
        });
    }
}
