package com.zhangqiang.downloadmanager.task.http.part;

import java.io.File;
import java.util.List;

public class PartInfo {

    private final List<PartFile> partFiles;

    public PartInfo(List<PartFile> partFiles) {
        this.partFiles = partFiles;
    }

    public List<PartFile> getPartFiles() {
        return partFiles;
    }

    public static class PartFile{
        private final long start;
        private final long end;
        private final File file;

        public PartFile(long start, long end, File file) {
            this.start = start;
            this.end = end;
            this.file = file;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }

        public File getFile() {
            return file;
        }
    }
}
