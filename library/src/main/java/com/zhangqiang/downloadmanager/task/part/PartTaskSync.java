package com.zhangqiang.downloadmanager.task.part;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public abstract class PartTaskSync {

    private List<PartTask> partTasks;
    private CountDownLatch countDownLatch;


    public PartTaskSync(List<PartTask> partTasks) {
        this.partTasks = partTasks;
    }

    public void start() throws InterruptedException {

        int runningCount = 0;
        for (int i = 0; i < partTasks.size(); i++) {
            PartTask partTask = partTasks.get(i);
            int status = partTask.getStatus();
            if (status == PartTask.STATUS_DOWNLOADING) {
                throw new IllegalArgumentException("error status : downloading");
            }
            if (status == PartTask.STATUS_COMPLETE) {
                continue;
            }
            partTask.setCallback(new CallbackImpl(partTask));
            partTask.start();
            runningCount++;
        }
        if (runningCount > 0) {

            countDownLatch = new CountDownLatch(runningCount);
            countDownLatch.await();
        }
    }

    protected abstract void onProgress(long current);

    protected abstract void onFail(Throwable e);

    protected abstract void onComplete() throws IOException;

    class CallbackImpl implements Callback {

        private PartTask partTask;

        CallbackImpl(PartTask partTask) {
            this.partTask = partTask;
        }

        @Override
        public void onStart() {

        }

        @Override
        public void onProgress(long current, long start, long end) {
            long totalProgress = 0;
            for (int i = 0; i < partTasks.size(); i++) {
                PartTask partTask = partTasks.get(i);
                totalProgress += partTask.getCurrent();
            }
            PartTaskSync.this.onProgress(totalProgress);
        }

        @Override
        public void onComplete() {
            if (countDownLatch != null) {
                countDownLatch.countDown();
                if (countDownLatch.getCount() == 0) {
                    try {
                        PartTaskSync.this.onComplete();
                    } catch (IOException e) {
                        e.printStackTrace();
                        PartTaskSync.this.onFail(e);
                    }
                }
            }
        }

        @Override
        public void onFail(Throwable e) {
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
            for (int i = 0; i < partTasks.size(); i++) {
                PartTask partTask = partTasks.get(i);
                if (partTask != this.partTask) {
                    partTask.pause();
                }
            }
            PartTaskSync.this.onFail(e);
        }

        @Override
        public void onPause() {
            if (countDownLatch != null) {
                countDownLatch.countDown();
            }
        }
    }

}
