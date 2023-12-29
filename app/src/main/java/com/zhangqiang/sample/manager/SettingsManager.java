package com.zhangqiang.sample.manager;

import android.content.Context;

import com.zhangqiang.options.Option;
import com.zhangqiang.options.Options;
import com.zhangqiang.options.store.ValueStore;
import com.zhangqiang.options.store.mmkv.MMKVValueStore;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-09
 */
public class SettingsManager {

    private static final SettingsManager instance = new SettingsManager();
    private final AtomicBoolean hasInit = new AtomicBoolean(false);
    private Option<Integer> maxRunningTaskCountOption;
    private Option<String> saveDirOption;
    private Option<Integer> httpDownloadThreadSize;
    private Option<Boolean> debugMode;

    private SettingsManager() {
    }

    public static SettingsManager getInstance() {
        return instance;
    }

    public void init(Context context) {
        if (hasInit.getAndSet(true)) {
            return;
        }
        context = context.getApplicationContext();
        ValueStore valueStore = new MMKVValueStore(context);
        maxRunningTaskCountOption = Options.ofInt("max_running_task_count", 3, valueStore);
        saveDirOption = Options.ofString("save_dir", "Download", valueStore);
        httpDownloadThreadSize = Options.ofInt("http_thread_size", 2, valueStore);
        debugMode = Options.ofBoolean("debug_mode", false, valueStore);
    }

    public Option<Integer> getMaxRunningTaskCountOption() {
        return maxRunningTaskCountOption;
    }

    public Option<String> getSaveDirOption() {
        return saveDirOption;
    }

    public int getMaxRunningTaskCount() {
        return getMaxRunningTaskCountOption().get();
    }

    public String getSaveDir() {
        return getSaveDirOption().get();
    }

    public Option<Integer> getHttpDownloadThreadSize() {
        return httpDownloadThreadSize;
    }

    public Option<Boolean> getDebugMode() {
        return debugMode;
    }
}
