package com.zhangqiang.myftp.account.bean;

public class AccountBean {

    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_LOGIN = 1;
    public static final int STATUS_PREPARE_LOGIN = 2;

    private String id;
    private String ip;
    private int port;
    private String userName;
    private String password;
    private int status;

    public String getId() {
        return id;
    }

    public AccountBean setId(String id) {
        this.id = id;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public AccountBean setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public int getPort() {
        return port;
    }

    public AccountBean setPort(int port) {
        this.port = port;
        return this;
    }

    public String getUserName() {
        return userName;
    }

    public AccountBean setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public AccountBean setPassword(String password) {
        this.password = password;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public AccountBean setStatus(int status) {
        this.status = status;
        return this;
    }
}
