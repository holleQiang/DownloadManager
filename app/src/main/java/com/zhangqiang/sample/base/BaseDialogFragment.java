package com.zhangqiang.sample.base;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        return useBottomSheet() ? new BottomSheetDialog(getActivity(),getTheme()) : new Dialog(getActivity(),getTheme());
//        return useBottomSheet() ? new BottomSheetDialog(getActivity(),getTheme()) : super.onCreateDialog(savedInstanceState);
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
