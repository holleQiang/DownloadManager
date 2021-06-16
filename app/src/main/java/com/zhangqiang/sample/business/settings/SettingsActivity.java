package com.zhangqiang.sample.business.settings;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.utils.RxJavaUtils;

import java.util.regex.Pattern;

public class SettingsActivity extends BaseActivity {

    private EditText etMaxRunningTaskSize;
    private EditText etSaveDir;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolBar = findViewById(R.id.m_tool_bar);
        setSupportActionBar(toolBar);
        etMaxRunningTaskSize = findViewById(R.id.et_max_running_task_size);
        etSaveDir = findViewById(R.id.et_save_dir);

        SettingsManager.getInstance().getMaxRunningTaskCountOption().toObservable()
                .compose(RxJavaUtils.<Integer>bindLifecycle(this))
                .subscribe(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        etMaxRunningTaskSize.setText(String.valueOf(integer));
                    }
                });
        SettingsManager.getInstance().getSaveDirOption().toObservable()
                .compose(RxJavaUtils.<String>bindLifecycle(this))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        etSaveDir.setText(s);
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
            String s = etMaxRunningTaskSize.getText().toString();
            if (!TextUtils.isEmpty(s)) {
                SettingsManager.getInstance().getMaxRunningTaskCountOption().set(Integer.valueOf(s));
            }
            String saveDir = etSaveDir.getText().toString();
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
