package com.zhangqiang.sample.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zhangqiang.sample.R;
import com.zhangqiang.visiblehelper.FragmentVisibleHelper;
import com.zhangqiang.visiblehelper.VisibleHelper;
import com.zhangqiang.visiblehelper.VisibleHelperOwner;

public abstract class BaseDialogFragment extends DialogFragment implements VisibleHelperOwner {

    private final FragmentVisibleHelper visibleHelper = new FragmentVisibleHelper(this);

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Context context = getActivity();
        if (context == null) {
            return super.onCreateDialog(savedInstanceState);
        }
//        return useBottomSheet() ? new BottomSheetDialog(getActivity(),getTheme()) : new AppCompatDialog(getActivity(),getTheme());
        return useBottomSheet() ? new BottomSheetDialog(getActivity(),getTheme()) : new AppCompatDialog(getActivity(),getTheme());
    }

    protected boolean useBottomSheet() {
        return false;
    }

    @Nullable
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);
        initView(view);
        return view;
    }

    protected abstract int getLayoutResId();

    protected abstract void initView(View view);

    @NonNull
    @Override
    public VisibleHelper getVisibleHelper() {
        return visibleHelper;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        visibleHelper.setUserVisibleHint();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        visibleHelper.onHiddenChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        visibleHelper.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        visibleHelper.onStop();
    }
}
