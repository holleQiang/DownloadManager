package com.zhangqiang.downloadmanager.speed;

public class SpeedRecord {

    long speed;
    long lastComputeTime;
    long lastLength;

    @Override
    public String toString() {
        return "SpeedRecord{" +
                "speed=" + speed +
                ", lastComputeTime=" + lastComputeTime +
                ", lastLength=" + lastLength +
                '}';
    }
}
