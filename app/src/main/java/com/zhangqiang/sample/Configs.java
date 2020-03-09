package com.zhangqiang.sample;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.keystore.OnValueChangedListener;
import com.zhangqiang.keystore.Option;
import com.zhangqiang.keystore.Options;

public class Configs {

    public static final Option<Integer> threadNum = Options.ofInt("thread_count", 1);

    static {
        threadNum.addOnValueChangedListener(new OnValueChangedListener() {
            @Override
            public void onValueChanged() {
                DownloadManager.getInstance().setPartSize(threadNum.get());
            }
        });
    }
}
