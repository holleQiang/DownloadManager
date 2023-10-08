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
    private String targetFileName;
    /**
     * 实际保存的文件名
     */
    private String saveFileName;
    private long contentLength;
    private int responseCode;
    private String contentType;
    private long createTime;
    @State
    private int state;
    private String errorMsg;
    private int threadSize;
    @Type
    private int type;
    private HttpDefaultTaskBean httpDefaultTaskBean;
    private HttpPartTaskBean httpPartTaskBean;

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

    public long getCreateTime() {
        return createTime;
    }

    public HttpTaskBean setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public HttpDefaultTaskBean getHttpDefaultTaskBean() {
        return httpDefaultTaskBean;
    }

    public void setHttpDefaultTaskBean(HttpDefaultTaskBean httpDefaultTask) {
        this.httpDefaultTaskBean = httpDefaultTask;
    }

    public HttpPartTaskBean getHttpPartTaskBean() {
        return httpPartTaskBean;
    }

    public void setHttpPartTask(HttpPartTaskBean httpPartTask) {
        this.httpPartTaskBean = httpPartTask;
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

    public String getTargetFileName() {
        return targetFileName;
    }

    public HttpTaskBean setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
        return this;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public HttpTaskBean setResponseCode(int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public HttpTaskBean setHttpPartTaskBean(HttpPartTaskBean httpPartTaskBean) {
        this.httpPartTaskBean = httpPartTaskBean;
        return this;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public HttpTaskBean setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
        return this;
    }
}
