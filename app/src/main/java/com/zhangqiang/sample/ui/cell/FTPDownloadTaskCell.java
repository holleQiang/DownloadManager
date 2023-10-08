package com.zhangqiang.sample.ui.cell;

import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.FragmentManager;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.action.Action;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.task.ftp.FTPDownloadTask;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.downloadmanager2.task.Status;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.ui.dialog.TaskOperationDialog;
import com.zhangqiang.sample.utils.IntentUtils;

import java.io.File;

public class FTPDownloadTaskCell extends MultiCell<FTPDownloadTask> {

    private static final String TAG = FTPDownloadTaskCell.class.getSimpleName();
    private final FragmentManager fragmentManager;

    public FTPDownloadTaskCell(FTPDownloadTask data, FragmentManager fragmentManager) {
        super(R.layout.item_download_ftp, data,null);
        this.fragmentManager = fragmentManager;
    }

    @Override
    protected void onBindViewHolder(ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        View view = viewHolder.getView();
        final Context context = view.getContext();
        final FTPDownloadTask downloadTask = getData();
        updateState(viewHolder);
        updateInfo(viewHolder);
        updateProgress(viewHolder);
        viewHolder.setOnClickListener(R.id.bt_state, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Status status = downloadTask.getStatus();
                LogUtils.i(TAG,"------------State:"+status);
                if (status == Status.FAIL || status == Status.CANCELED) {
                    downloadTask.start();
                } else if (status == Status.DOWNLOADING) {
                    downloadTask.cancel();
                } else if (status == Status.SUCCESS) {
                    File file = new File(downloadTask.getSaveDir(), downloadTask.getSaveFileName());
                    IntentUtils.openFileSmart(v.getContext(), file, null);
                }
            }
        });
        updateSpeed(viewHolder);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                TaskOperationDialog.newInstance().setOperationListener(new TaskOperationDialog.OperationListener() {
                    @Override
                    public void onDelete() {

                    }

                    @Override
                    public void onCopyLink() {

                    }

                    @Override
                    public void onRestart() {

                    }
                }).show(fragmentManager, "task_operate_dialog");
                return true;
            }
        });

        Object tag = view.getTag();
        if (tag != null) {
            view.removeOnAttachStateChangeListener((View.OnAttachStateChangeListener) tag);
        }
        View.OnAttachStateChangeListener onAttachStateChangeListener = new View.OnAttachStateChangeListener() {


            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
            }
        };
        view.addOnAttachStateChangeListener(onAttachStateChangeListener);
        view.setTag(onAttachStateChangeListener);
    }

    private void updateSpeed(ViewHolder viewHolder) {
//        FTPDownloadTask data = getData();
//        viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(data.getSpeed()) + "/s");
//
//        String resetTimeStr;
//        if (data.getContentLength() == 0) {
//            resetTimeStr = "剩余时间：" + "未知";
//        } else if (data.getContentLength() <= data.getCurrentLength()) {
//
//            if (data.getState() == FTPTaskInfo.STATE_COMPLETE) {
//                resetTimeStr = "已完成";
//            } else {
//                resetTimeStr = "剩余时间：0秒";
//            }
//        } else if (data.getSpeed() == 0) {
//            resetTimeStr = "剩余时间：" + "未知";
//        } else {
//            long resetLength = data.getContentLength() - data.getCurrentLength();
//            long resetTime = resetLength / data.getSpeed();
//            if (resetTime <= 0) {
//                resetTimeStr = "剩余时间：0秒";
//            } else if (resetTime < 60) {
//                resetTimeStr = "剩余时间：" + resetTime + "秒";
//            } else if (resetTime < 60 * 60) {
//                resetTimeStr = "剩余时间：" + resetTime / 60 + "分钟";
//            } else if (resetTime < 60 * 60 * 24) {
//                resetTimeStr = "剩余时间：" + resetTime / 60 / 60 + "小时";
//            } else {
//                resetTimeStr = "剩余时间：" + resetTime / 60 / 60 / 24 + "天";
//            }
//        }
//        viewHolder.setText(R.id.tv_rest_time, resetTimeStr);
    }

    public void updateSpeed() {
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateSpeed(viewHolder);
            }
        });
    }

    private void updateState(ViewHolder viewHolder) {
        FTPDownloadTask data = getData();

        Status status = data.getStatus();
        if (status == Status.IDLE) {
            viewHolder.setText(R.id.bt_state, R.string.waiting);
            changeVisibleByError(viewHolder, false);
            viewHolder.setVisibility(R.id.tv_speed, View.INVISIBLE);
        } else if (status == Status.DOWNLOADING) {
            viewHolder.setText(R.id.bt_state, R.string.pause);
            changeVisibleByError(viewHolder, false);
            viewHolder.setVisibility(R.id.tv_speed, View.VISIBLE);
        } else if (status == Status.SUCCESS) {
            viewHolder.setText(R.id.bt_state, R.string.open);
            changeVisibleByError(viewHolder, false);
            viewHolder.setVisibility(R.id.tv_speed, View.INVISIBLE);
        } else if (status == Status.FAIL) {
            viewHolder.setText(R.id.bt_state, R.string.fail);
            changeVisibleByError(viewHolder, true);
            viewHolder.setText(R.id.tv_error, data.getErrorMessage());
            viewHolder.setVisibility(R.id.tv_speed, View.INVISIBLE);
        } else if (status == Status.CANCELED) {
            viewHolder.setText(R.id.bt_state, R.string.continue_download);
            changeVisibleByError(viewHolder, false);
            viewHolder.setVisibility(R.id.tv_speed, View.INVISIBLE);
        }
    }

    private void updateInfo(ViewHolder viewHolder) {
        FTPDownloadTask data = getData();
        viewHolder.setText(R.id.tv_file_name, data.getSaveFileName());
    }

    private void changeVisibleByError(ViewHolder viewHolder, boolean isError) {

        viewHolder.setVisibility(R.id.tv_error, isError ? View.VISIBLE : View.INVISIBLE);
        viewHolder.setVisibility(R.id.pb_download_progress, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_speed, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_progress, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_rest_time, isError ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateProgress(ViewHolder viewHolder) {
        FTPDownloadTask data = getData();
//        long currentLength = data.getCurrentLength();
//        long totalLength = data.getContentLength();
//        int progress = (int) ((float) currentLength / totalLength * 100);
//        viewHolder.setText(R.id.tv_progress, StringUtils.formatFileLength(currentLength) + "/" + StringUtils.formatFileLength(totalLength));
//        ProgressBar progressBar = viewHolder.getView(R.id.pb_download_progress);
//        progressBar.setProgress(progress);
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
        LogUtils.i(TAG,"======updateState========="+getData().getStatus());
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateState(viewHolder);
            }
        });
    }
}
