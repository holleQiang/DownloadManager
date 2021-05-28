package com.zhangqiang.sample.ui.dialog;

import android.view.View;
import android.view.WindowManager;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-05-27
 */
public class TestDialog extends BaseDialogFragment {

    @Override
    protected boolean useBottomSheet() {
        return false;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_test;
    }

    @Override
    protected void initView(View view) {
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//        new CreateTaskDialog().show(getChildFragmentManager(),"ssss");
    }
}
