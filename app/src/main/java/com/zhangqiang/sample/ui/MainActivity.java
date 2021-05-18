package com.zhangqiang.sample.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.config.Configs;
import com.zhangqiang.sample.service.DownloadService;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private EditText etUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        setContentView(R.layout.activity_main);
        etUrl = findViewById(R.id.et_url);
        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    return performDownload();
                }
//                etUrl.getText().clear();
                return false;
            }
        });
        etUrl.setText("https://imtt.dd.qq.com/16891/apk/847A5ED16C396C7767FF4987915AAB06.apk?fsname=com.qq.reader_7.5.8.666_174.apk&csr=1bbd");
        findViewById(R.id.bt_go).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                performDownload();
            }
        });

        DownloadManagerFragment downloadManagerFragment = new DownloadManagerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container, downloadManagerFragment)
                .commit();

        handDownloadIntent();
    }

    private boolean performDownload() {
        CharSequence text = etUrl.getText();
        if (TextUtils.isEmpty(text)) {
            return false;
        }
        download(text.toString());
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handDownloadIntent();
    }

    private void handDownloadIntent() {
        String link = getIntent().getStringExtra("link");
        if (!TextUtils.isEmpty(link)) {
            etUrl.setText(link);
            etUrl.requestFocus();
            etUrl.setSelection(link.length());
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (inputMethodManager != null) {
                inputMethodManager.showSoftInput(etUrl, InputMethodManager.SHOW_IMPLICIT);
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
        DownloadManager.getInstance(this).download(url, 2, saveDir);
    }
}
