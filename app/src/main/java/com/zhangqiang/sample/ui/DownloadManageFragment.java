package com.zhangqiang.sample.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.TaskInfo;
import com.zhangqiang.downloadmanager.listeners.DownloadTaskListener;
import com.zhangqiang.downloadmanager.listeners.UIDownloadTaskListener;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseFragment;
import com.zhangqiang.sample.ui.cell.DownloadTaskCell;
import com.zhangqiang.sample.ui.widget.LinearRVDivider;
import com.zhangqiang.sample.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DownloadManageFragment extends BaseFragment {

    private CellRVAdapter cellRVAdapter;
    public static final String TAG = "DownloadManagerFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_download_manager, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.m_recycler_view);
        cellRVAdapter = new CellRVAdapter();
        recyclerView.setAdapter(cellRVAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        ColorDrawable dividerDrawable = new ColorDrawable(Color.parseColor("#eeeeee"));
        dividerDrawable.setBounds(0,0,0, ScreenUtils.dp2Px(view.getContext(),10));
        recyclerView.addItemDecoration(new LinearRVDivider(dividerDrawable));
        return view;
    }

    public void refresh() {

        List<Cell> cellList = new ArrayList<>();
        List<TaskInfo> allTask = DownloadManager.getInstance(getContext()).getTaskList();
        if (allTask != null && !allTask.isEmpty()) {
            for (int i = 0; i < allTask.size(); i++) {
                cellList.add(makeCell(allTask.get(i)));
            }
        }
        cellRVAdapter.setDataList(cellList);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        DownloadManager.getInstance(getContext()).getDownloadTaskListeners()
                .addDownloadTaskListener(onProgressChangedListener);
        refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        DownloadManager.getInstance(getContext()).getDownloadTaskListeners()
                .removeDownloadTaskListener(onProgressChangedListener);
    }

    private Cell makeCell(TaskInfo downloadTask) {

        return new DownloadTaskCell(downloadTask, getChildFragmentManager());
    }

    private final DownloadTaskListener onProgressChangedListener = new UIDownloadTaskListener() {
        @Override
        public void onTaskProgressChangedMain(String id) {
            DownloadTaskCell cell = findCellByTaskId(id);
            if (cell != null) {
                cell.updateProgress();
            }
        }

        @Override
        protected void onTaskSpeedChangedMain(String id) {
            DownloadTaskCell cell = findCellByTaskId(id);
            if (cell != null) {
                cell.updateSpeed();
            }
        }

        @Override
        public void onTaskInfoChangedMain(String id) {
            DownloadTaskCell cell = findCellByTaskId(id);
            if (cell != null) {
                cell.updateInfo();
            }
        }

        @Override
        public void onTaskStateChangedMain(String id) {
            DownloadTaskCell cell = findCellByTaskId(id);
            if (cell != null) {
                cell.updateState();
            }
        }

        @Override
        public void onTaskRemovedMain(String id) {
            int dataCount = cellRVAdapter.getDataCount();
            for (int i = dataCount - 1; i >= 0; i--) {
                DownloadTaskCell cell = (DownloadTaskCell) cellRVAdapter.getDataAt(i);
                TaskInfo taskEntity = cell.getData();
                if (Objects.equals(taskEntity.getId(), id)) {
                    cellRVAdapter.removeDataAtIndex(i);
                }
            }
        }

        @Override
        public void onTaskAddedMain(String id) {
            TaskInfo task = DownloadManager.getInstance(getContext()).getTaskInfo(id);
            if (task != null) {
                cellRVAdapter.addDataAtFirst(makeCell(task));
            }
        }

        @Override
        public void onActiveTaskSizeChangedMain() {

        }
    };

    private DownloadTaskCell findCellByTaskId(String id) {
        int dataCount = cellRVAdapter.getDataCount();
        for (int i = 0; i < dataCount; i++) {
            DownloadTaskCell cell = (DownloadTaskCell) cellRVAdapter.getDataAt(i);
            TaskInfo taskEntity = cell.getData();
            if (Objects.equals(taskEntity.getId(), id)) {
                return cell;
            }
        }
        return null;
    }
}
