package com.zhangqiang.sample.ui.cell;

import android.view.View;
import android.widget.ProgressBar;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.action.Action;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.M3u8DownloadTask;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.OnResourceInfoReadyListener;
import com.zhangqiang.downloadmanager.plugin.m3u8.task.M3u8ResourceInfo;
import com.zhangqiang.downloadmanager.speed.OnSpeedChangeListener;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.utils.IntentUtils;

import java.io.File;

public class M3u8DownloadTaskCell extends MultiCell<M3u8DownloadTask> {

    private static final String TAG = M3u8DownloadTaskCell.class.getSimpleName();
    private final View.OnLongClickListener onItemLongClickListener;

    public M3u8DownloadTaskCell(M3u8DownloadTask data, View.OnLongClickListener onItemLongClickListener) {
        super(R.layout.item_download_ftp, data, null);
        this.onItemLongClickListener = onItemLongClickListener;
    }

    @Override
    protected void onBindViewHolder(ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        View view = viewHolder.getView();
        final M3u8DownloadTask downloadTask = getData();
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
                    IntentUtils.openFileSmart(v.getContext(), file, null);
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
                    LogUtils.i(TAG,"=====onProgressChange======"+downloadTask.getCurrentDuration());
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
                public void onResourceInfoReady(M3u8ResourceInfo resourceInfo) {
                    LogUtils.i(TAG,"=====onResourceInfoReady======"+resourceInfo);
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
                downloadTask.getSpeedHelper().addOnSpeedChangeListener(onSpeedChangeListener);
                downloadTask.addOnResourceInfoReadyListener(onResourceInfoReadyListener);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                downloadTask.removeStatusChangeListener(onStatusChangeListener);
                downloadTask.removeOnProgressChangeListener(onProgressChangeListener);
                downloadTask.getSpeedHelper().removeOnSpeedChangeListener(onSpeedChangeListener);
                downloadTask.removeOnResourceInfoReadyListener(onResourceInfoReadyListener);
            }
        };
        view.addOnAttachStateChangeListener(onAttachStateChangeListener);
        view.setTag(onAttachStateChangeListener);
    }

    private void updateSpeed(ViewHolder viewHolder) {
        M3u8DownloadTask downloadTask = getData();
        long speed = downloadTask.getSpeedHelper().getSpeed();
        viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(speed) + "/s");

        String resetTimeStr;
        long totalDuration = getTotalDuration();
        if (totalDuration == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else if (totalDuration <= downloadTask.getCurrentDuration()) {

            if (downloadTask.getStatus() == Status.SUCCESS) {
                resetTimeStr = "已完成";
            } else {
                resetTimeStr = "剩余时间：0秒";
            }
        } else if (speed == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else {
            long resetTime = (totalDuration - downloadTask.getCurrentDuration()) / 1000;
            resetTimeStr = StringUtils.getRestTime(resetTime);
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
        DownloadTask data = getData();

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
        DownloadTask data = getData();
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
        M3u8DownloadTask data = getData();
        long currentDuration = data.getCurrentDuration();
        long totalDuration = getTotalDuration();
        int progress = (int) ((float) currentDuration / totalDuration * 100);
        viewHolder.setText(R.id.tv_progress, StringUtils.formatFileLength(data.getCurrentLength()) + "/" + StringUtils.formatFileLength(0));
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

    private long getTotalDuration() {
        M3u8ResourceInfo resourceInfo = getData().getResourceInfo();
        if (resourceInfo == null) {
            return 0;
        }
        return resourceInfo.getDuration();
    }

}
