package com.zhangqiang.qrcodescan.decode

import android.graphics.Rect
import android.hardware.Camera
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import com.google.zxing.BinaryBitmap
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.zhangqiang.qrcodescan.preview.PreviewView

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-21
 */
class DecodeThread(
    private val mCamera: Camera,
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
    private var mCaptureHandler: Handler? = null
    private var mRunning: Boolean = false
    private val mTmpRect: Rect = Rect()

    fun start() {

        if (mRunning) {
            return
        }
        mRunning = true
        mCaptureHandler = Handler(Looper.getMainLooper(), mHandlerCallback)
        val decodeHandlerThread = HandlerThread("qr_decode_thread")
        decodeHandlerThread.start()
        mDecodeHandler = Handler(decodeHandlerThread.looper, mHandlerCallback)
        mCaptureHandler?.sendEmptyMessage(MSG_CAPTURE)
    }

    fun stop() {
        if (!mRunning) {
            return
        }
        mRunning = false
        mCaptureHandler?.removeCallbacksAndMessages(null)
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
                mCamera.setOneShotPreviewCallback { data, camera ->
                    val msg = mDecodeHandler?.obtainMessage(MSG_DECODE)
                    msg?.apply {
                        if (obj == null) {
                            obj = PreviewData(
                                data,
                                camera?.parameters?.previewSize?.width ?: 0,
                                camera?.parameters?.previewSize?.height ?: 0
                            )
                        } else {
                            val previewData = obj as PreviewData
                            previewData.data = data
                            previewData.width = camera?.parameters?.previewSize?.height ?: 0
                            previewData.height = camera?.parameters?.previewSize?.width ?: 0
                        }
                        mDecodeHandler?.sendMessage(this)
                    }
                }
            }
            MSG_DECODE -> {
                previewView.getScanRegion(mTmpRect)
                val previewData = it.obj as PreviewData
                val left =
                    previewData.width.toFloat() / previewView.previewWidth * mTmpRect.left
                val top =
                    previewData.height.toFloat() / previewView.previewHeight * mTmpRect.top
                val right =
                    previewData.width.toFloat() / previewView.previewWidth * mTmpRect.right
                val bottom =
                    previewData.height.toFloat() / previewView.previewHeight * mTmpRect.bottom
                val source = buildLuminanceSource(
                    previewData.data,
                    previewData.width,
                    previewData.height,
                    Rect(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
                )
                try {
                    val result = QRCodeReader().decode(BinaryBitmap(HybridBinarizer(source)))
                    val text = result.text
                    Log.i(TAG, "=========$text")
                    if (text.isNotEmpty()) {
                        if (callback.onDecodeSuccess(text)) {
                            stop()
                            return@Callback true
                        }
                    }
                } catch (e: Throwable) {
                }
                mCaptureHandler?.sendEmptyMessage(MSG_CAPTURE)
            }
            else -> {
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

class PreviewData(
    var data: ByteArray?,
    var width: Int,
    var height: Int
)

interface Callback {
    /**
     * return true to stop decode
     */
    fun onDecodeSuccess(text: String): Boolean
}