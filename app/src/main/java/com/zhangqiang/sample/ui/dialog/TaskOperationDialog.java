package com.zhangqiang.sample.ui.dialog;

import android.view.View;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;

public class TaskOperationDialog extends BaseDialogFragment {


    private Listener listener;

    public static TaskOperationDialog newInstance() {
        return new TaskOperationDialog();
    }

    @Override
    protected boolean useBottomSheet() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_taks_operation;
    }

    @Override
    protected void initView(View view) {
        view.findViewById(R.id.bt_delete_task).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDeleteTaskClick();
                }
                getDialog().dismiss();
            }
        });
        view.findViewById(R.id.bt_copy_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCopyLinkClick();
                }
                getDialog().dismiss();
            }
        });
        view.findViewById(R.id.bt_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
    }

    public interface Listener {

        void onCopyLinkClick();

        void onDeleteTaskClick();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
