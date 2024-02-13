package com.zhangqiang.web.boomark.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.zhangqiang.celladapter.CellRVAdapter;
import com.zhangqiang.celladapter.cell.Cell;
import com.zhangqiang.common.dialog.BaseDialogFragment;
import com.zhangqiang.common.utils.BaseObserver;
import com.zhangqiang.common.utils.RXJavaUtils;
import com.zhangqiang.web.boomark.bean.BookMarkBean;
import com.zhangqiang.web.boomark.cell.BookMarkCell;
import com.zhangqiang.web.boomark.service.BookMarkService;
import com.zhangqiang.webview.R;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Function;

public class BookMarkDialog extends BaseDialogFragment {

    public interface OnBookmarkClickListener {
        void onBookmarkClick(BookMarkBean bookMarkBean);
    }

    private BookMarkService bookMarkService;
    private OnBookmarkClickListener onBookmarkClickListener;

    public static BookMarkDialog newInstance() {
        BookMarkDialog dialog = new BookMarkDialog();
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getContext();
        if (context != null) {
            bookMarkService = new BookMarkService(context);
        }
    }

    @Override
    protected boolean useBottomSheet() {
        return true;
    }

    @Override
    protected float getHeightRatio() {
        return 0.75f;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_book_mark;
    }

    @Override
    protected void initView(View view) {
        RecyclerView rvBookmark = view.findViewById(R.id.rv_bookmark);
        rvBookmark.setLayoutManager(new LinearLayoutManager(view.getContext()));
        rvBookmark.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.HORIZONTAL));
        CellRVAdapter adapter = new CellRVAdapter();
        rvBookmark.setAdapter(adapter);
        Observable.create(new ObservableOnSubscribe<List<BookMarkBean>>() {
                    @Override
                    public void subscribe(ObservableEmitter<List<BookMarkBean>> emitter) throws Exception {
                        emitter.onNext(bookMarkService.getBookMarks());
                    }
                })
                .compose(RXJavaUtils.applyIOMainSchedules())
                .compose(RXJavaUtils.bindLifecycle(this))
                .map(new Function<List<BookMarkBean>, List<Cell>>() {
                    @Override
                    public List<Cell> apply(List<BookMarkBean> bookMarkBeans) throws Exception {
                        ArrayList<Cell> cells = new ArrayList<>();
                        if (bookMarkBeans != null) {
                            for (BookMarkBean bookMarkBean : bookMarkBeans) {
                                cells.add(new BookMarkCell(bookMarkBean)
                                        .setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (onBookmarkClickListener != null) {
                                                    onBookmarkClickListener.onBookmarkClick(bookMarkBean);
                                                }
                                                dismiss();
                                            }
                                        }));
                            }
                        }
                        return cells;
                    }
                })
                .subscribe(new BaseObserver<List<Cell>>() {
                    @Override
                    public void onNext(List<Cell> cells) {
                        adapter.setDataList(cells);
                    }
                });
    }

    public BookMarkDialog setOnBookmarkClickListener(OnBookmarkClickListener onBookmarkClickListener) {
        this.onBookmarkClickListener = onBookmarkClickListener;
        return this;
    }
}
