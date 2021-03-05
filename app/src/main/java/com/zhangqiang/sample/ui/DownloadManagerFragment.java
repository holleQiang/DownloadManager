package com.zhangqiang.sample.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.db.entity.TaskEntity;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseFragment;
import com.zhangqiang.sample.ui.cell.DownloadTaskCell;

import java.util.ArrayList;
import java.util.List;

public class DownloadManagerFragment extends BaseFragment {

    private RecyclerView recyclerView;
    private CellRVAdapter cellRVAdapter;
    public static final String TAG = "DownloadManagerFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_download_manager, container, false);
        recyclerView = view.findViewById(R.id.m_recycler_view);
        cellRVAdapter = new CellRVAdapter();
        recyclerView.setAdapter(cellRVAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        return view;
    }

    public void refresh() {

        List<Cell> cellList = new ArrayList<>();
        List<TaskEntity> allTask = DownloadManager.getInstance(getContext()).getTaskList();
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
        refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private Cell makeCell(TaskEntity downloadTask) {

        return new DownloadTaskCell(downloadTask, getChildFragmentManager());
    }


}
