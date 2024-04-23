package com.zhangqiang.web.history.dialog;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhangqiang.common.dialog.BaseDialogFragment;
import com.zhangqiang.web.history.bean.VisitRecordBean;
import com.zhangqiang.web.history.cell.OnVisitRecordClickListener;
import com.zhangqiang.web.history.fragment.VisitRecordFragment;
import com.zhangqiang.web.manager.OpenOptions;
import com.zhangqiang.web.manager.WebManager;
import com.zhangqiang.webview.R;
import com.zhangqiang.webview.databinding.DialogVisitRecordBinding;

public class VisitRecordDialog extends BaseDialogFragment {

    private static final String INTENT_KEY_SESSION_ID = "session_id";

    public static VisitRecordDialog newInstance(String sessionId) {
        Bundle bundle = new Bundle();
        bundle.putString(INTENT_KEY_SESSION_ID, sessionId);
        VisitRecordDialog dialog = new VisitRecordDialog();
        dialog.setArguments(bundle);
        return dialog;
    }

    private String sessionId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Bundle arguments = getArguments();
            if (arguments != null) {
                sessionId = arguments.getString(INTENT_KEY_SESSION_ID);
            }
        } else {
            sessionId = savedInstanceState.getString(INTENT_KEY_SESSION_ID);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INTENT_KEY_SESSION_ID, sessionId);
    }


    @Override
    protected View getLayoutView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        DialogVisitRecordBinding binding = DialogVisitRecordBinding.inflate(inflater, container, false);
        binding.mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        VisitRecordFragment fragment = VisitRecordFragment.newInstance(sessionId);
        fragment.setOnVisitRecordClickListener(new OnVisitRecordClickListener() {
            @Override
            public void onVisitRecordClick(VisitRecordBean visitRecord, int position) {
                WebManager.getInstance().openWebViewActivity(getActivity(), visitRecord.getUrl(), new OpenOptions.Builder().setSessionId(sessionId).build());
                dismiss();
            }
        });
        getChildFragmentManager().beginTransaction().replace(R.id.fl_fragment_container, fragment).commitAllowingStateLoss();
        return binding.getRoot();
    }

    @Override
    protected int getLayoutResId() {
        return 0;
    }

    @Override
    protected void initView(View view) {

    }

    @Override
    protected float getHeightRatio() {
        return 0.75f;
    }

    @Override
    protected boolean useBottomSheet() {
        return true;
    }
}
