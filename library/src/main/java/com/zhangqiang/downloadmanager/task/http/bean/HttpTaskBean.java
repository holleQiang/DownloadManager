package com.zhangqiang.downloadmanager.task.http.bean;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.SOURCE;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Date;

public class HttpTaskBean {

    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_DEFAULT = 0;
    public static final int TYPE_PART = 1;

    public static final int STATE_IDLE = 0;
    public static final int STATE_START = 1;
    public static final int STATE_GENERATING_INFO = 2;
    public static final int STATE_WAITING_CHILDREN_TASK = 3;
    public static final int STATE_SUCCESS = 4;
    public static final int STATE_FAIL = 5;
    public static final int STATE_CANCEL = 6;

    @Retention(SOURCE)
    @Target({FIELD})
    @IntDef(value = {TYPE_UNKNOWN,TYPE_DEFAULT, TYPE_PART})
    @interface Type {
    }

    @Retention(SOURCE)
    @Target({FIELD})
    @IntDef(value = {STATE_IDLE,
            STATE_START,
            STATE_GENERATING_INFO,
            STATE_WAITING_CHILDREN_TASK,
            STATE_SUCCESS,
            STATE_FAIL,
            STATE_CANCEL,
    })
    @interface State {
    }

    private String id;
    private String url;
    private String saveDir;
    private String fileName;
    private long contentLength;
    private String contentType;
    private Date createTime;
    @State
    private int state;
    private String errorMsg;
    private int threadSize;
    @Type
    private int type;
    private HttpDefaultTaskBean httpDefaultTask;
    private HttpPartTaskBean httpPartTask;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public HttpTaskBean setSaveDir(String saveDir) {
        this.saveDir = saveDir;
        return this;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public HttpDefaultTaskBean getHttpDefaultTask() {
        return httpDefaultTask;
    }

    public void setHttpDefaultTask(HttpDefaultTaskBean httpDefaultTask) {
        this.httpDefaultTask = httpDefaultTask;
    }

    public HttpPartTaskBean getHttpPartTask() {
        return httpPartTask;
    }

    public void setHttpPartTask(HttpPartTaskBean httpPartTask) {
        this.httpPartTask = httpPartTask;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getThreadSize() {
        return threadSize;
    }

    public HttpTaskBean setThreadSize(int threadSize) {
        this.threadSize = threadSize;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public HttpTaskBean setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }
}
