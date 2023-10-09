package com.zhangqiang.sample.ui.dialog;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.plugin.ftp.request.FtpDownloadRequest;
import com.zhangqiang.downloadmanager.plugin.http.request.HttpDownloadRequest;
import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;
import com.zhangqiang.sample.databinding.DialogTaskCreateByLinkBinding;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.utils.DownloadUtils;

import java.io.File;

public class TaskCreateByLinkDialog extends BaseDialogFragment {

    private DialogTaskCreateByLinkBinding binding;
    private String url;

    public static TaskCreateByLinkDialog newInstance(String url){
        TaskCreateByLinkDialog dialog = new TaskCreateByLinkDialog();
        Bundle bundle = new Bundle();
        bundle.putString("url",url);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            this.url = arguments.getString("url");
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_task_create_by_link;
    }

    @Override
    protected void initView(View view) {
        binding = DialogTaskCreateByLinkBinding.bind(view);
        binding.etLink.setText(url);
        binding.btDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000 && grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            String link = binding.etLink.getText().toString();
            if (TextUtils.isEmpty(link)) {
                Toast.makeText(getActivity(), "请输入链接", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!DownloadUtils.downloadLink(link)) {
                Toast.makeText(getActivity(), "不支持下载此链接", Toast.LENGTH_SHORT).show();
            }else {
                getDialog().dismiss();
            }
        }
    }
}
