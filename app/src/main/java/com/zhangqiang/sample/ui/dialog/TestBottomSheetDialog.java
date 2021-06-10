package com.zhangqiang.sample.ui.dialog;

import android.view.View;
import android.view.Window;

import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseDialogFragment;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-04
 */
public class TestBottomSheetDialog extends BaseDialogFragment {

    @Override
    protected boolean useBottomSheet() {
        return true;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_test_sheet;
    }

    @Override
    protected void initView(View view) {

    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        View decorView = window.getDecorView();
        int systemUiVisibility = decorView.getSystemUiVisibility();
        decorView.setSystemUiVisibility(systemUiVisibility | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }
}
