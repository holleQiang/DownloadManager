package com.zhangqiang.myftp.db.entity;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class AccountEntity {

    @Id
    private String id;
    private String ip;
    private int port;
    private String userName;
    private String password;
    private int status;
    @Generated(hash = 1387867308)
    public AccountEntity(String id, String ip, int port, String userName,
            String password, int status) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.status = status;
    }
    @Generated(hash = 40307897)
    public AccountEntity() {
    }
    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getIp() {
        return this.ip;
    }
    public void setIp(String ip) {
        this.ip = ip;
    }
    public int getPort() {
        return this.port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public String getUserName() {
        return this.userName;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getPassword() {
        return this.password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public int getStatus() {
        return this.status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
}
