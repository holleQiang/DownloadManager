package com.zhangqiang.web.manager;

public class OpenOptions {

    private final boolean newTask;
    private final String sessionId;

    private OpenOptions(boolean newTask, String sessionId) {
        this.newTask = newTask;
        this.sessionId = sessionId;
    }

    public boolean isNewTask() {
        return newTask;
    }

    public String getSessionId() {
        return sessionId;
    }

    public static class Builder {

        private boolean newTask;
        private String sessionId;

        public Builder() {
        }

        public boolean isNewTask() {
            return newTask;
        }

        public Builder setNewTask(boolean newTask) {
            this.newTask = newTask;
            return this;
        }

        public Builder setSessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public OpenOptions build() {
            return new OpenOptions(newTask, sessionId);
        }
    }
}
