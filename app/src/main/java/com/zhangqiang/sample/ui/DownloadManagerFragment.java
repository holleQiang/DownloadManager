package com.zhangqiang.sample.ui;


import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.downloadmanager.manager.DownloadManager;
import com.zhangqiang.downloadmanager.manager.OnTaskCountChangeListener;
import com.zhangqiang.downloadmanager.manager.RemoveTaskOptions;
import com.zhangqiang.downloadmanager.plugin.ftp.FtpDownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.ftp.task.FTPDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.HttpDownloadPlugin;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpDownloadTask;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseFragment;
import com.zhangqiang.sample.ui.cell.FTPDownloadTaskCell;
import com.zhangqiang.sample.ui.cell.HttpDownloadTaskCell;
import com.zhangqiang.sample.ui.dialog.TaskOperationDialog;
import com.zhangqiang.sample.ui.widget.LinearRVDivider;
import com.zhangqiang.sample.utils.ClipboardUtils;
import com.zhangqiang.sample.utils.DownloadUtils;
import com.zhangqiang.sample.utils.IntentUtils;
import com.zhangqiang.sample.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class DownloadManagerFragment extends BaseFragment {

    public static DownloadManager downloadManager;
    private final CellRVAdapter cellRVAdapter = new CellRVAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_download_manager2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvDownloadTask = view.findViewById(R.id.rv_download_task);
        rvDownloadTask.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rvDownloadTask.setAdapter(cellRVAdapter);
        ColorDrawable dividerDrawable = new ColorDrawable(Color.parseColor("#eeeeee"));
        dividerDrawable.setBounds(0, 0, 0, ScreenUtils.dp2Px(view.getContext(), 10));
        rvDownloadTask.addItemDecoration(new LinearRVDivider(dividerDrawable));

        DownloadManager.getInstance().addTaskCountChangeListener(onTaskCountChangeListener);
        updateTaskList();
    }

    final OnTaskCountChangeListener onTaskCountChangeListener = new OnTaskCountChangeListener() {
        @Override
        public void onTaskCountChange(int newCount, int oldCount) {
            FragmentActivity activity = getActivity();
            if (activity == null) {
                return;
            }
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTaskList();
                }
            });
        }
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DownloadManager.getInstance().removeTaskCountChangeListener(onTaskCountChangeListener);
    }

    private void updateTaskList() {
        List<Cell> cells = new ArrayList<>();
        int taskCount = DownloadManager.getInstance().getTaskCount();
        for (int i = 0; i < taskCount; i++) {
            DownloadTask task = DownloadManager.getInstance().getTask(i);
            if (task instanceof HttpDownloadTask) {
                cells.add(makeHttpDownloadTaskCell((HttpDownloadTask) task));
            } else if (task instanceof FTPDownloadTask) {
                cells.add(makeFtpDownloadTaskCell(((FTPDownloadTask) task)));
            }
        }
        cellRVAdapter.setDataList(cells);
    }

    private Cell makeFtpDownloadTaskCell(FTPDownloadTask downloadTask) {
        return new FTPDownloadTaskCell(downloadTask, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TaskOperationDialog taskOperationDialog = TaskOperationDialog.newInstance();
                taskOperationDialog.setOperationListener(new TaskOperationDialog.OperationListener() {

                    @Override
                    public void onDelete(boolean deleteFile) {
                        DownloadManager.getInstance().deleteTask(downloadTask,new RemoveTaskOptions().setDeleteFile(deleteFile));
                    }

                    @Override
                    public void onCopyLink() {
                        ClipboardUtils.copy(v.getContext(),downloadTask.buildLink());
                    }

                    @Override
                    public void onRestart() {
                        DownloadManager.getInstance().deleteTask(downloadTask,new RemoveTaskOptions().setDeleteFile(true));
                        DownloadUtils.downloadFtpUrl(downloadTask.buildLink());
                    }

                    @Override
                    public void onOpenDirClick() {
                        IntentUtils.openDir(v.getContext(),downloadTask.getSaveDir());
                    }
                }).show(getChildFragmentManager(), "task_operate_dialog");
                return true;
            }
        });
    }

    private Cell makeHttpDownloadTaskCell(HttpDownloadTask downloadTask) {
        return new HttpDownloadTaskCell(downloadTask, new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                TaskOperationDialog taskOperationDialog = TaskOperationDialog.newInstance();
                taskOperationDialog.setOperationListener(new TaskOperationDialog.OperationListener() {

                    @Override
                    public void onDelete(boolean deleteFile) {
                        DownloadManager.getInstance().deleteTask(downloadTask,new RemoveTaskOptions().setDeleteFile(deleteFile));
                    }

                    @Override
                    public void onCopyLink() {
                        ClipboardUtils.copy(v.getContext(),downloadTask.getUrl());
                    }

                    @Override
                    public void onRestart() {
                        DownloadManager.getInstance().deleteTask(downloadTask,new RemoveTaskOptions().setDeleteFile(true));
                        DownloadUtils.downloadHttpUrl(downloadTask.getUrl());
                    }

                    @Override
                    public void onOpenDirClick() {
                        IntentUtils.openDir(v.getContext(),downloadTask.getSaveDir());
                    }
                }).show(getChildFragmentManager(), "task_operate_dialog");
                return true;
            }
        });
    }
}