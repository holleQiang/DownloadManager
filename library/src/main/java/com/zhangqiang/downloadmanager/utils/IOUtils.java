package com.zhangqiang.downloadmanager.utils;

import java.io.Closeable;
import java.io.IOException;

public class IOUtils {

    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }
}
