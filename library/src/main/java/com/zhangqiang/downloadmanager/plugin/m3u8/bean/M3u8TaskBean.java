package com.zhangqiang.downloadmanager.plugin.m3u8.bean;

import com.zhangqiang.downloadmanager.plugin.m3u8.parser.bean.M3u8File;

import org.greenrobot.greendao.annotation.NotNull;

import java.util.List;

public class M3u8TaskBean {

    public static final int STATE_IDLE = 0;
    public static final int STATE_START = 1;
    public static final int STATE_GENERATING_INFO = 2;
    public static final int STATE_WAITING_CHILDREN_TASK = 3;
    public static final int STATE_SUCCESS = 4;
    public static final int STATE_FAIL = 5;
    public static final int STATE_CANCEL = 6;
    private String id;
    /**
     * 下载链接
     */
    @NotNull
    private String url;
    /**
     * 保存目录
     */
    @NotNull
    private String saveDir;
    /**
     * 指定的文件名称
     */
    private String targetFileName;
    /**
     * 实际保存的文件名
     */
    private String saveFileName;

    private long createTime;

    private float duration;
    private M3u8File m3u8FileInfo;
    /**
     * 状态
     */
    @NotNull
    private int state;
    private String errorMsg;
    /**
     * 子任务id
     */
    private List<TSTaskBean> tsTaskBeans;

    public String getId() {
        return id;
    }

    public M3u8TaskBean setId(String id) {
        this.id = id;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public M3u8TaskBean setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getSaveDir() {
        return saveDir;
    }

    public M3u8TaskBean setSaveDir(String saveDir) {
        this.saveDir = saveDir;
        return this;
    }

    public String getTargetFileName() {
        return targetFileName;
    }

    public M3u8TaskBean setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
        return this;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public M3u8TaskBean setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
        return this;
    }

    public long getCreateTime() {
        return createTime;
    }

    public M3u8TaskBean setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    public float getDuration() {
        return duration;
    }

    public M3u8TaskBean setDuration(float duration) {
        this.duration = duration;
        return this;
    }

    public int getState() {
        return state;
    }

    public M3u8TaskBean setState(int state) {
        this.state = state;
        return this;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public M3u8TaskBean setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
        return this;
    }

    public List<TSTaskBean> getTsTaskBeans() {
        return tsTaskBeans;
    }

    public M3u8TaskBean setTsTaskBeans(List<TSTaskBean> tsTaskBeans) {
        this.tsTaskBeans = tsTaskBeans;
        return this;
    }

    public M3u8File getM3u8FileInfo() {
        return m3u8FileInfo;
    }

    public M3u8TaskBean setM3u8FileInfo(M3u8File m3u8FileInfo) {
        this.m3u8FileInfo = m3u8FileInfo;
        return this;
    }
}
