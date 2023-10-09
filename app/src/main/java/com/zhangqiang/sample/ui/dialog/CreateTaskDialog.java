package com.zhangqiang.sample.ui.dialog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.zhangqiang.downloadmanager.plugin.http.request.HttpDownloadRequest;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.ui.DownloadManagerFragment;

import java.io.File;

public class CreateTaskDialog extends BaseDialogFragment {

    private EditText etUrl;
    private EditText etThreadSize;
    private EditText etSaveName;
    private static final String DEBUG_URL = "https://imtt.dd.qq.com/16891/apk/847A5ED16C396C7767FF4987915AAB06.apk?fsname=com.qq.reader_7.5.8.666_174.apk&csr=1bbd";
    private String defaultUrl;

   public static CreateTaskDialog createAndShow(FragmentManager fragmentManager, String url){
       CreateTaskDialog dialog = new CreateTaskDialog();
       Bundle arg = new Bundle();
       arg.putString("url", url);
       dialog.setArguments(arg);
       dialog.show(fragmentManager, "create_task");
       return dialog;
   }

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
                url = defaultUrl;
            }
            if (TextUtils.isEmpty(url)) {
                url = DEBUG_URL;
            }
            File dirFile = new File(Environment.getExternalStorageDirectory(), SettingsManager.getInstance().getSaveDir());
            HttpDownloadRequest request = new HttpDownloadRequest(dirFile.getAbsolutePath(),
                    etSaveName.getText().toString().trim(),
                    url,
                    Integer.parseInt(etThreadSize.getText().toString()));
            DownloadManagerFragment.downloadManager.enqueue(request);
            getDialog().dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }
}
