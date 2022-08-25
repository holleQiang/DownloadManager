package com.zhangqiang.sample.business.container;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.zhangqiang.sample.base.BaseActivity;

public class ContainerActivity  extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!ContainerProcessorManager.getInstance().dispatchActivityCreate(this)) {
            finish();
        }
    }
}
