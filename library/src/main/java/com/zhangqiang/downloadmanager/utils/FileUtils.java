package com.zhangqiang.downloadmanager.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class FileUtils {

    public static final String TAG = FileUtils.class.getSimpleName();

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
            if (accessFile != null) {
                accessFile.close();
            }
            if (is != null) {
                is.close();
            }
        }
    }


    public static String getDistinctFileName(String dir, String fileName) {

        if (!new File(dir, fileName).exists()) {
            return fileName;
        }

        int index = fileName.lastIndexOf(".");
        final String firstName;
        final String lastName;
        if (index == -1) {
            firstName = fileName;
            lastName = "";
        } else {
            firstName = fileName.substring(0, index);
            lastName = fileName.substring(index + 1);
        }

        String targetFileName = fileName;
        for (int i = 1; i < Integer.MAX_VALUE; i++) {

            targetFileName = firstName + "(" + i + ")";
            if (!TextUtils.isEmpty(lastName)) {
                targetFileName += "." + lastName;
            }
            File file = new File(dir, targetFileName);
            if (!file.exists()) {
                break;
            }
        }
        return targetFileName;
    }

    public static void deleteFileOrThrow(File file) throws IOException {
        if (file.exists() && file.isFile()) {
            if (!file.delete()) {
                throw new IOException("delete file fail:" + file.getAbsolutePath());
            }
        }
    }

    public static void deleteFile(File file) {
        if (file.exists() && file.isFile()) {
            if (!file.delete()) {
                Log.i(TAG, "delete file fail:" + file.getAbsolutePath());
            }
        }
    }

    public static void deleteDir(File dir) {
        if (dir.exists() && dir.isDirectory()) {
            if (!dir.delete()) {
                Log.i(TAG, "delete dir fail:" + dir.getAbsolutePath());
            }
        }
    }

    public static File createDirIfNotExists(File dirFile) throws IOException {
        if (!dirFile.exists()) {
            if (!dirFile.mkdirs()) {
                throw new IOException("cannot create dir:" + dirFile.getAbsolutePath());
            }
        }
        return dirFile;
    }

    public static String getFileFormat(String filepath) {
        if (filepath == null) {
            return null;
        }
        int index = filepath.lastIndexOf(".");
        if (index == -1 || index == filepath.length() - 1) {
            return null;
        }
        return filepath.substring(index + 1);
    }
}
