package com.zhangqiang.downloadmanager2.plugin.http.task.schedule;

public abstract class IntervalTask implements Runnable {

    private final long interval;
    private volatile boolean alive;

    public IntervalTask(long interval) {
        this.interval = interval;
    }

    public long getInterval() {
        return interval;
    }

    public boolean isAlive() {
        return alive;
    }

    IntervalTask setAlive(boolean alive) {
        this.alive = alive;
        return this;
    }
}
