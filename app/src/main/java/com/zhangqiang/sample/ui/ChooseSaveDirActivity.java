package com.zhangqiang.sample.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;

import com.zhangqiang.filechooser.FileChooserFragment;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseActivity;

public class ChooseSaveDirActivity extends BaseActivity {

    private FileChooserFragment fileChooserFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.choose_save_dir);
        setContentView(R.layout.activity_choose_dir);
        fileChooserFragment = new FileChooserFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, fileChooserFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.choose_save_dir,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.bt_confirm) {
            String currentPath = fileChooserFragment.getCurrentPath();
            Intent intent = getIntent();
            intent.putExtra("path", currentPath);
            setResult(RESULT_OK, intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
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


}
