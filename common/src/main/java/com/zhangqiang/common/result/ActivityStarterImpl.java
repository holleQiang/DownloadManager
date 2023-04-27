package com.zhangqiang.common.result;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;


public class ActivityStarterImpl implements ActivityStarter{

    private final Activity activity;
    private int requestCode = 7788;
    private final SparseArray<ActivityResultCallback> requestCodeCallbacks = new SparseArray<>();

    public ActivityStarterImpl(Activity activity) {
        this.activity = activity;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data){
        requestCodeCallbacks.get(requestCode).onActivityResult(resultCode,data);
    }

    @Override
    public void startActivityForResult(Intent intent, ActivityResultCallback callback) {
        int requestCode = this.requestCode++;
        activity.startActivityForResult(intent, requestCode);
        requestCodeCallbacks.put(requestCode,callback);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, ActivityResultCallback callback) {
        activity.startActivityForResult(intent, requestCode);
        requestCodeCallbacks.put(requestCode,callback);
    }
}
