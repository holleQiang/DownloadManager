package com.zhangqiang.sample.ui.dialog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;
import com.zhangqiang.sample.manager.SettingsManager;

import java.io.File;

public class CreateTaskDialog extends BaseDialogFragment {

    private EditText etUrl;
    private EditText etThreadSize;
    private EditText etSaveName;
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
        etSaveName = view.findViewById(R.id.et_save_name);
        view.findViewById(R.id.bt_enqueue).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1000);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1000 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            String url = etUrl.getText().toString().trim();
            if (TextUtils.isEmpty(url)) {
                return;
            }
            File dirFile = new File(Environment.getExternalStorageDirectory(), SettingsManager.getInstance().getSaveDir());
            DownloadRequest request = new DownloadRequest.Builder(url, dirFile.getAbsolutePath())
                    .setThreadCount(Integer.parseInt(etThreadSize.getText().toString()))
                    .setFileName(etSaveName.getText().toString().trim())
                    .build();
            DownloadManager.getInstance(getContext()).enqueue(request);
            getDialog().dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
