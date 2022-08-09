package com.zhangqiang.sample.business.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.databinding.ActivitySettingsBinding;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.utils.RxJavaUtils;

import java.util.regex.Pattern;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.mToolBar);

        SettingsManager.getInstance().getMaxRunningTaskCountOption().toObservable()
                .compose(RxJavaUtils.<Integer>bindLifecycle(this))
                .subscribe(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        mBinding.etMaxRunningTaskSize.setText(String.valueOf(integer));
                    }
                });
        SettingsManager.getInstance().getSaveDirOption().toObservable()
                .compose(RxJavaUtils.<String>bindLifecycle(this))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        mBinding.etSaveDir.setText(s);
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.bt_confirm) {
            String s = mBinding.etMaxRunningTaskSize.getText().toString();
            if (!TextUtils.isEmpty(s)) {
                SettingsManager.getInstance().getMaxRunningTaskCountOption().set(Integer.valueOf(s));
            }
            String saveDir = mBinding.etSaveDir.getText().toString();
            if (isValidDir(saveDir)) {
                SettingsManager.getInstance().getSaveDirOption().set(saveDir);
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isValidDir(String saveDir) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9/]+");
        return pattern.matcher(saveDir).matches();
    }
}
