package com.zhangqiang.downloadmanager.db.entity;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.NotNull;

@Entity
public class PartEntity {

    @Id(autoincrement = true)
    private Long id;
    @NotNull
    private Long taskId;
    @NotNull
    private String savePath;
    private long current;
    private long start;
    private long end;
    private int threadIndex;
    private int threadSize;
    @Generated(hash = 675943319)
    public PartEntity(Long id, @NotNull Long taskId, @NotNull String savePath,
            long current, long start, long end, int threadIndex, int threadSize) {
        this.id = id;
        this.taskId = taskId;
        this.savePath = savePath;
        this.current = current;
        this.start = start;
        this.end = end;
        this.threadIndex = threadIndex;
        this.threadSize = threadSize;
    }
    @Generated(hash = 711446309)
    public PartEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public Long getTaskId() {
        return this.taskId;
    }
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    public String getSavePath() {
        return this.savePath;
    }
    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }
    public long getCurrent() {
        return this.current;
    }
    public void setCurrent(long current) {
        this.current = current;
    }
    public long getStart() {
        return this.start;
    }
    public void setStart(long start) {
        this.start = start;
    }
    public long getEnd() {
        return this.end;
    }
    public void setEnd(long end) {
        this.end = end;
    }
    public int getThreadIndex() {
        return this.threadIndex;
    }
    public void setThreadIndex(int threadIndex) {
        this.threadIndex = threadIndex;
    }
    public int getThreadSize() {
        return this.threadSize;
    }
    public void setThreadSize(int threadSize) {
        this.threadSize = threadSize;
    }

    
}
