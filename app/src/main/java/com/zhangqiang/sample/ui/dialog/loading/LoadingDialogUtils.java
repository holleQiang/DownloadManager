package com.zhangqiang.sample.ui.dialog.loading;

public class LoadingDialogUtils {

    public static void showLoading(LoadingDialogHolderOwner owner){
        owner.getLoadingDialogHolder().showLoading();
    }

    public static void hideLoading(LoadingDialogHolderOwner owner){
        owner.getLoadingDialogHolder().hideLoading();
    }
}
