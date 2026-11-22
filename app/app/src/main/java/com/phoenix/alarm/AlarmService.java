package com.phoenix.alarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;

public class AlarmService extends Service {

    public static final String ACTION_START = "com.phoenix.alarm.action.START_SERVICE";
    public static final String ACTION_RINGING_STOPPED = "com.phoenix.alarm.action.RINGING_STOPPED";

    private static final String CHANNEL_ID = "phoenix_alarm_channel";
    private static final int NOTIFICATION_ID = 1401;

    private ChimePlayer chimePlayer;
    private Vibrator vibrator;
    private PowerManager.WakeLock wakeLock;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(
                    PowerManager.PARTIAL_WAKE_LOCK,
                    "PhoenixAlarm::RingingWakeLock"
            );
            wakeLock.setReferenceCounted(false);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && AlarmReceiver.ACTION_STOP.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        AlarmPreferences.AlarmData data = new AlarmPreferences(this).load();
        Notification notification = buildNotification(data);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            );
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }

        startRinging(data);
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        stopRinging();
        stopForeground(STOP_FOREGROUND_REMOVE);
        sendBroadcast(new Intent(ACTION_RINGING_STOPPED).setPackage(getPackageName()));
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startRinging(AlarmPreferences.AlarmData data) {
        if (chimePlayer == null) {
            chimePlayer = new ChimePlayer();
            chimePlayer.start(data.sound, true);
        }
        startVibration();
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire(10L * 60L * 1000L);
        }
    }

    private void stopRinging() {
        if (chimePlayer != null) {
            chimePlayer.stop();
            chimePlayer = null;
        }
        if (vibrator != null) {
            vibrator.cancel();
            vibrator = null;
        }
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void startVibration() {
        if (vibrator == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager manager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                if (manager != null) vibrator = manager.getDefaultVibrator();
            } else {
                vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            }
        }
        if (vibrator == null || !vibrator.hasVibrator()) return;

        long[] pattern = {0L, 500L, 450L, 800L, 450L, 500L, 900L, 900L};
        vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0));
    }

    private Notification buildNotification(AlarmPreferences.AlarmData data) {
        Intent ringIntent = new Intent(this, AlarmRingActivity.class);
        ringIntent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );
        ringIntent.putExtra(AlarmRingActivity.EXTRA_FROM_ALARM, true);

        PendingIntent fullScreenIntent = PendingIntent.getActivity(
                this,
                2002,
                ringIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        PendingIntent stopIntent = PendingIntent.getBroadcast(
                this,
                3001,
                new Intent(this, AlarmReceiver.class).setAction(AlarmReceiver.ACTION_STOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm_notification)
                .setContentTitle(getString(R.string.alarm))
                .setContentText(getString(
                        R.string.alarm_notification_detail,
                        TimeUtils.formatTimeForDisplay(data.time),
                        data.sound
                ))
                .setCategory(Notification.CATEGORY_ALARM)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setPriority(Notification.PRIORITY_MAX)
                .setOngoing(true)
                .setAutoCancel(false)
                .setShowWhen(false)
                .setWhen(0L)
                .setContentIntent(fullScreenIntent)
                .setFullScreenIntent(fullScreenIntent, true)
                .addAction(new Notification.Action.Builder(
                        Icon.createWithResource(this, R.drawable.ic_alarm_notification),
                        getString(R.string.stop),
                        stopIntent
                ).build());

        if (data.snooze) {
            PendingIntent snoozeIntent = PendingIntent.getBroadcast(
                    this,
                    3002,
                    new Intent(this, AlarmReceiver.class).setAction(AlarmReceiver.ACTION_SNOOZE),
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            builder.addAction(new Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_alarm_notification),
                    getString(R.string.snooze_five),
                    snoozeIntent
            ).build());
        }

        return builder.build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager == null) return;

        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH
        );
        channel.setDescription(getString(R.string.alarm_ringing));
        channel.setSound(null, null);
        channel.enableVibration(false);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        manager.createNotificationChannel(channel);
    }
}
