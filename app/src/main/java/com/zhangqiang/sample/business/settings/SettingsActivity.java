package com.zhangqiang.sample.business.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.common.utils.BaseObserver;
import com.zhangqiang.common.utils.RXJavaUtils;
import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.business.settings.plugins.PluginInfoCell;
import com.zhangqiang.sample.databinding.ActivitySettingsBinding;
import com.zhangqiang.sample.manager.SettingsManager;

import java.util.ArrayList;
import java.util.List;
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
                .compose(RXJavaUtils.<Integer>bindLifecycle(this))
                .subscribe(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        mBinding.etMaxRunningTaskSize.setText(String.valueOf(integer));
                    }
                });
        SettingsManager.getInstance().getSaveDirOption().toObservable()
                .compose(RXJavaUtils.<String>bindLifecycle(this))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        mBinding.etSaveDir.setText(s);
                    }
                });

        SettingsManager.getInstance().getHttpDownloadThreadSize().toObservable()
                .compose(RXJavaUtils.bindLifecycle(this)).subscribe(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        mBinding.etThreadSize.setText(String.valueOf(integer));
                    }
                });
        SettingsManager.getInstance().getDebugMode().toObservable().compose(RXJavaUtils.bindLifecycle(this))
                        .subscribe(new BaseObserver<Boolean>() {
                            @Override
                            public void onNext(Boolean aBoolean) {
                                mBinding.cbDebugMode.setChecked(aBoolean);
                            }
                        });
        initialPluginInfo();
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
            String threadSize = mBinding.etThreadSize.getText().toString();
            if (!TextUtils.isEmpty(threadSize)) {
                SettingsManager.getInstance().getHttpDownloadThreadSize().set(Integer.valueOf(threadSize));
            }
            SettingsManager.getInstance().getDebugMode().set(mBinding.cbDebugMode.isChecked());
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isValidDir(String saveDir) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9/]+");
        return pattern.matcher(saveDir).matches();
    }

    private void initialPluginInfo() {
        CellRVAdapter pluginsAdapter = new CellRVAdapter();
        mBinding.rvPlugins.setAdapter(pluginsAdapter);
        mBinding.rvPlugins.setLayoutManager(new LinearLayoutManager(this));
        int pluginCount = DownloadManager.getInstance().getPluginCount();
        List<PluginInfoCell> pluginInfoCells = new ArrayList<>();
        for (int i = 0; i < pluginCount; i++) {
            pluginInfoCells.add(new PluginInfoCell(DownloadManager.getInstance().getPluginAt(i)));
        }
        pluginsAdapter.setDataList(pluginInfoCells);
    }
}
