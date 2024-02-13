package com.zhangqiang.web.boomark.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;

import com.zhangqiang.common.dialog.BaseDialogFragment;
import com.zhangqiang.webview.R;

public class BookmarkTitleEditDialog extends BaseDialogFragment {

    public static final String KEY_OLD_TITLE = "old_title";
    private String oldTitle;

    public interface OnConfirmListener {
        void onConfirm(String title);
    }

    public static BookmarkTitleEditDialog newInstance(String oldTitle) {
        BookmarkTitleEditDialog dialog = new BookmarkTitleEditDialog();
        Bundle args = new Bundle();
        args.putString(KEY_OLD_TITLE, oldTitle);
        dialog.setArguments(args);
        return dialog;
    }

    private OnConfirmListener onConfirmListener;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            oldTitle = arguments.getString(KEY_OLD_TITLE);
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_add_bookmark;
    }

    @Override
    protected void initView(View view) {
        EditText etTitle = view.findViewById(R.id.et_title);
        etTitle.setText(oldTitle);
        view.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onConfirmListener != null) {
                    onConfirmListener.onConfirm(etTitle.getText().toString());
                }
                dismiss();
            }
        });
    }

    public BookmarkTitleEditDialog setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
        return this;
    }
}
