package com.zhangqiang.myftp.session;

import com.zhangqiang.myftp.account.bean.AccountBean;
import com.zhangqiang.myftp.global.executor.AppExecutors;

import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class SessionService {

    private volatile Session session;
    private static volatile SessionService sessionService;
    private final AtomicBoolean sessionCreating = new AtomicBoolean(false);

    public synchronized static void init(){
        sessionService = new SessionService();
    }

    public static SessionService get(){
        if (sessionService == null) {
            throw new IllegalStateException("please call init method");
        }
        return sessionService;
    }

    public void createSession(AccountBean accountBean,
                              SessionCreateListener listener){
        if (session != null) {
            throw new IllegalStateException("session is already exists");
        }
        if (sessionCreating.getAndSet(true)) {
            return;
        }
        AppExecutors.defaultExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    FTPClient ftpClient = new FTPClient();
                    ftpClient.setConnectTimeout(5000);
                    ftpClient.setDataTimeout(10000);
                    ftpClient.enterLocalPassiveMode();
                    try {
                        ftpClient.connect(accountBean.getIp(),accountBean.getPort());
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            ftpClient.disconnect();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if (listener != null) {
                            listener.onConnectFail();
                        }
                        return;
                    }
                    try {
                        boolean loginSuccess = ftpClient.login(accountBean.getUserName(), accountBean.getPassword());
                    } catch (IOException e) {
                        e.printStackTrace();
                        try {
                            ftpClient.disconnect();
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                        if (listener != null) {
                            listener.onLoginFail();
                        }
                        return;
                    }
                    session = new Session(ftpClient);
                    if (listener != null) {
                        listener.onSuccess();
                    }
                }finally {
                    sessionCreating.set(false);
                }
            }
        });
    }

    public void createSessionIfNeed(AccountBean accountBean,
                                    SessionCreateListener listener){
        if (session == null) {
            createSession(accountBean,listener);
        }
    }

    public Session getSession() {
        return session;
    }

    public interface SessionCreateListener{
        void onSuccess();

        void onConnectFail();

        void onLoginFail();
    }
}
