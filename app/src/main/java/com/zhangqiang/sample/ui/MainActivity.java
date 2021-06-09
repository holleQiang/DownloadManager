package com.zhangqiang.sample.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.service.DownloadService;
import com.zhangqiang.sample.ui.dialog.CreateTaskDialog;
import com.zhangqiang.sample.ui.dialog.TestBottomSheetDialog;
import com.zhangqiang.sample.ui.dialog.TestDialog;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.m_tool_bar);
        setSupportActionBar(toolbar);

        DownloadManageFragment downloadManageFragment = new DownloadManageFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container, downloadManageFragment)
                .commit();

        handDownloadIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handDownloadIntent();
    }

    private void handDownloadIntent() {
        String link = getIntent().getStringExtra("link");
        if (TextUtils.isEmpty(link)) {
            Uri data = getIntent().getData();
            if (data != null) {
                if ("http".equals(data.getScheme()) || "https".equals(data.getScheme())) {
                    link = data.toString();
                }
            }
        }
        if (!TextUtils.isEmpty(link)) {
            showTaskCreateDialog(link);
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
        } else if (item.getItemId() == R.id.create_task) {
            showTaskCreateDialog("https://imtt.dd.qq.com/16891/apk/5C0FF221A948463BCF9F3255E0112034.apk?fsname=com.tencent.mm_8.0.6_1900.apk&csr=1bbd");
            return true;
        }else if(item.getItemId() == R.id.test){
            new TestDialog().show(getSupportFragmentManager(),"test");
            return true;
        }else if(item.getItemId() == R.id.test_sheet){
            new TestBottomSheetDialog().show(getSupportFragmentManager(),"testSheet");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showTaskCreateDialog(String url) {
        CreateTaskDialog dialog = new CreateTaskDialog();
        Bundle arg = new Bundle();
        arg.putString("url", url);
        dialog.setArguments(arg);
        dialog.show(getSupportFragmentManager(), "create_task");
    }
}
