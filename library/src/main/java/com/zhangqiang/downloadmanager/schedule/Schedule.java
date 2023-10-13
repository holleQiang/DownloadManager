package com.zhangqiang.downloadmanager.schedule;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

public class Schedule {

    private final Handler handler;

    private static final Schedule instance = new Schedule();

    private Schedule() {
        HandlerThread handlerThread = new HandlerThread("schedule");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {

                IntervalTask intervalTask = ((IntervalTask) msg.obj);
                intervalTask.run();

                if (intervalTask.isAlive()) {
                    Message message = handler.obtainMessage(0);
                    message.obj = intervalTask;
                    handler.sendMessageDelayed(message, intervalTask.getInterval());
                }
                return true;
            }
        });
    }

    public static Schedule getInstance() {
        return instance;
    }

    public void startSchedule(IntervalTask task) {
        task.setAlive(true);
        Message message = handler.obtainMessage(0);
        message.obj = task;
        handler.sendMessage(message);
    }

    public void stopSchedule(IntervalTask task) {
        task.setAlive(false);
        handler.removeMessages(0, task);
    }
}
