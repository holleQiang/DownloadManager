package com.zhangqiang.downloadmanager.plugin.http.bean;

import androidx.annotation.IntDef;

import java.util.Date;

public class HttpDefaultTaskBean {

    public static final int STATE_IDLE = 0;
    public static final int STATE_WRITING_TO_FILE = 1;
    public static final int STATE_FAIL = 2;
    public static final int STATE_CANCEL = 3;
    public static final int STATE_SUCCESS = 4;

    @IntDef(value = {STATE_IDLE,STATE_CANCEL, STATE_WRITING_TO_FILE, STATE_FAIL, STATE_SUCCESS})
    @interface State {
    }

    private String id;
    private long currentLength;
    @State
    private int state;
    private String errorMsg;
    private long createTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public long getCreateTime() {
        return createTime;
    }

    public HttpDefaultTaskBean setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }
}
