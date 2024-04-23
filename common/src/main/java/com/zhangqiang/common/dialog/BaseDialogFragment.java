package com.zhangqiang.common.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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
        return useBottomSheet() ? new BottomSheetDialog(getActivity(), getTheme()) : new Dialog(getActivity(), getTheme());
    }

    protected boolean useBottomSheet() {
        return false;
    }

    protected float getHeightRatio() {
        return -1;
    }

    @Nullable
    @Override
    public final View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = getLayoutView(inflater, container, savedInstanceState);
        float heightRatio = getHeightRatio();
        if (heightRatio != -1) {
            if (useBottomSheet()) {
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getResources().getDisplayMetrics().heightPixels));
                BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
                if (dialog != null) {
                    BottomSheetBehavior<FrameLayout> behavior = dialog.getBehavior();
                    behavior.setFitToContents(false);
                    behavior.setHalfExpandedRatio(heightRatio);
                    behavior.setSkipCollapsed(true);
                }
            } else {
                int height = (int) (getResources().getDisplayMetrics().heightPixels * heightRatio);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
            }
        }
        initView(view);
        return view;
    }

    protected View getLayoutView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(getLayoutResId(), container, false);
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
