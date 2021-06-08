package com.zhangqiang.sample.ui.cell;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.util.TypedValue;
import android.view.View;

import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.action.Action;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.ui.dialog.TaskOperationDialog;
import com.zhangqiang.sample.ui.widget.MultiProgressView;
import com.zhangqiang.sample.utils.IntentUtils;

import java.io.File;

public class DownloadTaskCell extends MultiCell<TaskInfo> {

    private static final String TAG = "DownloadTaskCell";
    private final FragmentManager fragmentManager;

    public DownloadTaskCell(TaskInfo data, FragmentManager fragmentManager) {
        super(R.layout.item_download, data, null);
        this.fragmentManager = fragmentManager;
    }


    @Override
    protected void onBindViewHolder(final ViewHolder viewHolder) {
        super.onBindViewHolder(viewHolder);
        final Context context = viewHolder.getView().getContext();
        final TaskInfo entity = getData();

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
        updateSpeed(viewHolder);
        viewHolder.getView().setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                TaskOperationDialog dialog = TaskOperationDialog.newInstance(getData().getId());
                dialog.show(fragmentManager, "task_operate_dialog");
                return true;
            }
        });
    }

    private void updateSpeed(ViewHolder viewHolder) {
        TaskInfo data = getData();
        viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(data.getSpeed()) + "/s");

        String resetTimeStr;
        if (data.getContentLength() == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else if (data.getContentLength() <= data.getCurrentLength()) {
            resetTimeStr = "已完成";
        } else if (data.getSpeed() == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else {
            long resetLength = data.getContentLength() - data.getCurrentLength();
            long resetTime = resetLength / data.getSpeed();
            if (resetTime <= 0) {
                resetTimeStr = "剩余时间：" + "0秒";
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
        TaskInfo data = getData();

        int status = data.getState();
        LogUtils.i(TAG, "=====updateState2======" + getData().getState());
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
        LogUtils.i(TAG, "=====updateState3======" + getData().getState());
    }

    private void updateInfo(ViewHolder viewHolder) {
        TaskInfo data = getData();
        viewHolder.setText(R.id.tv_file_name, data.getFileName());
    }

    private void changeVisible(ViewHolder viewHolder, boolean isError) {

        viewHolder.setVisibility(R.id.tv_error, isError ? View.VISIBLE : View.INVISIBLE);
        viewHolder.setVisibility(R.id.pb_download_progress, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_speed, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_progress, isError ? View.INVISIBLE : View.VISIBLE);
        viewHolder.setVisibility(R.id.tv_rest_time, isError ? View.INVISIBLE : View.VISIBLE);
    }

    private void updateProgress(ViewHolder viewHolder) {
        TaskInfo data = getData();
        long currentLength = data.getCurrentLength();
        long totalLength = data.getContentLength();
//        int progress = (int) ((float) currentLength / totalLength * 100);
        viewHolder.setText(R.id.tv_progress, StringUtils.formatFileLength(currentLength) + "/" + StringUtils.formatFileLength(totalLength));
        MultiProgressView multiProgressView = viewHolder.getView(R.id.pb_download_progress);
        multiProgressView.clear();
        TypedValue outValue = new TypedValue();
        viewHolder.getView().getContext().getTheme().resolveAttribute(R.attr.colorPrimary, outValue,false);
        int resourceId = outValue.resourceId;
        int partSize = data.getPartCount();
        for (int i = 0; i < partSize; i++) {
            long threadCurrentLength =  data.getPartCurrentLength(i);
            long threadContentLength =  data.getPartContentLength(i);
            int progress = (int) (((float)threadCurrentLength/threadContentLength)*100);
            multiProgressView.addProgressEntry(i,
                    progress,
                    100,
                    Color.rgb(128 / partSize * (i + 1), 128 / partSize * (i + 1), 128 / partSize * (i + 1) + 90),
                    null);
        }
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
        LogUtils.i(TAG, "=====updateState1======" + getData().getState());
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateState(viewHolder);
            }
        });
    }
}
