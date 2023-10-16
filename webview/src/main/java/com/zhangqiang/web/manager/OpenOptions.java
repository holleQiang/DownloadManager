package com.zhangqiang.web.manager;

public class OpenOptions {

    private final boolean newTask;

    private OpenOptions(boolean newTask) {
        this.newTask = newTask;
    }

    public boolean isNewTask() {
        return newTask;
    }

    public static class Builder{

        private boolean newTask;

        public Builder() {
        }

        public boolean isNewTask() {
            return newTask;
        }

        public Builder setNewTask(boolean newTask) {
            this.newTask = newTask;
            return this;
        }

        public OpenOptions build(){
            return new OpenOptions(newTask);
        }
    }
}
