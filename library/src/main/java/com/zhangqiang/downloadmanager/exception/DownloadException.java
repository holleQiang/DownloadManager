package com.zhangqiang.downloadmanager.exception;

public class DownloadException extends RuntimeException{

    public static final int HTTP_CONNECT_FAIL = 100;
    public static final int HTTP_RESPONSE_ERROR = 101;
    public static final int WRITE_FILE_FAIL = 102;
    public static final int MERGE_PART_FAIL = 103;

    private int code;

    public DownloadException(int code,String message) {
        super(message);
        this.code = code;
    }

    public DownloadException( int code,Throwable cause) {
        super(cause);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
