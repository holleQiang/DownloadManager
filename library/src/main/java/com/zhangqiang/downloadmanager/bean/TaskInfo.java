package com.zhangqiang.downloadmanager.bean;

import java.util.List;

public class TaskInfo {

    private  Long id;
    private String url;
    private String saveDir;
    private String fileName;
    private long currentLength;
    private long totalLength;
    private int state;
    private String eTag;
    private String lastModified;
    private String contentType;
    private long createTime;
    private String errorMsg;
    private int threadSize;
    private List<PartTaskInfo> partList;
    private long speed;
}
