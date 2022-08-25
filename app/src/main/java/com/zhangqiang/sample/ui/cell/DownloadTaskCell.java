package com.zhangqiang.sample.ui.cell;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;

import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.ViewHolderBinder;
import com.zhangqiang.celladapter.cell.action.Action;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.ui.dialog.TaskOperationDialog;
import com.zhangqiang.sample.ui.widget.LinearRVDivider;
import com.zhangqiang.sample.utils.IntentUtils;
import com.zhangqiang.sample.utils.ResourceUtils;
import com.zhangqiang.sample.utils.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadTaskCell extends MultiCell<TaskInfo> {

    private static final String TAG = "DownloadTaskCell";
    private final FragmentManager fragmentManager;
    private boolean showPartInfo;

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
        final TaskInfo taskInfo = getData();
        int partCount = taskInfo.getPartCount();
        for (int i = 0; i < partCount; i++) {

            cells.add(new MultiCell<>(R.layout.item_part_info, i, new ViewHolderBinder<Integer>() {
                @Override
                public void onBind(ViewHolder viewHolder, Integer i) {
                    long partSpeed = taskInfo.getPartSpeed(i);
                    final long partContentLength = taskInfo.getPartContentLength(i);
                    final long partCurrentLength = taskInfo.getPartCurrentLength(i);
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
        TaskInfo data = getData();
        viewHolder.setText(R.id.tv_speed, StringUtils.formatFileLength(data.getSpeed()) + "/s");

        String resetTimeStr;
        if (data.getContentLength() == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else if (data.getContentLength() <= data.getCurrentLength()) {
            if (data.getState() == DownloadManager.STATE_COMPLETE) {
                resetTimeStr = "已完成";
            } else {
                resetTimeStr = "剩余时间：0秒";
            }
        } else if (data.getSpeed() == 0) {
            resetTimeStr = "剩余时间：" + "未知";
        } else {
            long resetLength = data.getContentLength() - data.getCurrentLength();
            long resetTime = resetLength / data.getSpeed();
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
        TaskInfo data = getData();

        int status = data.getState();
        if (status == DownloadManager.STATE_IDLE) {
            viewHolder.setText(R.id.bt_state, R.string.waiting);
            changeVisibleByError(viewHolder, false);
            viewHolder.setVisibility(R.id.tv_speed, View.INVISIBLE);
        } else if (status == DownloadManager.STATE_DOWNLOADING) {
            viewHolder.setText(R.id.bt_state, R.string.pause);
            changeVisibleByError(viewHolder, false);
            viewHolder.setVisibility(R.id.tv_speed, View.VISIBLE);
        } else if (status == DownloadManager.STATE_COMPLETE) {
            viewHolder.setText(R.id.bt_state, R.string.open);
            changeVisibleByError(viewHolder, false);
            viewHolder.setVisibility(R.id.tv_speed, View.INVISIBLE);
        } else if (status == DownloadManager.STATE_FAIL) {
            viewHolder.setText(R.id.bt_state, R.string.fail);
            changeVisibleByError(viewHolder, true);
            viewHolder.setText(R.id.tv_error, data.getErrorMsg());
            viewHolder.setVisibility(R.id.tv_speed, View.INVISIBLE);
        } else if (status == DownloadManager.STATE_PAUSE) {
            viewHolder.setText(R.id.bt_state, R.string.continue_download);
            changeVisibleByError(viewHolder, false);
            viewHolder.setVisibility(R.id.tv_speed, View.INVISIBLE);
        }
    }

    private void updateInfo(ViewHolder viewHolder) {
        TaskInfo data = getData();
        viewHolder.setText(R.id.tv_file_name, data.getFileName());
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
        TaskInfo data = getData();
        long currentLength = data.getCurrentLength();
        long totalLength = data.getContentLength();
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
        invalidate(new Action() {
            @Override
            public void onBind(ViewHolder viewHolder) {
                updateState(viewHolder);
            }
        });
    }
}
