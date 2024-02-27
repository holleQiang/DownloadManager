package com.zhangqiang.web.history.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.common.fragment.BaseFragment;
import com.zhangqiang.common.utils.BaseObserver;
import com.zhangqiang.common.utils.RXJavaUtils;
import com.zhangqiang.web.history.bean.VisitRecordBean;
import com.zhangqiang.web.history.cell.VisitDateCell;
import com.zhangqiang.web.history.cell.VisitRecordCell;
import com.zhangqiang.web.history.service.VisitRecordService;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

public class HistoryFragment extends BaseFragment {

    private CellRVAdapter cellRVAdapter;

    public interface OnVisitRecordClickListener {
        void onVisitRecordClick(VisitRecordBean visitRecordBean);
    }

    private OnVisitRecordClickListener onVisitRecordClickListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvHistory = view.findViewById(R.id.rv_history);
        rvHistory.setLayoutManager(new LinearLayoutManager(view.getContext()));
        cellRVAdapter = new CellRVAdapter();
        rvHistory.setAdapter(cellRVAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshHistoryList();
    }

    private void refreshHistoryList() {
        Observable.create(new ObservableOnSubscribe<List<VisitRecordBean>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<VisitRecordBean>> emitter) throws Exception {
                        try {
                            FragmentActivity activity = getActivity();
                            if (activity == null) {
                                throw new RuntimeException("activity is null");
                            }
                            emitter.onNext(new VisitRecordService(activity).getVisitRecords());
                        } catch (Throwable e) {
                            emitter.tryOnError(e);
                        }
                    }
                })
                .compose(RXJavaUtils.applyIOMainSchedules())
                .compose(RXJavaUtils.bindLifecycle(this))
                .map(new Function<List<VisitRecordBean>, List<Cell>>() {
                    @Override
                    public List<Cell> apply(List<VisitRecordBean> visitRecordBeans) throws Exception {
                        List<Cell> cells = new ArrayList<>();
                        long currentDate = System.currentTimeMillis();
                        int lastLevel = 0;
                        for (VisitRecordBean visitRecordBean : visitRecordBeans) {
                            long visitDate = visitRecordBean.getVisitDate();
                            long pastDate = currentDate - visitDate;
                            if (pastDate < 60 * 60 * 1000) {
                                if (lastLevel != 1) {
                                    lastLevel = 1;
                                    cells.add(new VisitDateCell(getString(R.string.last_one_hour)));
                                }
                            } else if (pastDate < 24 * 60 * 60 * 1000) {
                                if (lastLevel != 2) {
                                    lastLevel = 2;
                                    cells.add(new VisitDateCell(getString(R.string.last_one_day)));
                                }
                            } else if (pastDate < 30L * 24 * 60 * 60 * 1000) {
                                if (lastLevel != 3) {
                                    lastLevel = 3;
                                    cells.add(new VisitDateCell(getString(R.string.last_one_month)));
                                }
                            } else {
                                if (lastLevel != 4) {
                                    lastLevel = 4;
                                    cells.add(new VisitDateCell(getString(R.string.one_month_before)));
                                }
                            }
                            cells.add(new VisitRecordCell(visitRecordBean, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (onVisitRecordClickListener != null) {
                                        onVisitRecordClickListener.onVisitRecordClick(visitRecordBean);
                                    }
                                }
                            }, new VisitRecordCell.OnVisitRecordLongClickListener() {
                                @Override
                                public void onVisitRecordLongClick(VisitRecordBean data, int position) {
                                    FragmentActivity activity = getActivity();
                                    if (activity == null) {
                                        return;
                                    }
                                    new AlertDialog.Builder(activity).setTitle(R.string.tips)
                                            .setMessage(R.string.delete_record)
                                            .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.cancel();
                                                }
                                            })
                                            .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    new VisitRecordService(activity).remove(data.getId());
                                                    cellRVAdapter.removeDataAtIndex(position);
                                                    dialog.dismiss();
                                                }
                                            })
                                            .show();
                                }
                            }));
                        }
                        return cells;
                    }
                })
                .subscribe(new BaseObserver<List<Cell>>() {
                    @Override
                    public void onNext(List<Cell> cells) {
                        cellRVAdapter.setDataList(cells);
                    }
                });
    }

    public HistoryFragment setOnVisitRecordClickListener(OnVisitRecordClickListener onVisitRecordClickListener) {
        this.onVisitRecordClickListener = onVisitRecordClickListener;
        return this;
    }
}
