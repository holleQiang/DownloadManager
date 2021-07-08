package com.zhangqiang.qrcodescan.decode

import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.google.android.cameraview.BaseCamera
import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.zhangqiang.qrcodescan.preview.PreviewView


/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-21
 */
class DecodeThread(
    private val mCamera: BaseCamera,
    private val previewView: PreviewView,
    private val callback: Callback
) {

    companion object {
        const val TAG = "DecodeThread"
        const val MSG_CAPTURE = 0
        const val MSG_DECODE = 1
        const val MSG_AUTO_FOCUS = 2
    }

    private var mDecodeHandler: Handler? = null
    private var mRunning: Boolean = false
    private val mTmpRect: Rect = Rect()
    private val multiReader = MultiFormatReader()

    init {
        val hints = mutableMapOf<DecodeHintType, Any>()
        val mutableListOf = mutableListOf<BarcodeFormat>()
        mutableListOf.add(BarcodeFormat.QR_CODE)
        hints[DecodeHintType.POSSIBLE_FORMATS] = mutableListOf
        multiReader.setHints(hints)
    }

    fun start() {

        if (mRunning) {
            return
        }
        mRunning = true
        val decodeHandlerThread = HandlerThread("qr_decode_thread")
        decodeHandlerThread.start()
        mDecodeHandler = Handler(decodeHandlerThread.looper, mHandlerCallback)
        mDecodeHandler?.sendEmptyMessage(MSG_CAPTURE)
    }

    fun stop() {
        if (!mRunning) {
            return
        }
        mRunning = false
        mDecodeHandler?.removeCallbacksAndMessages(null)
    }

    fun destroy() {
        stop()
        mDecodeHandler?.looper?.quit()
    }

    private val mHandlerCallback = Handler.Callback {
        if (!mRunning) {
            return@Callback false
        }
        when (it.what) {
            MSG_CAPTURE -> {
                mCamera.takePicture { data, width, height ->

                    previewView.getScanRegion(mTmpRect)

//                val left =
//                    previewData.width.toFloat() / previewView.previewWidth * mTmpRect.left
//                val top =
//                    previewData.height.toFloat() / previewView.previewHeight * mTmpRect.top
//                val right =
//                    previewData.width.toFloat() / previewView.previewWidth * mTmpRect.right
//                val bottom =
//                    previewData.height.toFloat() / previewView.previewHeight * mTmpRect.bottom
//                val source = buildLuminanceSource(
//                    previewData.data,
//                    previewData.width,
//                    previewData.height,
//                    Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
//                )
                    val source = buildLuminanceSource(
                        data,
                        width,
                        height,
                        Rect(0, 0, width, height)
                    )
                    try {
                        val result =
                            multiReader.decodeWithState(BinaryBitmap(HybridBinarizer(source)))
                        val text = result.text
                        Log.i(TAG, "=========$text")
                        if (text.isNotEmpty()) {
                            if (callback.onDecodeSuccess(text)) {
                                stop()
                                return@takePicture
                            }
                        }
                    } catch (e: Throwable) {
                        e.printStackTrace()
                    }
                    mDecodeHandler?.sendEmptyMessage(MSG_CAPTURE)
                }
            }
        }
        true
    }

    private fun buildLuminanceSource(
        data: ByteArray?,
        width: Int,
        height: Int,
        decodeRegion: Rect
    ): PlanarYUVLuminanceSource {
        // Go ahead and assume it's YUV rather than die.
        return PlanarYUVLuminanceSource(
            data, width, height, decodeRegion.left, decodeRegion.top,
            decodeRegion.width(), decodeRegion.height(), false
        )
    }
}


interface Callback {
    /**
     * return true to stop decode
     */
    fun onDecodeSuccess(text: String): Boolean

}