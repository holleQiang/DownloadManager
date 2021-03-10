package com.zhangqiang.downloadmanager.db.entity;

import com.zhangqiang.downloadmanager.db.dao.DaoSession;
import com.zhangqiang.downloadmanager.db.dao.PartEntityDao;
import com.zhangqiang.downloadmanager.db.dao.TaskEntityDao;

import org.greenrobot.greendao.DaoException;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.ToMany;

import java.util.List;

@Entity
public class TaskEntity{

    @Id(autoincrement = true)
    Long id;
    private String url;
    private String saveDir;
    private String fileName;
    private long currentLength;
    private long contentLength;
    private int state;
    private String eTag;
    private String lastModified;
    private String contentType;
    private long createTime;
    private String errorMsg;
    private int threadSize;
    @ToMany(referencedJoinProperty = "taskId")
    private List<PartEntity> partList;
    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;
    /** Used for active entity operations. */
    @Generated(hash = 263689402)
    private transient TaskEntityDao myDao;
    @Generated(hash = 1884574042)
    public TaskEntity(Long id, String url, String saveDir, String fileName,
            long currentLength, long contentLength, int state, String eTag,
            String lastModified, String contentType, long createTime,
            String errorMsg, int threadSize) {
        this.id = id;
        this.url = url;
        this.saveDir = saveDir;
        this.fileName = fileName;
        this.currentLength = currentLength;
        this.contentLength = contentLength;
        this.state = state;
        this.eTag = eTag;
        this.lastModified = lastModified;
        this.contentType = contentType;
        this.createTime = createTime;
        this.errorMsg = errorMsg;
        this.threadSize = threadSize;
    }
    @Generated(hash = 397975341)
    public TaskEntity() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
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
    public String getFileName() {
        return this.fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public long getCurrentLength() {
        return this.currentLength;
    }
    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }
    public long getContentLength() {
        return this.contentLength;
    }
    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }
    public int getState() {
        return this.state;
    }
    public void setState(int state) {
        this.state = state;
    }
    public String getETag() {
        return this.eTag;
    }
    public void setETag(String eTag) {
        this.eTag = eTag;
    }
    public String getLastModified() {
        return this.lastModified;
    }
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
    public String getContentType() {
        return this.contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public long getCreateTime() {
        return this.createTime;
    }
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }
    public String getErrorMsg() {
        return this.errorMsg;
    }
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
    public int getThreadSize() {
        return this.threadSize;
    }
    public void setThreadSize(int threadSize) {
        this.threadSize = threadSize;
    }
    /**
     * To-many relationship, resolved on first access (and after reset).
     * Changes to to-many relations are not persisted, make changes to the target entity.
     */
    @Generated(hash = 2072162551)
    public List<PartEntity> getPartList() {
        if (partList == null) {
            final DaoSession daoSession = this.daoSession;
            if (daoSession == null) {
                throw new DaoException("Entity is detached from DAO context");
            }
            PartEntityDao targetDao = daoSession.getPartEntityDao();
            List<PartEntity> partListNew = targetDao._queryTaskEntity_PartList(id);
            synchronized (this) {
                if (partList == null) {
                    partList = partListNew;
                }
            }
        }
        return partList;
    }
    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 141659070)
    public synchronized void resetPartList() {
        partList = null;
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }
    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }
    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 424431507)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getTaskEntityDao() : null;
    }
   
}
