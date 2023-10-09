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

import com.zhangqiang.downloadmanager.plugin.ftp.request.FtpDownloadRequest;
import com.zhangqiang.downloadmanager.plugin.http.request.HttpDownloadRequest;
import com.zhangqiang.downloadmanager.request.DownloadRequest;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;
import com.zhangqiang.sample.databinding.DialogTaskCreateByLinkBinding;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.ui.DownloadManagerFragment;

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
            Uri uri = Uri.parse(link);
            String scheme = uri.getScheme();
            DownloadRequest downloadRequest = null;
            if ("http".equals(scheme) || "https".equals(scheme)) {
                File dirFile = new File(Environment.getExternalStorageDirectory(), SettingsManager.getInstance().getSaveDir());
                downloadRequest = new HttpDownloadRequest(dirFile.getAbsolutePath(),
                        null,
                        link,
                        2);
            } else if ("ftp".equals(scheme)) {
                String userInfo = uri.getUserInfo();
                String username = null;
                String password = null;
                if (!TextUtils.isEmpty(userInfo)) {
                    String[] split = userInfo.split(":");
                    if (split.length >= 1) {
                        username = split[0];
                    }
                    if (split.length >= 2) {
                        password = split[1];
                    }
                }

                String path = uri.getPath();
                File dirFile = new File(Environment.getExternalStorageDirectory(), SettingsManager.getInstance().getSaveDir());
                String ftpDir = path.substring(0, path.lastIndexOf("/"));
                if (TextUtils.isEmpty(ftpDir)) {
                    ftpDir = "/";
                }
                downloadRequest = new FtpDownloadRequest(dirFile.getAbsolutePath(),
                        null,
                        uri.getHost(),
                        uri.getPort(),
                        username,
                        password,
                        ftpDir,
                        uri.getLastPathSegment()
                );
            }
            if (downloadRequest == null) {
                Toast.makeText(getActivity(), "不支持下载此链接", Toast.LENGTH_SHORT).show();
            } else {
                DownloadManagerFragment.downloadManager.enqueue(downloadRequest);
                dismiss();
            }
        }
    }
}
