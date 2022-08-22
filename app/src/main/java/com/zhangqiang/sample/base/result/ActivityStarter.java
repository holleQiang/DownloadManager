package com.zhangqiang.sample.base.result;

import android.content.Intent;

public interface ActivityStarter {

    void startActivityForResult(Intent intent,ActivityResultCallback callback);

    void startActivityForResult(Intent intent,int requestCode,ActivityResultCallback callback);

    interface ActivityResultCallback{
        void onActivityResult(int resultCode, Intent data);
    }
}
