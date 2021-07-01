package com.zhangqiang.sample.business.settings;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.zhangqiang.qrcodescan.widget.LimitAdapter;
import com.zhangqiang.qrcodescan.widget.LimitHorizontalLayout;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.base.BaseActivity;
import com.zhangqiang.sample.databinding.ActivitySettingsBinding;
import com.zhangqiang.sample.impl.BaseObserver;
import com.zhangqiang.sample.manager.SettingsManager;
import com.zhangqiang.sample.utils.RxJavaUtils;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setSupportActionBar(mBinding.mToolBar);

        SettingsManager.getInstance().getMaxRunningTaskCountOption().toObservable()
                .compose(RxJavaUtils.<Integer>bindLifecycle(this))
                .subscribe(new BaseObserver<Integer>() {
                    @Override
                    public void onNext(Integer integer) {
                        mBinding.etMaxRunningTaskSize.setText(String.valueOf(integer));
                    }
                });
        SettingsManager.getInstance().getSaveDirOption().toObservable()
                .compose(RxJavaUtils.<String>bindLifecycle(this))
                .subscribe(new BaseObserver<String>() {
                    @Override
                    public void onNext(String s) {
                        mBinding.etSaveDir.setText(s);
                    }
                });
        mBinding.dhlLayout.setAdapter(new LimitAdapter() {
            @Override
            public int getCount() {
                return 10;
            }

            @Override
            public int getViewType(int position) {
                return position % 2;
            }

            @NotNull
            @Override
            public View getView(@NotNull ViewGroup container, int position, @Nullable View cacheView) {
                if (getViewType(position) == 0) {
                    TextView textView = new TextView(container.getContext());
                    textView.setTextSize(15);
                    textView.setBackgroundColor((int) (Math.random() * 0xFFFFFFFF));
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 5; i++) {
                        sb.append(position);
                    }
                    textView.setText(sb.toString());
                    return textView;
                } else {

                    ImageView imageView = new ImageView(container.getContext());
                    imageView.setImageResource(R.mipmap.ic_launcher);
                    LimitHorizontalLayout.LayoutParams layoutParams = new LimitHorizontalLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LimitHorizontalLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.leftMargin = 50;
                    imageView.setLayoutParams(layoutParams);
                    return imageView;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.bt_confirm) {
            String s = mBinding.etMaxRunningTaskSize.getText().toString();
            if (!TextUtils.isEmpty(s)) {
                SettingsManager.getInstance().getMaxRunningTaskCountOption().set(Integer.valueOf(s));
            }
            String saveDir = mBinding.etSaveDir.getText().toString();
            if (isValidDir(saveDir)) {
                SettingsManager.getInstance().getSaveDirOption().set(saveDir);
            }
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isValidDir(String saveDir) {
        Pattern pattern = Pattern.compile("[a-zA-Z0-9/]+");
        return pattern.matcher(saveDir).matches();
    }
}
