package com.zhangqiang.sample.ui.dialog;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.DownloadRequest;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;

public class TaskOperationDialog extends BaseDialogFragment {


    private long taskId;

    public static TaskOperationDialog newInstance(long taskId) {
        Bundle arg = new Bundle();
        arg.putLong("taskId", taskId);
        TaskOperationDialog dialog = new TaskOperationDialog();
        dialog.setArguments(arg);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            taskId = arguments.getLong("taskId");
        }
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

                TaskDeleteConfirmDialog dialog = TaskDeleteConfirmDialog.newInstance(taskId);
                dialog.show(getFragmentManager(), "delete_confirm");
                getDialog().dismiss();
            }
        });
        view.findViewById(R.id.bt_copy_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskInfo taskInfo = DownloadManager.getInstance(context).getTaskInfo(taskId);
                if (taskInfo == null) {
                    return;
                }
                copy(context, taskInfo.getUrl());
                Toast.makeText(context, R.string.copy_success, Toast.LENGTH_SHORT).show();
                getDialog().dismiss();
            }
        });
        view.findViewById(R.id.bt_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskInfo taskInfo = DownloadManager.getInstance(context).getTaskInfo(taskId);
                if (taskInfo != null) {
                    DownloadManager.getInstance(context).deleteTask(taskId, true);
                    DownloadRequest request = new DownloadRequest.Builder(taskInfo.getUrl(), taskInfo.getSaveDir())
                            .setThreadCount(taskInfo.getThreadSize())
                            .setFileName(taskInfo.getFileName())
                            .build();
                    DownloadManager.getInstance(context).enqueue(request);
                    getDialog().dismiss();
                }
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
}
