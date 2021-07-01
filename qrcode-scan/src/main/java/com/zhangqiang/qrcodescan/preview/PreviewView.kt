package com.zhangqiang.qrcodescan.preview

import android.graphics.Rect

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-21
 */
interface PreviewView {

    val previewWidth:Int
    val previewHeight:Int
    fun getScanRegion(rect: Rect)
}