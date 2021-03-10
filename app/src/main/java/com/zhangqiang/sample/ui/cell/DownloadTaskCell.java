package com.zhangqiang.sample.ui.cell;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.Toast;

import com.zhangqiang.celladapter.cell.CellParent;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.action.Action;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.ui.dialog.TaskDeleteConfirmDialog;
import com.zhangqiang.sample.ui.dialog.TaskOperationDialog;
import com.zhangqiang.sample.utils.IntentUtils;

import java.io.File;

public class DownloadTaskCell extends MultiCell<TaskEntity> {

    private static final String TAG = "DownloadTaskCell";
    private FragmentManager fragmentManager;

    public DownloadTaskCell(TaskEntity data, FragmentManager fragmentManager) {
        super(R.layout.item_download, data, null);
        this.fragmentManager = fragmentManager;
    }


    @Override
    protected void onBindViewHolder(final ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        final Context context = viewHolder.getView().getContext();
        final TaskEntity entity = getData();

        updateState(viewHolder);
        updateInfo(viewHolder);
        updateProgress(viewHolder);
        viewHolder.setOnClickListener(R.id.bt_state, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = entity.getState();
                if (status == DownloadManager.STATE_FAIL || status == DownloadManager.STATE_PAUSE) {
                    DownloadManager.getInstance(context).start(entity.getId());
                } else if (status == DownloadManager.STATE_DOWNLOADING) {
                    DownloadManager.getInstance(context).pause(entity.getId());
                } else if (status == DownloadManager.STATE_COMPLETE) {
                    File file = new File(entity.getSaveDir(), entity.getFileName());
                    IntentUtils.openFile(v.getContext(), file, entity.getContentType());
                }
            }
        });
        viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(100) + "/s");

        viewHolder.getView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                final Context context = v.getContext();
                TaskOperationDialog dialog = TaskOperationDialog.newInstance();
                dialog.setListener(new TaskOperationDialog.Listener() {
                    @Override
                    public void onCopyLinkClick() {

                        copy(context, entity.getUrl());
                        Toast.makeText(context,R.string.copy_success,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDeleteTaskClick() {

                        TaskDeleteConfirmDialog dialog = TaskDeleteConfirmDialog.newInstance();
                        dialog.setListener(new TaskDeleteConfirmDialog.Listener() {
                            @Override
                            public void onConfirm(boolean deleteFile) {
                                DownloadManager.getInstance(context).deleteTask(entity.getId(), deleteFile);
                                CellParent parent = getParent();
                                if (parent != null) {
                                    parent.removeData(DownloadTaskCell.this);
                                }
                            }
                        });
                        dialog.show(fragmentManager, "delete_confirm");
                    }
                });
                dialog.show(fragmentManager, "task_operate_dialog");

                return true;
            }
        });
    }

    private void updateState( ViewHolder viewHolder) {
        TaskEntity data = getData();

        int status = data.getState();
        if (status == DownloadManager.STATE_IDLE) {
            viewHolder.setText(R.id.bt_state, R.string.waiting);
            changeVisible(viewHolder, false);
        } else if (status == DownloadManager.STATE_DOWNLOADING) {
            viewHolder.setText(R.id.bt_state, R.string.pause);
            changeVisible(viewHolder, false);
        } else if (status == DownloadManager.STATE_COMPLETE) {
            viewHolder.setText(R.id.bt_state, R.string.open);
            changeVisible(viewHolder, false);
        } else if (status == DownloadManager.STATE_FAIL) {
            viewHolder.setText(R.id.bt_state, R.string.fail);
            changeVisible(viewHolder, true);
            viewHolder.setText(R.id.tv_error, data.getErrorMsg());
        } else if (status == DownloadManager.STATE_PAUSE) {
            viewHolder.setText(R.id.bt_state, R.string.continue_download);
            changeVisible(viewHolder, false);
        }
    }

    private void updateInfo(ViewHolder viewHolder){
        TaskEntity data = getData();
        viewHolder.setText(R.id.tv_file_name,data.getFileName());
    }

    private void changeVisible(ViewHolder viewHolder, boolean isError) {

        viewHolder.setVisibility(R.id.tv_error, isError ? View.VISIBLE : View.INVISIBLE);
        viewHolder.setVisibility(R.id.pb_download_progress, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_speed, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_progress, isError ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateProgress(ViewHolder viewHolder) {
        TaskEntity data = getData();
        long currentLength = data.getCurrentLength();
        long totalLength = data.getContentLength();
        int progress = (int) ((float) currentLength / totalLength * 100);
        viewHolder.setProgress(R.id.pb_download_progress, progress);
        viewHolder.setText(R.id.tv_progress, StringUtils.formatFileLength(currentLength) + "/" + StringUtils.formatFileLength(totalLength));
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

    public void updateProgress() {
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateProgress(viewHolder);
            }
        });
    }

    public void updateInfo() {
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateInfo(viewHolder);
            }
        });
    }

    public void updateState() {
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateState(viewHolder);
            }
        });
    }
}
