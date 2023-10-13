package com.zhangqiang.downloadmanager.plugin.http.bean;

import androidx.annotation.IntDef;

public class HttpPartTaskItemBean {

    public static final int STATE_IDLE = 0;
    public static final int STATE_START = 1;
    public static final int STATE_GENERATING_INFO = 2;
    public static final int STATE_SAVING_FILE = 3;
    public static final int STATE_FAIL = 4;
    public static final int STATE_SUCCESS = 5;
    public static final int STATE_CANCEL = 6;

    @IntDef(value = {STATE_IDLE,
            STATE_START,
            STATE_GENERATING_INFO,
            STATE_SAVING_FILE,
            STATE_FAIL,
            STATE_SUCCESS,
            STATE_CANCEL})
    @interface State {
    }

    private String id;
    private String saveDir;
    /**
     * 实际保存的文件名
     */
    private String saveFileName;
    private long startPosition;
    private long endPosition;
    private long currentLength;
    @State
    private int state;
    private long createTime;
    private String errorMsg;
    /**
     * 优先级
     */
    private int priority;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public HttpPartTaskItemBean setSaveDir(String saveDir) {
        this.saveDir = saveDir;
        return this;
    }

    public long getStartPosition() {
        return startPosition;
    }

    public HttpPartTaskItemBean setStartPosition(long startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    public long getCurrentLength() {
        return currentLength;
    }

    public HttpPartTaskItemBean setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
        return this;
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

    public long getCreateTime() {
        return createTime;
    }

    public HttpPartTaskItemBean setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public HttpPartTaskItemBean setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public HttpPartTaskItemBean setPriority(int priority) {
        this.priority = priority;
        return this;
    }
}
