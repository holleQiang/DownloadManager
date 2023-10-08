package com.zhangqiang.downloadmanager.task.speed;

import android.os.SystemClock;

import com.zhangqiang.base.CurrentLengthOwner;

import java.util.ArrayList;
import java.util.List;

public class SpeedHelper {

    private final SpeedRecord record = new SpeedRecord();
    private final CurrentLengthOwner currentLengthOwner;
    private final List<OnSpeedChangeListener> onSpeedChangeListeners = new ArrayList<>();

    public SpeedHelper(CurrentLengthOwner currentLengthOwner) {
        this.currentLengthOwner = currentLengthOwner;
    }

    public void calculateSpeed(){
        long currentLength = currentLengthOwner.getCurrentLength();
        if (record.lastComputeTime == 0) {
            record.lastComputeTime = SystemClock.elapsedRealtime();
            record.lastLength = currentLength;
            return;
        }
        long currentTime = SystemClock.elapsedRealtime();
        long deltaTime = currentTime - record.lastComputeTime;
        if (deltaTime > 0) {
            long deltaLength = currentLength - record.lastLength;
            long newSpeed = deltaLength * 1000 / deltaTime;
            boolean changed = record.speed != newSpeed;
            if (changed) {
                record.speed = newSpeed;
            }
            record.lastComputeTime = currentTime;
            record.lastLength = currentLength;
            if(changed){
                dispatchSpeedChange();
            }
        }
    }

    public void addOnSpeedChangeListener(OnSpeedChangeListener listener){
        onSpeedChangeListeners.add(listener);
    }

    public void removeOnSpeedChangeListener(OnSpeedChangeListener listener){
        onSpeedChangeListeners.remove(listener);
    }

    private void dispatchSpeedChange(){
        for (int i = onSpeedChangeListeners.size() - 1; i >= 0; i--) {
            onSpeedChangeListeners.get(i).onSpeedChange();
        }
    }

    public long getSpeed(){
        return  record.speed;
    }
}
