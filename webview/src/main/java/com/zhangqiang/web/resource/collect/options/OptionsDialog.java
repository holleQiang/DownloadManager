package com.zhangqiang.web.resource.collect.options;

import android.view.View;

import com.zhangqiang.common.dialog.BaseDialogFragment;
import com.zhangqiang.webview.R;

public class OptionsDialog extends BaseDialogFragment {

    private OnOptionClickListener onOptionClickListener;


    @Override
    protected int getLayoutResId() {
        return R.layout.dialog_options;
    }

    @Override
    protected void initView(View view) {
        view.findViewById(R.id.bt_copy_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onOptionClickListener != null) {
                    onOptionClickListener.onOptionClick(Option.COPY_LINK);
                }
                dismiss();
            }
        });
    }

    public OptionsDialog setOnOptionClickListener(OnOptionClickListener onOptionClickListener) {
        this.onOptionClickListener = onOptionClickListener;
        return this;
    }
}
