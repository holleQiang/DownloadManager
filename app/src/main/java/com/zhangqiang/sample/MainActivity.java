package com.zhangqiang.sample;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.db.DBManager;
import com.zhangqiang.downloadmanager.task.DownloadListener;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.utils.StringUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private EditText etUrl;
    private DownloadManagerFragment downloadManagerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etUrl = findViewById(R.id.et_url);
        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    CharSequence text = v.getText();
                    if (TextUtils.isEmpty(text)) {
                        return false;
                    }
                    download(text.toString());
                }
                etUrl.getText().clear();
                return false;
            }
        });

        downloadManagerFragment = new DownloadManagerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container, downloadManagerFragment)
                .commit();

        String link = getIntent().getStringExtra("link");
        if (!TextUtils.isEmpty(link)) {
            etUrl.setText(link);
            etUrl.requestFocus();
            etUrl.setSelection(link.length());
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(etUrl,InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void download(String url) {
        String saveDir = Configs.saveDir.get();
        if (TextUtils.isEmpty(saveDir)) {
            saveDir = new File(getFilesDir(), "download").getAbsolutePath();
        }
        DownloadTask task = DownloadManager.getInstance().download(url, saveDir);

        downloadManagerFragment.refresh();
    }
}
