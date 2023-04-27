package com.zhangqiang.common.activity;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.zhangqiang.common.dialog.loading.LoadingDialogHolder;
import com.zhangqiang.common.dialog.loading.LoadingDialogHolderImpl;
import com.zhangqiang.common.dialog.loading.LoadingDialogHolderOwner;
import com.zhangqiang.common.result.ActivityStarter;
import com.zhangqiang.common.result.ActivityStarterImpl;
import com.zhangqiang.common.result.ActivityStarterOwner;
import com.zhangqiang.visiblehelper.ActivityVisibleHelper;
import com.zhangqiang.visiblehelper.VisibleHelper;
import com.zhangqiang.visiblehelper.VisibleHelperOwner;

public abstract class BaseActivity extends AppCompatActivity implements VisibleHelperOwner,
        LoadingDialogHolderOwner,
        ActivityStarterOwner {

    private final ActivityVisibleHelper visibleHelper = new ActivityVisibleHelper();
    private final LoadingDialogHolderImpl mLoadingDialogHolder = new LoadingDialogHolderImpl(this);
    private final ActivityStarterImpl mActivityStarter = new ActivityStarterImpl(this);

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
    protected void onResume() {
        super.onResume();
        mLoadingDialogHolder.dispatchActivityResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        visibleHelper.onStop();
    }

    @Override
    public LoadingDialogHolder getLoadingDialogHolder() {
        return mLoadingDialogHolder;
    }

    @Override
    public ActivityStarter getActivityStarter() {
        return mActivityStarter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mActivityStarter.onActivityResult(requestCode, resultCode, data);
    }
}
