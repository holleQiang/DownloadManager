package com.zhangqiang.sample.ui.dialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;
import com.zhangqiang.sample.databinding.DialogTaksOperationBinding;

public class TaskOperationDialog extends BaseDialogFragment {

    private DialogTaksOperationBinding binding;

    public interface OperationListener {
        void onDelete(boolean deleteFile);

        void onCopyLink();

        void onRestart();

        void onOpenDirClick();

        void onLookupQRCodeClick();
    }

    private OperationListener operationListener;

    public static TaskOperationDialog newInstance() {
        Bundle arg = new Bundle();
        TaskOperationDialog dialog = new TaskOperationDialog();
        dialog.setArguments(arg);
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        binding = DialogTaksOperationBinding.bind(view);
        binding.btDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                TaskDeleteConfirmDialog dialog = TaskDeleteConfirmDialog.newInstance();
                dialog.setOnDeleteListener(new TaskDeleteConfirmDialog.OnDeleteListener() {
                    @Override
                    public void onDelete(boolean deleteFile) {
                        if (operationListener != null) {
                            operationListener.onDelete(deleteFile);
                        }
                    }
                });
                dialog.show(getParentFragmentManager(), "delete_confirm");
                getDialog().dismiss();
            }
        });
        binding.btCopyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (operationListener != null) {
                    operationListener.onCopyLink();
                }
                getDialog().dismiss();
            }
        });
        binding.btRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (operationListener != null) {
                    operationListener.onRestart();
                }
                getDialog().dismiss();
            }
        });
        binding.btOpenDir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (operationListener != null) {
                    operationListener.onOpenDirClick();
                }
                getDialog().dismiss();
            }
        });
        binding.btLookupQrcode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (operationListener != null) {
                    operationListener.onLookupQRCodeClick();
                }
                getDialog().dismiss();
            }
        });
        binding.btCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().cancel();
            }
        });
    }

    public TaskOperationDialog setOperationListener(OperationListener operationListener) {
        this.operationListener = operationListener;
        return this;
    }
}
