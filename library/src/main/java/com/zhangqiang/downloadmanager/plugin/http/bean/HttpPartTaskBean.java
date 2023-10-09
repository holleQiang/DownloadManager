package com.zhangqiang.downloadmanager.plugin.http.bean;

import androidx.annotation.IntDef;

import java.util.List;

public class HttpPartTaskBean {

    public static final int STATE_IDLE = 0;
    public static final int STATE_WAITING_PART_FINISH = 1;
    public static final int STATE_MERGING_PART = 2;
    public static final int STATE_FAIL = 3;
    public static final int STATE_SUCCESS = 4;
    public static final int STATE_CANCEL = 5;

    @IntDef(value = {STATE_IDLE,
            STATE_WAITING_PART_FINISH,
            STATE_MERGING_PART,
            STATE_FAIL,
            STATE_SUCCESS,
            STATE_CANCEL})
    @interface State {
    }

    private String id;
    @State
    private int state;
    private long createTime;
    private String errorMsg;
    private List<HttpPartTaskItemBean> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public HttpPartTaskBean setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public List<HttpPartTaskItemBean> getItems() {
        return items;
    }

    public void setItems(List<HttpPartTaskItemBean> items) {
        this.items = items;
    }


}
