package com.zhangqiang.sample.base;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import com.zhangqiang.visiblehelper.ActivityVisibleHelper;
import com.zhangqiang.visiblehelper.VisibleHelper;
import com.zhangqiang.visiblehelper.VisibleHelperOwner;

public abstract class BaseActivity extends AppCompatActivity implements VisibleHelperOwner {

    private ActivityVisibleHelper visibleHelper = new ActivityVisibleHelper();

    @NonNull
    @Override
    public VisibleHelper getVisibleHelper() {
        return visibleHelper;
    }

    @Override
    protected void onStart() {
        super.onStart();
        visibleHelper.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        visibleHelper.onStop();
    }
}
