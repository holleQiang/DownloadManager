package com.zhangqiang.qrcodescan

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.hardware.Camera
import android.os.Build
import android.os.Bundle
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.cameraview.CameraView
import com.zhangqiang.qrcodescan.databinding.QrcodeActivityScanBinding
import com.zhangqiang.qrcodescan.decode.Callback
import com.zhangqiang.qrcodescan.decode.DecodeThread
import com.zhangqiang.qrcodescan.preview.PreviewUtils
import com.zhangqiang.qrcodescan.preview.PreviewView

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-17
 */
class QRCodeScanActivity : AppCompatActivity(), PreviewView {

    companion object {
        const val REQUEST_CODE_CAMERA = 1000
        const val TAG = "QRCodeScanActivity"
    }

    private lateinit var mViewBinding: QrcodeActivityScanBinding
    private var mDecodeThread: DecodeThread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewBinding = QrcodeActivityScanBinding.inflate(layoutInflater)
        setContentView(mViewBinding.root)
        mViewBinding.cameraView.addCallback(mCallback)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT
            }
            window.decorView.apply {
                systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    .or(View.SYSTEM_UI_FLAG_LAYOUT_STABLE)
                    .or(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
            }
        }
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_CAMERA
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_CAMERA
            && permissions.size == 1 && permissions[0] == Manifest.permission.CAMERA
            && grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            mViewBinding.cameraView.start()
        }
    }

    override fun onResume() {
        super.onResume()
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            start()
        }
    }

    override fun onPause() {
        super.onPause()
        stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mDecodeThread?.destroy()
    }

    private fun start() {
        mViewBinding.cameraView.start()
    }

    private fun stop() {
        mViewBinding.cameraView.stop()
    }

    private val mCallback = object : CameraView.Callback() {
        override fun onCameraOpened(cameraView: CameraView?) {
            super.onCameraOpened(cameraView)
            mDecodeThread?.start()
        }

        override fun onCameraClosed(cameraView: CameraView?) {
            super.onCameraClosed(cameraView)
            mDecodeThread?.stop()
        }
    }

    override val previewWidth: Int
        get() = mViewBinding.cameraView.width
    override val previewHeight: Int
        get() = mViewBinding.cameraView.height

    override fun getScanRegion(rect: Rect) {
        rect.set(
            mViewBinding.scanView.left,
            mViewBinding.scanView.top,
            mViewBinding.scanView.right,
            mViewBinding.scanView.bottom
        )
    }
}