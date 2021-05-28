package com.zhangqiang.sample.ui.dialog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.Request;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;

import java.io.File;

public class CreateTaskDialog extends BaseDialogFragment {

    private EditText etUrl;
    private EditText etThreadSize;
    private String defaultUrl = "https://imtt.dd.qq.com/16891/apk/847A5ED16C396C7767FF4987915AAB06.apk?fsname=com.qq.reader_7.5.8.666_174.apk&csr=1bbd";
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            defaultUrl =  arguments.getString("url");
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_task_create;
    }

    @Override
    protected void initView(View view) {
        etUrl = view.findViewById(R.id.et_url);
        etUrl.setText(defaultUrl);
        etThreadSize = view.findViewById(R.id.et_thread_size);
        view.findViewById(R.id.bt_enqueue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1000);
            }
        });
        Window window = this.getDialog().getWindow();
        if (window != null) {
            if (Build.VERSION.SDK_INT >= 21) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            String url = etUrl.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                return;
            }
            File dirFile = new File(Environment.getExternalStorageDirectory(), "Download");
            Request request = new Request.Builder(url, dirFile.getAbsolutePath())
                    .setThreadCount(Integer.parseInt(etThreadSize.getText().toString()))
                    .build();
            DownloadManager.getInstance(getContext()).enqueue(request);
            getDialog().dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        getView().requestLayout();
    }
}
