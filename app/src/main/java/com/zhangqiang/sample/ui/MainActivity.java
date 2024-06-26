package com.zhangqiang.sample.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.m3u8.request.M3u8DownloadRequest;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.qrcodescan.HttpProcessor;
import com.zhangqiang.qrcodescan.Processor;
import com.zhangqiang.qrcodescan.QRCodeScanActivity;
import com.zhangqiang.qrcodescan.QRCodeScanManager;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.business.settings.SettingsActivity;
import com.zhangqiang.sample.databinding.ActivityMainBinding;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.ui.dialog.TaskCreateByLinkDialog;
import com.zhangqiang.sample.utils.IntentUtils;
import com.zhangqiang.sample.utils.QRCodeResultProcessUtils;
import com.zhangqiang.web.manager.WebManager;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class MainActivity extends BaseActivity {


    private ActivityMainBinding mBinding;
    private String pendingScanUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.mToolBar);

        DownloadManagerFragment downloadManageFragment = new DownloadManagerFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container, downloadManageFragment)
                .commit();

        handDownloadIntent();

        QRCodeScanManager.Companion.getInstance().addProcessor(mHttpProcessor);
        QRCodeScanManager.Companion.getInstance().addProcessor(fallbackProcessor);

        int scaledTouchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        LogUtils.i("Test","------------"+scaledTouchSlop);
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
                String scheme = data.getScheme();
                if ("http".equals(scheme)
                        || "https".equals(scheme)
                        || "ftp".equals(scheme)) {
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
            WebManager.getInstance().openWebViewActivity(this,null);
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
        QRCodeScanManager.Companion.getInstance().removeProcessor(fallbackProcessor);
    }

    private void showTaskCreateDialog(String url) {
        TaskCreateByLinkDialog.newInstance(url).show(getSupportFragmentManager(), "task_create_by_link");
    }

    private final Processor mHttpProcessor = new HttpProcessor() {
        @Override
        public void processHttpUrls(@NotNull List<String> urls) {
            if (getLifecycle().getCurrentState() != Lifecycle.State.RESUMED) {
                pendingScanUrl = urls.get(0);
            } else {
                processHttpUrl(urls.get(0));
            }
        }
    };

    private void processHttpUrl(String url) {
        QRCodeResultProcessUtils.processHttpUrl(this, url);
    }

    private final Processor fallbackProcessor = new Processor() {
        @Override
        public boolean process(@NonNull String text) {
            try {
                IntentUtils.openActivityByUri(MainActivity.this, Uri.parse(text));
            } catch (Throwable e) {
                Toast.makeText(MainActivity.this, getString(R.string.cannot_found_app_to_process), Toast.LENGTH_SHORT).show();
            }
            return false;
        }
    };
}
