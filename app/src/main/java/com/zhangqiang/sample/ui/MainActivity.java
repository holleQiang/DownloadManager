package com.zhangqiang.sample.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.task.ftp.request.FTPDownloadRequest;
import com.zhangqiang.qrcodescan.HttpProcessor;
import com.zhangqiang.qrcodescan.Processor;
import com.zhangqiang.qrcodescan.QRCodeScanActivity;
import com.zhangqiang.qrcodescan.QRCodeScanManager;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.business.settings.SettingsActivity;
import com.zhangqiang.sample.business.web.WebViewActivity;
import com.zhangqiang.sample.databinding.ActivityMainBinding;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.service.DownloadService;
import com.zhangqiang.sample.ui.dialog.CreateTaskDialog;
import com.zhangqiang.sample.utils.IntentUtils;
import com.zhangqiang.sample.utils.QRCodeResultProcessUtils;
import com.zhangqiang.sample.utils.WebViewUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class MainActivity extends BaseActivity {


    private ActivityMainBinding mBinding;
    private String pendingScanUrl;

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
        QRCodeScanManager.Companion.getInstance().addProcessor(new Processor() {
            @Override
            public boolean process(@NonNull String text) {
                try {
                    IntentUtils.openActivityByUri(MainActivity.this,Uri.parse(text));
                }catch (Throwable e){
                    Toast.makeText(MainActivity.this, getString(R.string.cannot_found_app_to_process), Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        });

//        File dirFile = new File(Environment.getExternalStorageDirectory(), SettingsManager.getInstance().getSaveDir());
//        FTPDownloadRequest request = new FTPDownloadRequest.Builder()
//                .setHost("10.93.45.211")
//                .setPort(21)
//                .setUserName("mobile")
//                .setPassword("test")
//                .setFtpDir("/test")
//                .setFtpFileName("utils.ts")
//                .setSaveDir(dirFile.getAbsolutePath())
//                .build();
//        DownloadManager.getInstance(this).enqueue(request);
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
        if (pendingScanUrl != null) {
            processHttpUrl(pendingScanUrl);
            pendingScanUrl = null;
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
            showTaskCreateDialog("");
            return true;
        } else if (item.getItemId() == R.id.web_view) {
            startActivity(new Intent(this, WebViewActivity.class));
        } else if (item.getItemId() == R.id.scan_qr_code) {
            startActivity(new Intent(this, QRCodeScanActivity.class));
        } else if (item.getItemId() == R.id.choose_image_with_qrcode) {
            IntentUtils.openChooseImagePage(this, getContentResolver(), new IntentUtils.ChooseImagePageCallback() {
                @Override
                public void onChooseImage(String imageFilePath) {
                    IntentUtils.openQRCodeDecodePage(MainActivity.this, imageFilePath);
                }
            });
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
        CreateTaskDialog.createAndShow(getSupportFragmentManager(), url);
    }

    private final Processor mHttpProcessor = new HttpProcessor() {
        @Override
        public void processHttpUrls(@NotNull List<String> urls) {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
                pendingScanUrl = urls.get(0);
            } else {
                processHttpUrl(urls.get(0));
                WebViewUtils.open(MainActivity.this, urls.get(0));
            }
        }
    };

    private void processHttpUrl(String url) {
        QRCodeResultProcessUtils.processHttpUrl(this,url);
    }

}
