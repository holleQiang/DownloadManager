package com.zhangqiang.common.dialog.loading;

import android.app.ProgressDialog;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;

public class LoadingDialogHolderImpl implements LoadingDialogHolder{

    private ProgressDialog dialog;
    private final AppCompatActivity activity;
    private boolean dialogShowPending = false;

    public LoadingDialogHolderImpl(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public void showLoading() {
        if (dialog == null) {
            dialog = new ProgressDialog(activity);
            dialog.setMessage("Loading...");
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
        }
        if (activity.getLifecycle().getCurrentState() == Lifecycle.State.RESUMED) {
            dialog.show();
        }else {
            dialogShowPending = true;
        }
    }

    @Override
    public void hideLoading() {
        if (dialog!= null) {
            dialog.dismiss();
            dialog = null;
        }
        dialogShowPending = false;
    }

    public void dispatchActivityResume(){
        if(dialogShowPending ){
            if (dialog != null) {
                dialog.show();
            }
            dialogShowPending = false;
        }
    }
}
