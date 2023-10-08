package com.zhangqiang.sample.ui.cell;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.ViewHolderBinder;
import com.zhangqiang.celladapter.cell.action.Action;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.task.speed.OnSpeedChangeListener;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.downloadmanager2.plugin.http.task.HttpDownloadTask;
import com.zhangqiang.downloadmanager2.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager2.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager2.plugin.http.task.OnResourceInfoReadyListener;
import com.zhangqiang.downloadmanager2.plugin.http.task.ResourceInfo;
import com.zhangqiang.downloadmanager2.task.OnSaveFileNameChangeListener;
import com.zhangqiang.downloadmanager2.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager2.task.Status;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.ui.widget.LinearRVDivider;
import com.zhangqiang.sample.utils.IntentUtils;
import com.zhangqiang.sample.utils.ResourceUtils;
import com.zhangqiang.sample.utils.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HttpDownloadTaskCell extends MultiCell<HttpDownloadTask> {

    public interface OnLongClickListener {

        void onLongClick();
    }

    private static final String TAG = HttpDownloadTaskCell.class.getSimpleName();
    private boolean showPartInfo;
    private OnLongClickListener onLongClickListener;

    public HttpDownloadTaskCell(HttpDownloadTask data) {
        super(R.layout.item_download, data, null);
    }


    @Override
    protected void onBindViewHolder(final ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        View view = viewHolder.getView();
        final Context context = view.getContext();
        final HttpDownloadTask downloadTask = getData();

        updateState(viewHolder);
        updateInfo(viewHolder);
        updateProgress(viewHolder);
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
        updateSpeed(viewHolder);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                if (onLongClickListener != null) {
                    onLongClickListener.onLongClick();
                }
                return true;
            }
        });
        viewHolder.setOnCheckedChangeListener(R.id.cb_show_part_info, null);
        viewHolder.setChecked(R.id.cb_show_part_info, showPartInfo);
        viewHolder.setOnCheckedChangeListener(R.id.cb_show_part_info, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showPartInfo = isChecked;
                updatePartInfo(viewHolder);
            }
        });
        updatePartInfo(viewHolder);
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
            final OnSaveFileNameChangeListener onSaveFileNameChangeListener = new OnSaveFileNameChangeListener() {
                @Override
                public void onSaveFileNameChange(String name, String oldName) {
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            updateInfo();
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
                downloadTask.addOnSaveFileNameChangeListener(onSaveFileNameChangeListener);
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                downloadTask.removeStatusChangeListener(onStatusChangeListener);
                downloadTask.removeOnProgressChangeListener(onProgressChangeListener);
                downloadTask.getSpeedHelper().removeOnSpeedChangeListener(onSpeedChangeListener);
                downloadTask.removeOnResourceInfoReadyListener(onResourceInfoReadyListener);
                downloadTask.removeOnSaveFileNameChangeListener(onSaveFileNameChangeListener);
            }
        };
        view.addOnAttachStateChangeListener(onAttachStateChangeListener);
        view.setTag(onAttachStateChangeListener);
    }

    private void updatePartInfo(ViewHolder viewHolder) {
        RecyclerView rvPartInfo = viewHolder.getView(R.id.rv_part_info);
        Context context = viewHolder.getView().getContext();
        if (showPartInfo) {
            rvPartInfo.setLayoutManager(new LinearLayoutManager(context));
            CellRVAdapter adapter = new CellRVAdapter();
            adapter.setDataList(makePartCells());
            rvPartInfo.setAdapter(adapter);
        } else {
            rvPartInfo.setAdapter(null);
        }
        if (rvPartInfo.getItemDecorationCount() <= 0) {
            int attrColor = ResourceUtils.getAttrColor(context, R.attr.colorAccent);
            ColorDrawable dividerDrawable = new ColorDrawable(attrColor);
            dividerDrawable.setBounds(0, 0, 0, ScreenUtils.dp2Px(context, 1));
            rvPartInfo.addItemDecoration(new LinearRVDivider(dividerDrawable));
        }
    }

    private List<Cell> makePartCells() {
        List<Cell> cells = new ArrayList<>();
        final HttpDownloadTask httpDownloadTask = getData();
        int partCount = httpDownloadTask.getPartDownloadTaskCount();
        for (int i = 0; i < partCount; i++) {
            HttpPartDownloadTask httpPartDownloadTask = httpDownloadTask.getPartDownloadTaskAt(i);
            cells.add(new MultiCell<>(R.layout.item_part_info, i, new ViewHolderBinder<Integer>() {
                @Override
                public void onBind(ViewHolder viewHolder, Integer i) {
                    long partSpeed = httpPartDownloadTask.getSpeedHelper().getSpeed();
                    final long partContentLength = httpPartDownloadTask.getEndPosition() - httpPartDownloadTask.getStartPosition();
                    final long partCurrentLength = httpPartDownloadTask.getCurrentLength();
                    viewHolder.setText(R.id.tv_progress, StringUtils.formatFileLength(partCurrentLength) + "/" + StringUtils.formatFileLength(partContentLength));
                    viewHolder.setProgress(R.id.pb_progress, (int) ((float) partCurrentLength / partContentLength * 100));
                    viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(partSpeed) + "/s");
                    viewHolder.setText(R.id.tv_part_index, String.valueOf(i));
                }
            }));
        }
        return cells;
    }

    private void updateSpeed(ViewHolder viewHolder) {
        HttpDownloadTask data = getData();
        long speed = data.getSpeedHelper().getSpeed();
        viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(speed) + "/s");

        String resetTimeStr;
        long contentLength = getContentLength();
        if (contentLength == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else if (contentLength <= data.getCurrentLength()) {

            if (data.getStatus() == Status.SUCCESS) {
                resetTimeStr = "已完成";
            } else {
                resetTimeStr = "剩余时间：0秒";
            }
        } else if (speed == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else {
            long resetLength = contentLength - data.getCurrentLength();
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
                updatePartInfo(viewHolder);
            }
        });
    }

    private void updateState(ViewHolder viewHolder) {
        HttpDownloadTask data = getData();

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
        HttpDownloadTask data = getData();
        viewHolder.setText(R.id.tv_file_name, data.getSaveFileName());
    }

    private void changeVisibleByError(ViewHolder viewHolder, boolean isError) {

        viewHolder.setVisibility(R.id.tv_error, isError ? View.VISIBLE : View.INVISIBLE);
        viewHolder.setVisibility(R.id.pb_download_progress, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_speed, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_progress, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_rest_time, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.cb_show_part_info, isError ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateProgress(ViewHolder viewHolder) {
        HttpDownloadTask data = getData();
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
                updatePartInfo(viewHolder);
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

    public HttpDownloadTaskCell setOnLongClickListener(OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
        return this;
    }
}
