package com.zhangqiang.sample.ui.cell;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Toast;

import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.celladapter.cell.CellParent;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.UIDownloadListener;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.ui.dialog.TaskDeleteConfirmDialog;
import com.zhangqiang.sample.ui.dialog.TaskOperationDialog;
import com.zhangqiang.sample.utils.IntentUtils;

import java.io.File;

public class DownloadTaskCell extends MultiCell<DownloadTask> {

    private static final String TAG = "DownloadTaskCell";
    private FragmentManager fragmentManager;

    public DownloadTaskCell(DownloadTask data, FragmentManager fragmentManager) {
        super(R.layout.item_download, data, null);
        this.fragmentManager = fragmentManager;
    }


    @Override
    protected void onBindViewHolder(final ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);

        final DownloadTask downloadTask = getData();

        updateState(downloadTask, viewHolder);
        updateProgress(downloadTask, viewHolder);
        viewHolder.setOnClickListener(R.id.bt_state, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int status = downloadTask.getState();
                if (status == DownloadTask.STATE_IDLE || status == DownloadTask.STATE_FAIL || status == DownloadTask.STATE_PAUSE) {
                    downloadTask.start();
                } else if (status == DownloadTask.STATE_DOWNLOADING) {
                    downloadTask.pause();
                } else if (status == DownloadTask.STATE_COMPLETE) {
                    File file = new File(downloadTask.getSaveDir(), downloadTask.getFileName());
                    IntentUtils.openFile(v.getContext(), file, downloadTask.getContentType());
                }
            }
        });
        Cell.setOnAttachStateChangeListener(R.id.tag_key_attach_listener, viewHolder.getView(), new View.OnAttachStateChangeListener() {

            UIDownloadListener downloadListener = new UIDownloadListener() {
                @Override
                protected void onDownloadStart() {
                    super.onDownloadStart();
                    updateState(downloadTask, viewHolder);
                }

                @Override
                protected void onDownloadProgress(long current, long total) {
                    super.onDownloadProgress(current, total);
                    updateProgress(downloadTask, viewHolder);
                }

                @Override
                protected void onDownloadComplete() {
                    super.onDownloadComplete();
                    updateState(downloadTask, viewHolder);
                }

                @Override
                protected void onDownloadFail(Throwable e) {
                    super.onDownloadFail(e);
                    updateState(downloadTask, viewHolder);
                }

                @Override
                protected void onDownloadPause() {
                    super.onDownloadPause();
                    updateState(downloadTask, viewHolder);
                }

                @Override
                protected void onDownloadSpeed(long length, long timeMillions) {
                    super.onDownloadSpeed(length, timeMillions);
                    LogUtils.i(TAG, "=======onDownloadSpeed========");
                    long speed = timeMillions > 0 ? length / timeMillions * 1000 : 0;
                    viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(speed) + "/s");
                }
            };

            @Override
            public void onViewAttachedToWindow(View v) {
                downloadTask.addDownloadListener(downloadListener);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                downloadTask.removeDownloadListener(downloadListener);
                downloadListener.stopSpeedCalculator();
            }
        });

        viewHolder.getView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                final Context context = v.getContext();
                TaskOperationDialog dialog = TaskOperationDialog.newInstance();
                dialog.setListener(new TaskOperationDialog.Listener() {
                    @Override
                    public void onCopyLinkClick() {

                        copy(context, downloadTask.getUrl());
                        Toast.makeText(context,R.string.copy_success,Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onDeleteTaskClick() {

                        TaskDeleteConfirmDialog dialog = TaskDeleteConfirmDialog.newInstance();
                        dialog.setListener(new TaskDeleteConfirmDialog.Listener() {
                            @Override
                            public void onConfirm(boolean deleteFile) {
                                DownloadManager.getInstance().deleteTask(downloadTask, deleteFile);
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

    private void updateState(DownloadTask downloadTask, ViewHolder viewHolder) {
        viewHolder.setText(R.id.tv_file_name, downloadTask.getFileName());
        int status = downloadTask.getState();
        if (status == DownloadTask.STATE_IDLE) {
            viewHolder.setText(R.id.bt_state, R.string.download);
            changeVisible(viewHolder, false);
        } else if (status == DownloadTask.STATE_DOWNLOADING) {
            viewHolder.setText(R.id.bt_state, R.string.pause);
            changeVisible(viewHolder, false);
        } else if (status == DownloadTask.STATE_COMPLETE) {
            viewHolder.setText(R.id.bt_state, R.string.open);
            changeVisible(viewHolder, false);
        } else if (status == DownloadTask.STATE_FAIL) {
            viewHolder.setText(R.id.bt_state, R.string.fail);
            changeVisible(viewHolder, true);
            viewHolder.setText(R.id.tv_error, downloadTask.getErrorMsg());
        } else if (status == DownloadTask.STATE_PAUSE) {
            viewHolder.setText(R.id.bt_state, R.string.continue_download);
            changeVisible(viewHolder, false);
        }
    }

    private void changeVisible(ViewHolder viewHolder, boolean isError) {

        viewHolder.setVisibility(R.id.tv_error, isError ? View.VISIBLE : View.INVISIBLE);
        viewHolder.setVisibility(R.id.pb_download_progress, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_speed, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_progress, isError ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateProgress(DownloadTask downloadTask, ViewHolder viewHolder) {
        long currentLength = downloadTask.getCurrentLength();
        long totalLength = downloadTask.getTotalLength();
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
}
