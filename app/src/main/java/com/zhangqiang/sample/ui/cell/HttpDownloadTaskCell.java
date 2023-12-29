package com.zhangqiang.sample.ui.cell;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.ViewHolderBinder;
import com.zhangqiang.celladapter.cell.action.Action;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.speed.OnSpeedChangeListener;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.HttpPartDownloadTask;
import com.zhangqiang.downloadmanager.plugin.http.task.OnProgressChangeListener;
import com.zhangqiang.downloadmanager.plugin.http.task.OnResourceInfoReadyListener;
import com.zhangqiang.downloadmanager.plugin.http.task.ResourceInfo;
import com.zhangqiang.downloadmanager.task.OnSaveFileNameChangeListener;
import com.zhangqiang.downloadmanager.task.OnStatusChangeListener;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.ui.cell.icon.ApkIconProvider;
import com.zhangqiang.sample.ui.cell.icon.BackgroundProvider;
import com.zhangqiang.sample.ui.cell.icon.DefaultIconProvider;
import com.zhangqiang.sample.ui.cell.icon.FileIconProvider;
import com.zhangqiang.sample.ui.cell.icon.ImageIconProvider;
import com.zhangqiang.sample.ui.cell.icon.VideoIconProvider;
import com.zhangqiang.sample.ui.widget.LinearRVDivider;
import com.zhangqiang.sample.utils.IntentUtils;
import com.zhangqiang.sample.utils.ResourceUtils;
import com.zhangqiang.sample.utils.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HttpDownloadTaskCell extends MultiCell<HttpDownloadTask> {


    private static final String TAG = HttpDownloadTaskCell.class.getSimpleName();
    private boolean showPartInfo;
    private final LifecycleOwner lifecycleOwner;
    private final View.OnLongClickListener onItemLongClickListener;
    private boolean debugMode;

    public HttpDownloadTaskCell(HttpDownloadTask data,
                                LifecycleOwner lifecycleOwner,
                                View.OnLongClickListener onItemLongClickListener,
                                boolean debugMode) {
        super(R.layout.item_download, data, null);
        this.lifecycleOwner = lifecycleOwner;
        this.onItemLongClickListener = onItemLongClickListener;
        this.debugMode = debugMode;
    }


    @Override
    protected void onBindViewHolder(final ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        View view = viewHolder.getView();
        final Context context = view.getContext();
        final HttpDownloadTask downloadTask = getData();

        updateIcon(viewHolder);
        updateButton(viewHolder);
        updateFailView(viewHolder);
        updateFileName(viewHolder);
        updateProgress(viewHolder);
        updatePartInfo(viewHolder);
        updateSpeed(viewHolder);
        updateDebugView(viewHolder);

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
        viewHolder.setOnCheckedChangeListener(R.id.cb_show_part_info, null);
        viewHolder.setChecked(R.id.cb_show_part_info, showPartInfo);
        viewHolder.setOnCheckedChangeListener(R.id.cb_show_part_info, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showPartInfo = isChecked;
                updatePartInfo(viewHolder);
            }
        });
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
                            updateButton();
                            updateFileIcon();
                            updateFailView();
                            updateSpeed();
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
                            updateFileName();
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
                            updateFileName();
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

    private void updateDebugView(ViewHolder viewHolder) {
        viewHolder.setVisibility(R.id.cb_show_part_info, debugMode ? View.VISIBLE : View.GONE);
        viewHolder.setVisibility(R.id.rv_part_info, debugMode ? View.VISIBLE : View.GONE);
    }

    private void updateFailView(ViewHolder viewHolder) {
        HttpDownloadTask data = getData();
        Status status = data.getStatus();
        if (status == Status.FAIL) {
            viewHolder.setVisibility(R.id.tv_error, View.VISIBLE);
            viewHolder.setText(R.id.tv_error, data.getErrorMessage());
        } else {
            viewHolder.setVisibility(R.id.tv_error, View.GONE);
        }
    }

    private void updateFailView() {
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateFailView(viewHolder);
            }
        });
    }

    private void updateIcon(ViewHolder viewHolder) {
        HttpDownloadTask downloadTask = getData();
        Status status = downloadTask.getStatus();
        FileIconProvider fileIconProvider;
        BackgroundProvider backgroundProvider = null;
        String saveFileName = downloadTask.getSaveFileName();
        String suffix = FileUtils.getSuffix(saveFileName);
        File file = new File(downloadTask.getSaveDir(), saveFileName);
        if ("apk".equalsIgnoreCase(suffix)) {
            fileIconProvider = new ApkIconProvider(lifecycleOwner, viewHolder.getView().getContext());
        } else if ("jpg".equalsIgnoreCase(suffix)
                || "png".equalsIgnoreCase(suffix)
                || "webp".equalsIgnoreCase(suffix)
                || "jpeg".equalsIgnoreCase(suffix)) {
            ImageIconProvider imageIconProvider = new ImageIconProvider();
            fileIconProvider = imageIconProvider;
            backgroundProvider = imageIconProvider;
        } else if ("mp4".equalsIgnoreCase(suffix)) {
            fileIconProvider = new VideoIconProvider();
        } else {
            fileIconProvider = new DefaultIconProvider();
        }
        if (status == Status.SUCCESS) {
            if (file.exists()) {
                fileIconProvider.loadFileIcon(viewHolder.getView(R.id.iv_file_icon), file);
//                if (backgroundProvider != null) {
//                    backgroundProvider.loadBackground(viewHolder.getView(R.id.iv_background), file);
//                }
                return;
            }
        }
        viewHolder.setImageResource(R.id.iv_file_icon, fileIconProvider.defaultIconResource());
        viewHolder.setImageDrawable(R.id.iv_background, null);
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
        Status status = data.getStatus();
        if (status != Status.DOWNLOADING) {
            viewHolder.setVisibility(R.id.tv_rest_time, View.GONE);
            viewHolder.setVisibility(R.id.tv_speed, View.GONE);
        } else {
            viewHolder.setVisibility(R.id.tv_rest_time, View.VISIBLE);
            viewHolder.setVisibility(R.id.tv_speed, View.VISIBLE);

            long speed = data.getSpeedHelper().getSpeed();
            viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(speed) + "/s");

            String resetTimeStr;
            long contentLength = getContentLength();
            if (contentLength == 0) {
                resetTimeStr = "剩余时间：" + "未知";
            } else if (contentLength <= data.getCurrentLength()) {
                resetTimeStr = "剩余时间：0秒";
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

    private void updateButton(ViewHolder viewHolder) {
        HttpDownloadTask data = getData();

        Status status = data.getStatus();
        if (status == Status.IDLE) {
            viewHolder.setText(R.id.bt_state, R.string.waiting);
            viewHolder.setEnable(R.id.bt_state, true);
        } else if (status == Status.DOWNLOADING) {
            viewHolder.setText(R.id.bt_state, R.string.pause);
            viewHolder.setEnable(R.id.bt_state, true);
        } else if (status == Status.SUCCESS) {
            File file = new File(data.getSaveDir(), data.getSaveFileName());
            if (file.exists()) {
                viewHolder.setText(R.id.bt_state, R.string.open);
                viewHolder.setEnable(R.id.bt_state, true);
            } else {
                viewHolder.setText(R.id.bt_state, R.string.file_not_exists);
                viewHolder.setEnable(R.id.bt_state, false);
            }
        } else if (status == Status.FAIL) {
            viewHolder.setText(R.id.bt_state, R.string.fail);
            viewHolder.setEnable(R.id.bt_state, true);
        } else if (status == Status.CANCELED) {
            viewHolder.setText(R.id.bt_state, R.string.continue_download);
            viewHolder.setEnable(R.id.bt_state, true);
        }
    }

    private void updateFileName(ViewHolder viewHolder) {
        HttpDownloadTask data = getData();
        viewHolder.setText(R.id.tv_file_name, data.getSaveFileName());
    }

    private void updateProgress(ViewHolder viewHolder) {
        HttpDownloadTask data = getData();
        Status status = data.getStatus();
        long totalLength = getContentLength();
        if (status == Status.SUCCESS) {
            viewHolder.setVisibility(R.id.pb_download_progress, View.GONE);
            viewHolder.setText(R.id.tv_progress, StringUtils.formatFileLength(totalLength));
        } else {
            viewHolder.setVisibility(R.id.pb_download_progress, View.VISIBLE);
            long currentLength = data.getCurrentLength();
            int progress = (int) ((float) currentLength / totalLength * 100);
            viewHolder.setText(R.id.tv_progress, StringUtils.formatFileLength(currentLength) + "/" + StringUtils.formatFileLength(totalLength));
            ProgressBar progressBar = viewHolder.getView(R.id.pb_download_progress);
            progressBar.setProgress(progress);
        }
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

    public void updateFileName() {
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateFileName(viewHolder);
            }
        });
    }

    public void updateButton() {
        LogUtils.i(TAG, "======updateState=========" + getData().getStatus());
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateButton(viewHolder);
            }
        });
    }

    private void updateFileIcon() {
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateIcon(viewHolder);
            }
        });
    }

    private long getContentLength() {
        ResourceInfo resourceInfo = getData().getResourceInfo();
        return resourceInfo == null ? 0 : resourceInfo.getContentLength();
    }

    public void setDebugMode(boolean debugMode) {
        if (this.debugMode != debugMode) {
            this.debugMode = debugMode;
            invalidate();
        }
    }
}
