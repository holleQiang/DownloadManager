package com.zhangqiang.sample;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.celladapter.cell.MultiCell;
import com.zhangqiang.celladapter.cell.ViewHolderBinder;
import com.zhangqiang.celladapter.vh.ViewHolder;
import com.zhangqiang.downloadmanager.DownloadManager;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.UIDownloadListener;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager.utils.StringUtils;
import com.zhangqiang.sample.utils.IntentUtils;

import java.io.File;
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
        List<DownloadTask> allTask = DownloadManager.getInstance().getAllTask();
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

    private Cell makeCell(DownloadTask downloadTask) {

        return new MultiCell<>(R.layout.item_download, downloadTask, new ViewHolderBinder<DownloadTask>() {

            void updateState(DownloadTask downloadTask, ViewHolder viewHolder) {
                int status = downloadTask.getState();
                if (status == DownloadTask.STATE_IDLE) {
                    viewHolder.setText(R.id.bt_state, R.string.download);
                } else if (status == DownloadTask.STATE_DOWNLOADING) {
                    viewHolder.setText(R.id.bt_state, R.string.pause);
                } else if (status == DownloadTask.STATE_COMPLETE) {
                    viewHolder.setText(R.id.bt_state, R.string.open);
                } else if (status == DownloadTask.STATE_FAIL) {
                    viewHolder.setText(R.id.bt_state, R.string.fail);
                } else if (status == DownloadTask.STATE_PAUSE) {
                    viewHolder.setText(R.id.bt_state, R.string.continue_download);
                }
            }

            void updateProgress(DownloadTask downloadTask, ViewHolder viewHolder) {
                long currentLength = downloadTask.getCurrentLength();
                long totalLength = downloadTask.getTotalLength();
                int progress = (int) ((float) currentLength / totalLength * 100);
                viewHolder.setProgress(R.id.pb_download_progress, progress);
                viewHolder.setText(R.id.tv_progress, StringUtils.formatFileLength(currentLength) + "/" + StringUtils.formatFileLength(totalLength));
            }

            @Override
            public void onBind(final ViewHolder viewHolder, final DownloadTask downloadTask) {
                viewHolder.setText(R.id.tv_file_name, downloadTask.getFileName());
                updateState(downloadTask, viewHolder);
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
                        downloadListener.startSpeedCalculator();
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        downloadTask.removeDownloadListener(downloadListener);
                        downloadListener.stopSpeedCalculator();
                    }
                });

                viewHolder.getView().setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new AlertDialog.Builder(v.getContext())
                                .setTitle(R.string.title)
                                .setMessage(R.string.delete_task)
                                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                })
                                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        DownloadManager.getInstance().deleteTask(downloadTask);
                                        dialog.dismiss();
                                        refresh();
                                    }
                                })
                                .show();
                        return true;
                    }
                });
            }
        });
    }
}
