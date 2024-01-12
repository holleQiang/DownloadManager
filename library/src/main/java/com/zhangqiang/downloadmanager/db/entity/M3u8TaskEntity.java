package com.zhangqiang.downloadmanager.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class M3u8TaskEntity {

    @Id
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

    @NotNull
    private long createTime;

    private long duration;
    /**
     * 状态
     */
    @NotNull
    private int state;
    private String errorMsg;
    /**
     * 子任务id
     */
    private String tsIds;
    @Generated(hash = 1533615442)
    public M3u8TaskEntity(String id, @NotNull String url, @NotNull String saveDir,
            String targetFileName, String saveFileName, long createTime,
            long duration, int state, String errorMsg, String tsIds) {
        this.id = id;
        this.url = url;
        this.saveDir = saveDir;
        this.targetFileName = targetFileName;
        this.saveFileName = saveFileName;
        this.createTime = createTime;
        this.duration = duration;
        this.state = state;
        this.errorMsg = errorMsg;
        this.tsIds = tsIds;
    }
    @Generated(hash = 1623796849)
    public M3u8TaskEntity() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getUrl() {
        return this.url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    public String getSaveDir() {
        return this.saveDir;
    }
    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }
    public String getTargetFileName() {
        return this.targetFileName;
    }
    public void setTargetFileName(String targetFileName) {
        this.targetFileName = targetFileName;
    }
    public String getSaveFileName() {
        return this.saveFileName;
    }
    public void setSaveFileName(String saveFileName) {
        this.saveFileName = saveFileName;
    }
    public long getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public long getDuration() {
        return this.duration;
    }
    public void setDuration(long duration) {
        this.duration = duration;
    }
    public int getState() {
        return this.state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public String getErrorMsg() {
        return this.errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public String getTsIds() {
        return this.tsIds;
    }
    public void setTsIds(String tsIds) {
        this.tsIds = tsIds;
    }
}
