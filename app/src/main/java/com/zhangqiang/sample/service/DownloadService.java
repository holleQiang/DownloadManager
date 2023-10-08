package com.zhangqiang.sample.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

import com.zhangqiang.downloadmanager.utils.LogUtils;
import com.zhangqiang.sample.R;
import com.zhangqiang.sample.ui.MainActivity;

public class DownloadService extends Service {


    private ClipboardManager clipboardManager;
    private NotificationManager notificationManager;
    private int notificationId = 32365;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(clipChangedListener);
        }
        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(getResources().getString(R.string.download_notification_channel_id),
                    getResources().getString(R.string.download_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        processCurrentClipboard();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    ClipboardManager.OnPrimaryClipChangedListener clipChangedListener = new ClipboardManager.OnPrimaryClipChangedListener() {
        @Override
        public void onPrimaryClipChanged() {
            processCurrentClipboard();
        }
    };

    private void processCurrentClipboard() {
        if (clipboardManager == null) {
            return;
        }
        if (!clipboardManager.hasPrimaryClip()) {
            return;
        }
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null) {
            return;
        }
        int itemCount = clipData.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            ClipData.Item item = clipData.getItemAt(i);
            CharSequence sequence = item.getText();
            if (sequence == null) {
                continue;
            }
            String text = sequence.toString();
            Uri uri = Uri.parse(text);
            String scheme = uri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                Notification notification = buildDownloadTaskNotification(text);
                if (notificationManager != null) {
                    notificationManager.notify(notificationId, notification);
                    notificationId++;
                }
            }
            LogUtils.i("DownloadService", "==========" + item.toString());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (clipboardManager != null) {
            clipboardManager.removePrimaryClipChangedListener(clipChangedListener);
            clipboardManager = null;
        }
    }

    public Notification buildDownloadTaskNotification(String link) {

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("link", link);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1000, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Builder(this, getResources().getString(R.string.download_notification_channel_id))
                .setContentTitle("点击下载此内容")
                .setContentText(link)
                .setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .build();
    }

    public static String getLink(ClipboardManager clipboardManager) {
        if (!clipboardManager.hasPrimaryClip()) {
            return null;
        }
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null) {
            return null;
        }
        int itemCount = clipData.getItemCount();
        for (int i = 0; i < itemCount; i++) {
            ClipData.Item item = clipData.getItemAt(i);
            String text = item.getText().toString();
            Uri uri = Uri.parse(text);
            String scheme = uri.getScheme();
            if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) {
                return text;
            }
            LogUtils.i("DownloadService", "==========" + item.toString());
        }
        return null;
    }
}
