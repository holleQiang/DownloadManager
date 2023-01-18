package com.zhangqiang.sample.ui.dialog;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.CheckBox;

import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;

public class TaskDeleteConfirmDialog extends BaseDialogFragment {

    private CheckBox cbDeleteFile;
    private String taskId;

    public static TaskDeleteConfirmDialog newInstance(String taskId) {
        Bundle arg = new Bundle();
        arg.putString("taskId",taskId);
        TaskDeleteConfirmDialog dialog = new TaskDeleteConfirmDialog();
        dialog.setArguments(arg);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            taskId = arguments.getString("taskId");
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_delete_task_confirm;
    }

    @Override
    protected void initView(View view) {
        cbDeleteFile = view.findViewById(R.id.cb_delete_file);
        view.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                TaskInfo taskInfo = DownloadManager.getInstance(context).getTaskInfo(taskId);
                if (taskInfo == null) {
                    return;
                }
                DownloadManager.getInstance(context).deleteTask(taskInfo.getId(), cbDeleteFile.isChecked());
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

    @Override
    protected boolean useBottomSheet() {
        return true;
    }

}
