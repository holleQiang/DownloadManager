package com.zhangqiang.downloadmanager.utils;

import okhttp3.Call;

public class OKHttpUtils {

    public static void cancelCall(Call call){
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }
}
