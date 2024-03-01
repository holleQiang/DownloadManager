package com.zhangqiang.web.history.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.zhangqiang.common.activity.BaseActivity;
import com.zhangqiang.web.history.fragment.VisitRecordFragment;
import com.zhangqiang.webview.R;
import com.zhangqiang.webview.databinding.ActivityVisitRecordBinding;

public class VisitRecordActivity extends BaseActivity {

    private static final String INTENT_KEY_SESSION_ID = "session_id";

    public static Intent newIntent(Context context, String sessionId) {
        Intent intent = new Intent(context, VisitRecordActivity.class);
        intent.putExtra(INTENT_KEY_SESSION_ID, sessionId);
        return intent;
    }

    private String sessionId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            sessionId = getIntent().getStringExtra(INTENT_KEY_SESSION_ID);
        } else {
            sessionId = savedInstanceState.getString(INTENT_KEY_SESSION_ID);
        }

        ActivityVisitRecordBinding binding = ActivityVisitRecordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.mToolBar);
        binding.mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fl_fragment_container, VisitRecordFragment.newInstance(sessionId))
                .commitAllowingStateLoss();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(INTENT_KEY_SESSION_ID, sessionId);
    }
}
