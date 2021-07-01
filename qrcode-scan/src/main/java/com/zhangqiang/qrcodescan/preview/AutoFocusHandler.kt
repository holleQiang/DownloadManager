package com.zhangqiang.qrcodescan.preview

import android.content.Context
import android.graphics.Camera
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

/**
 * description :
 * creator : zhangqing.jason@bytedance.com
 * date : 2021-06-25
 */
class AutoFocusHandler(val mCamera: Camera, private val mContext: Context) {
    var mSensor: Sensor? = null
    var mSensorManager: SensorManager? = null

    init {
        mSensorManager = mContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mSensor = mSensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    fun start() {

        mSensorManager?.registerListener(mSensorEventListener,mSensor,SensorManager.SENSOR_DELAY_NORMAL)
    }

    fun stop() {
        mSensorManager?.unregisterListener(mSensorEventListener)
    }

    private val mSensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }
}