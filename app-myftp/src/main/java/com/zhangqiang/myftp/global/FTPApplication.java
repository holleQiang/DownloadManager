package com.zhangqiang.myftp.global;

import android.app.Application;

import com.zhangqiang.myftp.account.AccountService;
import com.zhangqiang.myftp.session.SessionService;

public class FTPApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AccountService.init(this);
        SessionService.init();
    }
}
