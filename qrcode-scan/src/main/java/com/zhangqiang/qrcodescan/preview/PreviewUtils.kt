package com.zhangqiang.qrcodescan.preview

import android.graphics.Point
import android.hardware.Camera
import android.util.Size
import java.util.*
import kotlin.math.abs

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-30
 */
object PreviewUtils {

    fun findBestPreviewSize(camera: Camera, previewWidth: Int, previewHeight: Int): Size {

        val previewRatio = previewWidth.toFloat() / previewHeight
        val supportedPreviewSizes = camera.parameters.supportedPreviewSizes
        supportedPreviewSizes.sortWith(Comparator { o1, o2 ->
            o1?.let {
                o2?.let {
                    o1.width * o1.height - o2.width * o2.height
                } ?: 0
            } ?: 0
        })
        var minCloseSize: Camera.Size? = null
        var minDeltaRatio: Float = Float.MAX_VALUE
        for (size in supportedPreviewSizes) {
//            if (size.width * size.height < previewWidth * previewHeight) {
//                continue
//            }
            val ratio = size.width.toFloat() / size.height
            val deltaRatio = abs(ratio - previewRatio)
            if (deltaRatio <= minDeltaRatio) {
                minDeltaRatio = deltaRatio
                minCloseSize = size
            }
        }
        val targetSize = minCloseSize ?: supportedPreviewSizes.last()
        return Size(targetSize.width, targetSize.height)
    }

    data class Size(val width: Int, val height: Int)
}