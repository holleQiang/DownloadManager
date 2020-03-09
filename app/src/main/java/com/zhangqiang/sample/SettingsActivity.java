package com.zhangqiang.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsActivity extends BaseActivity {

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
    }


}
