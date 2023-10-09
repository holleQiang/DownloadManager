package com.zhangqiang.downloadmanager.manager;

public class RemoveTaskOptions {
    private boolean deleteFile;

    public boolean isDeleteFile() {
        return deleteFile;
    }

    public RemoveTaskOptions setDeleteFile(boolean deleteFile) {
        this.deleteFile = deleteFile;
        return this;
    }
}
