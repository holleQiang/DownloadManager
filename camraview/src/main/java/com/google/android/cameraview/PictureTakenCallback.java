package com.google.android.cameraview;

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-07-07
 */
public interface PictureTakenCallback {

    void onPictureTaken(byte[] data, int width, int height);
}
