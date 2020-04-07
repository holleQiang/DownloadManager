package com.zhangqiang.sample.ui.dialog;

import android.view.View;
import android.widget.CheckBox;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;

public class TaskDeleteConfirmDialog extends BaseDialogFragment {

    private Listener listener;
    private CheckBox cbDeleteFile;

    public static TaskDeleteConfirmDialog newInstance() {
        return new TaskDeleteConfirmDialog();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_delete_task_confirm;
    }

    @Override
    protected void initView(View view) {
        cbDeleteFile = view.findViewById(R.id.cb_delete_file);
        view.findViewById(R.id.bt_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onConfirm(cbDeleteFile.isChecked());
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

    @Override
    protected boolean useBottomSheet() {
        return true;
    }

    public interface Listener {

        void onConfirm(boolean deleteFile);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
