package com.zhangqiang.sample.ui.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;

public class TaskOperationDialog extends BaseDialogFragment {

    public interface OperationListener{
        void onDelete();

        void onCopyLink();

        void onRestart();
    }

    private OperationListener operationListener;

    public static TaskOperationDialog newInstance() {
        Bundle arg = new Bundle();
        TaskOperationDialog dialog = new TaskOperationDialog();
        dialog.setArguments(arg);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
    }

    @Override
    protected boolean useBottomSheet() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_taks_operation;
    }

    @Override
    protected void initView(View view) {
        final Context context = getContext();
        view.findViewById(R.id.bt_delete_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (operationListener != null) {
                    operationListener.onDelete();
                }
//                TaskDeleteConfirmDialog dialog = TaskDeleteConfirmDialog.newInstance(taskId);
//                dialog.show(getFragmentManager(), "delete_confirm");
                getDialog().dismiss();
            }
        });
        view.findViewById(R.id.bt_copy_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(operationListener != null){
                    operationListener.onCopyLink();
                }
//                TaskInfo taskInfo = DownloadManager.getInstance(context).getTaskInfo(taskId);
//                if (taskInfo instanceof HttpTaskInfo) {
//                    HttpTaskInfo httpTaskInfo = (HttpTaskInfo) taskInfo;
//                    copy(context, httpTaskInfo.getUrl());
//                    Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show();
//                    getDialog().dismiss();
//                }
                getDialog().dismiss();
            }
        });
        view.findViewById(R.id.bt_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(operationListener != null){
                    operationListener.onRestart();
                }
//                TaskInfo taskInfo = DownloadManager.getInstance(context).getTaskInfo(taskId);
//                if (taskInfo instanceof HttpTaskInfo) {
//                    HttpTaskInfo httpTaskInfo = (HttpTaskInfo) taskInfo;
//                    DownloadManager.getInstance(context).deleteTask(taskId, true);
//                    DownloadRequest request = new HttpDownloadRequest.Builder()
//                            .setUrl(httpTaskInfo.getUrl())
//                            .setSaveDir(httpTaskInfo.getSaveDir())
//                            .setThreadSize(httpTaskInfo.getThreadCount())
//                            .setFileName(httpTaskInfo.getFileName())
//                            .build();
//                    DownloadManager.getInstance(context).enqueue(request);
//                    getDialog().dismiss();
//                }else if(taskInfo instanceof FTPTaskInfo){
//                    FTPTaskInfo ftpTaskInfo = (FTPTaskInfo) taskInfo;
//                    DownloadManager.getInstance(context).deleteTask(taskId, true);
//                    DownloadRequest request = new FTPDownloadRequest.Builder()
//                            .setHost(ftpTaskInfo.getHost())
//                            .setPort(ftpTaskInfo.getPort())
//                            .setSaveDir(ftpTaskInfo.getSaveDir())
//                            .setFileName(ftpTaskInfo.getFileName())
//                            .setFtpDir(ftpTaskInfo.getFtpDir())
//                            .setFtpFileName(ftpTaskInfo.getFtpFileName())
//                            .setUserName(ftpTaskInfo.getUserName())
//                            .setPassword(ftpTaskInfo.getPassword())
//                            .build();
//                    DownloadManager.getInstance(context).enqueue(request);
//                    getDialog().dismiss();
//                }
                getDialog().dismiss();
            }
        });
        view.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
    }

    private void copy(Context context, String url) {
        if (context == null) {
            return;
        }
        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null) {
            return;
        }
        ClipData clipData = ClipData.newPlainText(url, url);
        clipboardManager.setPrimaryClip(clipData);
    }

    public TaskOperationDialog setOperationListener(OperationListener operationListener) {
        this.operationListener = operationListener;
        return this;
    }
}
