package com.zhangqiang.sample.ui.dialog;

import android.view.View;
import android.view.Window;
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
    protected int getLayoutResId() {
        return R.layout.dialog_test;
    }

    @Override
    protected void initView(View view) {
    }

    @Override
    public int getTheme() {
        return super.getTheme();
    }

    @Override
    public void onStart() {
        super.onStart();
        Window window = getDialog().getWindow();
        View decorView = window.getDecorView();
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        int systemUiVisibility = decorView.getSystemUiVisibility();
        decorView.setSystemUiVisibility(systemUiVisibility
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
//        new CreateTaskDialog().show(getChildFragmentManager(),"ssss");
    }
}
