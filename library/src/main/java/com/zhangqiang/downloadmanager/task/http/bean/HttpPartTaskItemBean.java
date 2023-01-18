package com.zhangqiang.downloadmanager.task.http.bean;

import androidx.annotation.IntDef;

import java.util.Date;

public class HttpPartTaskItemBean {

    public static final int STATE_IDLE = 0;
    public static final int STATE_GENERATING_INFO = 1;
    public static final int STATE_WRING_TO_FILE = 2;
    public static final int STATE_FAIL = 3;
    public static final int STATE_SUCCESS = 4;
    public static final int STATE_CANCEL = 5;

    @IntDef(value = {STATE_IDLE,
            STATE_GENERATING_INFO,
            STATE_WRING_TO_FILE,
            STATE_FAIL,
            STATE_SUCCESS,
            STATE_CANCEL})
    @interface State {
    }

    private String id;
    private String filePath;
    private long startPosition;
    private long currentPosition;
    private long endPosition;
    @State
    private int state;
    private Date createTime;
    private String errorMsg;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(long startPosition) {
        this.startPosition = startPosition;
    }

    public long getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(long currentPosition) {
        this.currentPosition = currentPosition;
    }

    public long getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(long endPosition) {
        this.endPosition = endPosition;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
