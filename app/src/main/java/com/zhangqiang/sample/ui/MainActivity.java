package com.zhangqiang.sample.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Lifecycle;

import com.zhangqiang.qrcodescan.HttpProcessor;
import com.zhangqiang.qrcodescan.Processor;
import com.zhangqiang.qrcodescan.QRCodeScanActivity;
import com.zhangqiang.qrcodescan.QRCodeScanManager;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.business.settings.SettingsActivity;
import com.zhangqiang.sample.business.web.WebViewActivity;
import com.zhangqiang.sample.databinding.ActivityMainBinding;
import com.zhangqiang.sample.service.DownloadService;
import com.zhangqiang.sample.ui.dialog.CreateTaskDialog;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private ActivityMainBinding mBinding;
    private String pendingUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(this, DownloadService.class);
        startService(intent);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setSupportActionBar(mBinding.mToolBar);

        DownloadManageFragment downloadManageFragment = new DownloadManageFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container, downloadManageFragment)
                .commit();

        handDownloadIntent();

        QRCodeScanManager.Companion.getInstance().addProcessor(mHttpProcessor);


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
    protected void onResume() {
        super.onResume();
        if (pendingUrl != null) {
            showTaskCreateDialog(pendingUrl);
            pendingUrl = null;
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
        }else if(item.getItemId() == R.id.web_view){
            startActivity(new Intent(this, WebViewActivity.class));
        }else if(item.getItemId() == R.id.scan_qr_code){
            startActivity(new Intent(this, QRCodeScanActivity.class));
        }
//        else if(item.getItemId() == R.id.test){
//            new TestDialog().show(getSupportFragmentManager(),"test");
//            return true;
//        }else if(item.getItemId() == R.id.test_sheet){
//            new TestBottomSheetDialog().show(getSupportFragmentManager(),"testSheet");
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        QRCodeScanManager.Companion.getInstance().removeProcessor(mHttpProcessor);
    }

    private void showTaskCreateDialog(String url) {
        CreateTaskDialog.createAndShow(getSupportFragmentManager(),url);
    }

    private final Processor mHttpProcessor = new HttpProcessor() {
        @Override
        public void processHttpUrls(@NotNull List<String> urls) {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
                pendingUrl = urls.get(0);
            }else {
                showTaskCreateDialog(urls.get(0));
            }
        }
    };
}
