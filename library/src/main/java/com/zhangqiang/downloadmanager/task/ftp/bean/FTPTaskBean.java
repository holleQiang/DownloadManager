package com.zhangqiang.downloadmanager.task.ftp.bean;

public class FTPTaskBean {
    public static final int STATE_IDLE = 0;
    public static final int STATE_DOWNLOADING = 1;
    public static final int STATE_SUCCESS = 2;
    public static final int STATE_FAIL = 3;
    public static final int STATE_CANCEL = 4;
    private int state = STATE_IDLE;
    private String errorMsg;

    public int getState() {
        return state;
    }

    public FTPTaskBean setState(int state) {
        this.state = state;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public FTPTaskBean setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }
}
