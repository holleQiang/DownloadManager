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
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.common.fragment.BaseFragment;
import com.zhangqiang.common.utils.BaseObserver;
import com.zhangqiang.common.utils.RXJavaUtils;
import com.zhangqiang.web.history.bean.VisitRecordBean;
import com.zhangqiang.web.history.cell.VisitRecordCell;
import com.zhangqiang.web.history.service.VisitRecordService;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

public class HistoryFragment extends BaseFragment {

    public interface OnVisitRecordClickListener{
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
        rvHistory.setLayoutManager(new GridLayoutManager(view.getContext(), 4));
        CellRVAdapter cellRVAdapter = new CellRVAdapter();
        rvHistory.setAdapter(cellRVAdapter);

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
                .subscribe(new BaseObserver<List<VisitRecordBean>>() {
                    @Override
                    public void onNext(List<VisitRecordBean> visitRecordBeans) {
                        List<VisitRecordCell> cells = new ArrayList<>();
                        for (VisitRecordBean visitRecordBean : visitRecordBeans) {
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
                        cellRVAdapter.setDataList(cells);
                    }
                });
    }

    public HistoryFragment setOnVisitRecordClickListener(OnVisitRecordClickListener onVisitRecordClickListener) {
        this.onVisitRecordClickListener = onVisitRecordClickListener;
        return this;
    }
}
