package com.zhangqiang.downloadmanager.task.ftp;

import android.text.TextUtils;

import com.zhangqiang.downloadmanager.exception.DownloadException;
import com.zhangqiang.downloadmanager.task.ftp.callback.ResourceInfo;
import com.zhangqiang.downloadmanager.utils.FileUtils;
import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.downloadmanager2.task.DownloadTask;
import com.zhangqiang.downloadmanager2.task.Status;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.util.Objects;

public class FTPDownloadTask extends DownloadTask {

    public static final String TAG = FTPDownloadTask.class.getCanonicalName();

    private final String host;
    private final int port;
    private final String userName;
    private final String password;
    private final String ftpDir;
    private final String ftpFileName;

    private Thread thread;
    private FTPClient ftpClient;
    private long currentLength;


    public FTPDownloadTask(String saveDir, String targetFileName, long createTime, String host, int port, String userName, String password, String ftpDir, String ftpFileName) {
        super(saveDir, targetFileName, createTime);
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.ftpDir = ftpDir;
        this.ftpFileName = ftpFileName;
    }

    public FTPDownloadTask(String saveDir, String targetFileName, long createTime, Status status, String errorMessage, String host, int port, String userName, String password, String ftpDir, String ftpFileName) {
        super(saveDir, targetFileName, createTime, status, errorMessage);
        this.host = host;
        this.port = port;
        this.userName = userName;
        this.password = password;
        this.ftpDir = ftpDir;
        this.ftpFileName = ftpFileName;
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
                ftpClient.enterLocalPassiveMode();
                try {
                    ftpClient.connect(host, port);
                    LogUtils.i(TAG, "========ftp connect success");
                    boolean loginSuccess = ftpClient.login(userName, password);
                    if (loginSuccess) {
                        LogUtils.i(TAG, "========ftp login success");
                    } else {
                        dispatchFail(new DownloadException(1001, "login fail"));
                        return;
                    }

                    boolean changeDirSuccess = ftpClient.changeWorkingDirectory(ftpDir);
                    if (changeDirSuccess) {
                        LogUtils.i(TAG, "========ftp change dir success");
                    } else {
                        dispatchFail(new DownloadException(1002, "change dir fail"));
                        return;
                    }
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
                    boolean setFileTypeSuccess = ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                    if (setFileTypeSuccess) {
                        LogUtils.i(TAG, "========ftp set file success");
                    } else {
                        dispatchFail(new DownloadException(1003, "set file type fail"));
                        return;
                    }
                    //continue download
                    ftpClient.setRestartOffset(currentLength);
                    InputStream inputStream = ftpClient.retrieveFileStream(ftpFileName);
                    FileUtils.writeToFileFrom(inputStream,
                            new File(getSaveFileName(), getSaveFileName()),
                            currentLength,
                            new FileUtils.WriteFileListener() {
                                @Override
                                public void onWriteFile(byte[] buffer, int offset, int len) {
                                    currentLength += len;
                                }
                            });
                    LogUtils.i(TAG, "========ftp 下载成功");
                    dispatchSuccess();
                } catch (IOException e) {
                    e.printStackTrace();
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
        return ftpFileName;
    }


    private static String encode(String ftpStr) {
        try {
            return new String(ftpStr.getBytes("utf-8"), "iso8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("encode ftr str fail:" + ftpStr);
        }
    }
}
