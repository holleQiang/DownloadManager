package com.zhangqiang.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.View;

import com.zhangqiang.filechooser.FileChooserFragment;
import com.zhangqiang.sample.base.BaseActivity;

public class ChooseSaveDirActivity extends BaseActivity {

    private FileChooserFragment fileChooserFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_dir);
        fileChooserFragment = new FileChooserFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, fileChooserFragment)
                .commit();
        findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String currentPath = fileChooserFragment.getCurrentPath();
                Intent intent = getIntent();
                intent.putExtra("path", currentPath);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (fileChooserFragment.backward()) {
                return true;
            }
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

        }
        return super.onKeyDown(keyCode, event);
    }
}
