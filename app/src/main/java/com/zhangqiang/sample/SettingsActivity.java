package com.zhangqiang.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.zhangqiang.activitystart.ActivityStartHelper;
import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.config.Configs;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.utils.RxJavaUtils;

public class SettingsActivity extends BaseActivity {

    private TextView tvCurrentPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        RadioGroup radioGroup = findViewById(R.id.rg_thread_num);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_thread_1) {
                    Configs.threadNum.set(1);
                } else if (checkedId == R.id.rb_thread_2) {
                    Configs.threadNum.set(2);
                } else if (checkedId == R.id.rb_thread_3) {
                    Configs.threadNum.set(3);
                }
            }
        });
        RadioButton radioButton1 = findViewById(R.id.rb_thread_1);
        RadioButton radioButton2 = findViewById(R.id.rb_thread_2);
        RadioButton radioButton3 = findViewById(R.id.rb_thread_3);
        if (Configs.threadNum.get() == 1) {
            radioButton1.setChecked(true);
        } else if (Configs.threadNum.get() == 2) {
            radioButton2.setChecked(true);
        }
        if (Configs.threadNum.get() == 3) {
            radioButton3.setChecked(true);
        }

        Configs.saveDir.toObservable()
                .compose(RxJavaUtils.<String>bindLifecycle(this))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String value) {
                        super.onNext(value);
                        tvCurrentPath.setText(value);
                    }
                });
        tvCurrentPath = findViewById(R.id.tv_current_path);
        View.OnClickListener chooseDirListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(v.getContext(), ChooseSaveDirActivity.class);
                ActivityStartHelper.startActivityForResult(SettingsActivity.this, intent, new ActivityStartHelper.Callback() {
                    @Override
                    public void onActivityResult(int resultCode, Intent data) {
                        if (resultCode == RESULT_OK) {
                            String path = data.getStringExtra("path");
                            Configs.saveDir.set(path);
                        }
                    }
                });
            }
        };
        tvCurrentPath.setOnClickListener(chooseDirListener);
        findViewById(R.id.tv_current_path_title).setOnClickListener(chooseDirListener);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
