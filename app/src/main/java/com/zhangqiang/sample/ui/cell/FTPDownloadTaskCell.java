package com.zhangqiang.sample.ui.cell;

import android.view.View;
import android.widget.ProgressBar;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.action.Action;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.plugin.ftp.callback.ResourceInfo;
import com.zhangqiang.downloadmanager.plugin.ftp.task.FTPDownloadTask;
import com.zhangqiang.downloadmanager.plugin.ftp.task.OnResourceInfoReadyListener;
import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.speed.OnSpeedChangeListener;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.utils.IntentUtils;

import java.io.File;

public class FTPDownloadTaskCell extends MultiCell<FTPDownloadTask> {

    private static final String TAG = FTPDownloadTaskCell.class.getSimpleName();
    private final View.OnLongClickListener onItemLongClickListener;

    public FTPDownloadTaskCell(FTPDownloadTask data,  View.OnLongClickListener onItemLongClickListener) {
        super(R.layout.item_download_ftp, data, null);
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    protected void onBindViewHolder(ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        View view = viewHolder.getView();
        final FTPDownloadTask downloadTask = getData();
        updateState(viewHolder);
        updateInfo(viewHolder);
        updateProgress(viewHolder);
        updateSpeed(viewHolder);
        viewHolder.setOnClickListener(R.id.bt_state, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Status status = downloadTask.getStatus();
                LogUtils.i(TAG, "------------State:" + status);
                if (status == Status.FAIL || status == Status.CANCELED) {
                    downloadTask.start();
                } else if (status == Status.DOWNLOADING) {
                    downloadTask.cancel();
                } else if (status == Status.SUCCESS) {
                    File file = new File(downloadTask.getSaveDir(), downloadTask.getSaveFileName());
                    IntentUtils.openFileSmart(v.getContext(), file, downloadTask.getResourceInfo().getContentType());
                }
            }
        });
        view.setOnLongClickListener(onItemLongClickListener);
        Object tag = view.getTag();
        if (tag != null) {
            view.removeOnAttachStateChangeListener((View.OnAttachStateChangeListener) tag);
        }
        View.OnAttachStateChangeListener onAttachStateChangeListener = new View.OnAttachStateChangeListener() {

            final OnStatusChangeListener onStatusChangeListener = new OnStatusChangeListener() {
                @Override
                public void onStatusChange(Status newStatus, Status oldStatus) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            updateState();
                        }
                    });
                }
            };
            final OnProgressChangeListener onProgressChangeListener = new OnProgressChangeListener() {
                @Override
                public void onProgressChange() {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            updateProgress();
                        }
                    });
                }
            };
            final OnResourceInfoReadyListener onResourceInfoReadyListener = new OnResourceInfoReadyListener() {
                @Override
                public void onResourceInfoReady(ResourceInfo resourceInfo) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            updateInfo();
                        }
                    });
                }
            };

            final OnSpeedChangeListener onSpeedChangeListener = new OnSpeedChangeListener() {
                @Override
                public void onSpeedChange() {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            updateSpeed();
                        }
                    });
                }
            };

            @Override
            public void onViewAttachedToWindow(View v) {
                downloadTask.addStatusChangeListener(onStatusChangeListener);
                downloadTask.addOnProgressChangeListener(onProgressChangeListener);
                downloadTask.addOnResourceInfoReadyListener(onResourceInfoReadyListener);
                downloadTask.getSpeedHelper().addOnSpeedChangeListener(onSpeedChangeListener);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                downloadTask.removeStatusChangeListener(onStatusChangeListener);
                downloadTask.removeOnProgressChangeListener(onProgressChangeListener);
                downloadTask.removeOnResourceInfoReadyListener(onResourceInfoReadyListener);
                downloadTask.getSpeedHelper().removeOnSpeedChangeListener(onSpeedChangeListener);
            }
        };
        view.addOnAttachStateChangeListener(onAttachStateChangeListener);
        view.setTag(onAttachStateChangeListener);
    }

    private void updateSpeed(ViewHolder viewHolder) {
        FTPDownloadTask downloadTask = getData();
        long speed = downloadTask.getSpeedHelper().getSpeed();
        viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(speed) + "/s");

        String resetTimeStr;
        long contentLength = getContentLength();
        if (contentLength == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else if (contentLength <= downloadTask.getCurrentLength()) {

            if (downloadTask.getStatus() == Status.SUCCESS) {
                resetTimeStr = "已完成";
            } else {
                resetTimeStr = "剩余时间：0秒";
            }
        } else if (speed == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else {
            long resetLength = contentLength - downloadTask.getCurrentLength();
            long resetTime = resetLength / speed;
            if (resetTime <= 0) {
                resetTimeStr = "剩余时间：0秒";
            } else if (resetTime < 60) {
                resetTimeStr = "剩余时间：" + resetTime + "秒";
            } else if (resetTime < 60 * 60) {
                resetTimeStr = "剩余时间：" + resetTime / 60 + "分钟";
            } else if (resetTime < 60 * 60 * 24) {
                resetTimeStr = "剩余时间：" + resetTime / 60 / 60 + "小时";
            } else {
                resetTimeStr = "剩余时间：" + resetTime / 60 / 60 / 24 + "天";
            }
        }
        viewHolder.setText(R.id.tv_rest_time, resetTimeStr);
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
        long currentLength = data.getCurrentLength();
        long totalLength = getContentLength();
        int progress = (int) ((float) currentLength / totalLength * 100);
        viewHolder.setText(R.id.tv_progress, StringUtils.formatFileLength(currentLength) + "/" + StringUtils.formatFileLength(totalLength));
        ProgressBar progressBar = viewHolder.getView(R.id.pb_download_progress);
        progressBar.setProgress(progress);
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
        LogUtils.i(TAG, "======updateState=========" + getData().getStatus());
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateState(viewHolder);
            }
        });
    }

    private long getContentLength() {
        ResourceInfo resourceInfo = getData().getResourceInfo();
        return resourceInfo == null ? 0 : resourceInfo.getContentLength();
    }
}
