package com.zhangqiang.downloadmanager.schedule;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class IntervalTask implements Runnable {

    private final long interval;
    private final AtomicBoolean alive = new AtomicBoolean(false);

    public IntervalTask(long interval) {
        this.interval = interval;
    }

    public long getInterval() {
        return interval;
    }

    public boolean isAlive() {
        return alive.get();
    }

    void setAlive(boolean alive) {
        this.alive.set(alive);
    }
}
