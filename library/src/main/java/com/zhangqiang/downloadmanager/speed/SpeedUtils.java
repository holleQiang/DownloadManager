package com.zhangqiang.downloadmanager.speed;

import com.zhangqiang.downloadmanager.task.CurrentLengthOwner;
import com.zhangqiang.downloadmanager.utils.LogUtils;

public class SpeedUtils {

    private static final String TAG = SpeedUtils.class.getSimpleName();

    public static boolean computeSpeed(CurrentLengthOwner speedSupport) {

//        LogUtils.i(TAG,"computeSpeed=========" + speedSupport);
//        SpeedRecord record = speedSupport.getSpeedRecord();
//        long currentLength = speedSupport.getCurrentLength();
//        if (record.lastComputeTime == 0) {
//            record.lastComputeTime = SystemClock.elapsedRealtime();
//            record.lastLength = currentLength;
//            return false;
//        }
//        long currentTime = SystemClock.elapsedRealtime();
//        long deltaTime = currentTime - record.lastComputeTime;
//        if (deltaTime > 0) {
//            long deltaLength = currentLength - record.lastLength;
//            long newSpeed = deltaLength * 1000 / deltaTime;
//            boolean changed = record.speed != newSpeed;
//            if (changed) {
//                record.speed = newSpeed;
//            }
//            record.lastComputeTime = currentTime;
//            record.lastLength = currentLength;
//            return changed;
//        }
        return false;
    }

    public static long getSpeed(SpeedSupport speedSupport) {
        return 0;
//        return speedSupport.getSpeedRecord().speed;
    }

    public static void resetStatus(SpeedSupport speedSupport) {
        LogUtils.i(TAG, "=======resetStatus=======");
//        SpeedRecord speedRecord = speedSupport.getSpeedRecord();
//        speedRecord.lastLength = speedSupport.getCurrentLength();
//        speedRecord.lastComputeTime = SystemClock.elapsedRealtime();
//        speedRecord.speed = 0;
    }
}
