package com.zhangqiang.downloadmanager.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FileUtils {

    public interface WriteFileListener {

        void onWriteFile(byte[] buffer, int offset, int len);
    }

    public static void writeToFileFrom(InputStream is,
                                       File file,
                                       long from,
                                       WriteFileListener writeFileListener) throws IOException {

        RandomAccessFile accessFile = null;
        try {

            accessFile = new RandomAccessFile(file, "rw");
            accessFile.seek(from);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = is.read(buffer)) != -1) {

                accessFile.write(buffer, 0, len);
                if (writeFileListener != null) {
                    writeFileListener.onWriteFile(buffer, 0, len);
                }
            }
        } finally {
            IOUtils.closeSilently(accessFile);
            IOUtils.closeSilently(is);
        }
    }
}
