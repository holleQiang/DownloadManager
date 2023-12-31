package com.zhangqiang.sample.base.permission;

import androidx.fragment.app.Fragment;

public class FragmentPermissionHelper extends PermissionHelper{

    private final Fragment fragment;

    public FragmentPermissionHelper(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public void requestPermissions(String[] permissions, int requestCode) {
        fragment.requestPermissions(permissions, requestCode);
    }
}
