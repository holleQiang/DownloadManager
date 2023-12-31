package com.zhangqiang.sample.base;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.zhangqiang.sample.base.permission.FragmentPermissionHelper;
import com.zhangqiang.sample.base.permission.PermissionHelper;
import com.zhangqiang.sample.base.permission.PermissionHelperOwner;
import com.zhangqiang.visiblehelper.FragmentVisibleHelper;
import com.zhangqiang.visiblehelper.VisibleHelper;
import com.zhangqiang.visiblehelper.VisibleHelperOwner;

public class BaseFragment extends Fragment implements VisibleHelperOwner, PermissionHelperOwner {

    private final FragmentVisibleHelper visibleHelper = new FragmentVisibleHelper(this);
    private final PermissionHelper permissionHelper = new FragmentPermissionHelper(this);

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

    @Override
    public PermissionHelper getPermissionHelper() {
        return permissionHelper;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getPermissionHelper().onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
