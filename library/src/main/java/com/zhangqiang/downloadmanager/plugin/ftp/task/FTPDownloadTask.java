package com.zhangqiang.downloadmanager.plugin.ftp.task;

import android.text.TextUtils;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.plugin.ftp.callback.ResourceInfo;
import com.zhangqiang.downloadmanager.task.DownloadTask;
import com.zhangqiang.downloadmanager.task.Status;
import com.zhangqiang.downloadmanager.utils.LogUtils;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FTPDownloadTask extends DownloadTask {

    public static final String TAG = FTPDownloadTask.class.getCanonicalName();

    private final String host;
    private final int port;
    private final String userName;
    private final String password;
    private final String ftpDir;
    private final String ftpFileName;
    private String saveFileName;
    private Thread thread;
    private FTPClient ftpClient;
    private ResourceInfo resourceInfo;
    private final List<OnResourceInfoReadyListener> onResourceInfoReadyListeners = new ArrayList<>();


    public FTPDownloadTask(String id,
                           String saveDir, String targetFileName, long createTime, String host, int port, String userName, String password, String ftpDir, String ftpFileName) {
        super(id, saveDir, targetFileName, createTime);
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.ftpDir = ftpDir;
        this.ftpFileName = ftpFileName;
        this.saveFileName = targetFileName;
        if (TextUtils.isEmpty(this.saveFileName)) {
            this.saveFileName = ftpFileName;
        }
    }

    public FTPDownloadTask(String id,
                           String saveDir,
                           String targetFileName,
                           long createTime,
                           Status status,
                           String errorMessage,
                           long currentLength,
                           String host,
                           int port,
                           String userName,
                           String password,
                           String ftpDir,
                           String ftpFileName,
                           ResourceInfo resourceInfo,
                           String saveFileName) {
        super(id, saveDir, targetFileName, createTime, status, errorMessage, currentLength);
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.ftpDir = ftpDir;
        this.ftpFileName = ftpFileName;
        this.resourceInfo = resourceInfo;
        this.saveFileName = saveFileName;
    }

    @Override
    protected void onStart() {

        if (TextUtils.isEmpty(ftpDir)
                || TextUtils.isEmpty(ftpFileName)
                || TextUtils.isEmpty(getSaveDir())) {
            dispatchFail(new DownloadException(DownloadException.PARAM_ERROR, "param error"));
            return;
        }

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ftpClient = new FTPClient();
                ftpClient.setConnectTimeout(5000);
                ftpClient.setDataTimeout(10000);
                ftpClient.setControlKeepAliveTimeout(300);
                ftpClient.enterLocalPassiveMode();
                try {
                    LogUtils.i(TAG, "========ftp connect start,host:" + host + ";port:" + port);
                    ftpClient.connect(host, port);
                    int replyCode = ftpClient.getReplyCode();
                    if (!FTPReply.isPositiveCompletion(replyCode)) {
                        dispatchFail(new RuntimeException("connect fail"));
                        return;
                    }
                    LogUtils.i(TAG, "========ftp connect success");
                    boolean loginSuccess = ftpClient.login(userName, password);
                    if (!loginSuccess) {
                        dispatchFail(new DownloadException(1001, "login fail,username:" + userName + ";password:" + password));
                        return;
                    }
                    LogUtils.i(TAG, "========ftp login success");

                    boolean changeDirSuccess = ftpClient.changeWorkingDirectory(ftpDir);
                    if (!changeDirSuccess) {
                        LogUtils.i(TAG, "change dir fail:" + ftpDir);
                        dispatchFail(new DownloadException(1002, "change dir fail:" + ftpDir));
                        return;
                    }
                    LogUtils.i(TAG, "========ftp change dir success");

                    FTPFile targetFtpFile = null;
                    FTPFile[] ftpFiles = ftpClient.listFiles();
                    if (ftpFiles != null && ftpFiles.length > 0) {
                        for (FTPFile ftpFile : ftpFiles) {
                            LogUtils.i(TAG, "========ftp file:" + ftpFile.getName());
                            if (Objects.equals(ftpFile.getName(), ftpFileName)) {
                                targetFtpFile = ftpFile;
                            }
                        }
                    }
                    if (targetFtpFile == null) {
                        dispatchFail(new DownloadException(1004, "file not exists"));
                        return;
                    }
                    ResourceInfo resourceInfo = new ResourceInfo(targetFtpFile.getSize(),
                            URLConnection.getFileNameMap().getContentTypeFor(getSaveFileName()));
                    dispatchResourceInfoReady(resourceInfo);

                    boolean setFileTypeSuccess = ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    if (!setFileTypeSuccess) {
                        dispatchFail(new DownloadException(1003, "set file type fail"));
                        return;
                    }
                    LogUtils.i(TAG, "========ftp set file success");

                    //continue download
                    ftpClient.setRestartOffset(getCurrentLength());
                    InputStream inputStream = ftpClient.retrieveFileStream(ftpFileName);
                    performSaveFile(inputStream);

                    //保证100%回调
                    dispatchProgressChange();
                    dispatchSuccess();
                    LogUtils.i(TAG, "========ftp 下载成功");
                } catch (IOException e) {
                    dispatchFail(new DownloadException(1000, e));
                } finally {
                    try {
                        ftpClient.disconnect();
                        ftpClient = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    protected void onCancel() {
        thread.interrupt();
        thread = null;
        try {
            if (ftpClient != null) {
                ftpClient.disconnect();
                ftpClient = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getSaveFileName() {
        return saveFileName;
    }


    private static String encode(String ftpStr) {
        try {
            return new String(ftpStr.getBytes("utf-8"), "iso8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encode ftr str fail:" + ftpStr);
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getFtpDir() {
        return ftpDir;
    }

    public String getFtpFileName() {
        return ftpFileName;
    }

    public void addOnResourceInfoReadyListener(OnResourceInfoReadyListener listener) {
        onResourceInfoReadyListeners.add(listener);
    }

    public void removeOnResourceInfoReadyListener(OnResourceInfoReadyListener listener) {
        onResourceInfoReadyListeners.remove(listener);
    }

    protected void dispatchResourceInfoReady(ResourceInfo resourceInfo) {
        this.resourceInfo = resourceInfo;
        for (int i = onResourceInfoReadyListeners.size() - 1; i >= 0; i--) {
            onResourceInfoReadyListeners.get(i).onResourceInfoReady(resourceInfo);
        }
    }

    public ResourceInfo getResourceInfo() {
        return resourceInfo;
    }

    public String buildLink() {
        StringBuilder sb = new StringBuilder("ftp://");
        String userName = getUserName();
        if (!TextUtils.isEmpty(userName)) {
            try {
                sb.append(URLEncoder.encode(userName, "utf-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            String password = getPassword();
            if (!TextUtils.isEmpty(password)) {
                sb.append(":");
                try {
                    sb.append(URLEncoder.encode(password, "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            sb.append("@");
        }
        sb.append(getHost());
        sb.append(":");
        sb.append(getPort());
        sb.append(getFtpDir());
        sb.append("/");
        sb.append(getFtpFileName());
        return sb.toString();
    }
}
