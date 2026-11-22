package com.phoenix.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public final class AlarmScheduler {

    public static final int REQUEST_ALARM = 1001;
    public static final int REQUEST_SNOOZE = 1002;
    public static final int REQUEST_SHOW_APP = 2001;

    private AlarmScheduler() {
    }

    public static boolean scheduleNext(Context context) {
        AlarmPreferences preferences = new AlarmPreferences(context);
        AlarmPreferences.AlarmData data = preferences.load();

        if (!data.saved || !data.hasAnyDay()) {
            cancelAlarm(context);
            return false;
        }

        long triggerAt = TimeUtils.nextOccurrenceMillis(data, System.currentTimeMillis());
        if (triggerAt <= 0L) {
            cancelAlarm(context);
            return false;
        }

        PendingIntent operation = operation(context, REQUEST_ALARM, AlarmReceiver.ACTION_TRIGGER);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return false;

        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(
                triggerAt,
                showAppIntent(context)
        );

        try {
            if (!canScheduleExactAlarms(context)) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation);
                return false;
            }
            alarmManager.setAlarmClock(info, operation);
            return true;
        } catch (SecurityException ignored) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation);
            return false;
        }
    }

    public static boolean scheduleSnooze(Context context, long delayMillis) {
        AlarmPreferences preferences = new AlarmPreferences(context);
        AlarmPreferences.AlarmData data = preferences.load();
        if (!data.snooze) return false;

        long triggerAt = System.currentTimeMillis() + Math.max(60_000L, delayMillis);
        PendingIntent operation = operation(
                context,
                REQUEST_SNOOZE,
                AlarmReceiver.ACTION_SNOOZE_TRIGGER
        );
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return false;

        AlarmManager.AlarmClockInfo info = new AlarmManager.AlarmClockInfo(
                triggerAt,
                showAppIntent(context)
        );

        try {
            if (!canScheduleExactAlarms(context)) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation);
                return false;
            }
            alarmManager.setAlarmClock(info, operation);
            return true;
        } catch (SecurityException ignored) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, operation);
            return false;
        }
    }

    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return alarmManager != null && alarmManager.canScheduleExactAlarms();
    }

    public static void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        alarmManager.cancel(operation(context, REQUEST_ALARM, AlarmReceiver.ACTION_TRIGGER));
    }

    public static void cancelSnooze(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;
        alarmManager.cancel(operation(context, REQUEST_SNOOZE, AlarmReceiver.ACTION_SNOOZE_TRIGGER));
    }

    public static void cancelAll(Context context) {
        cancelAlarm(context);
        cancelSnooze(context);
    }

    private static PendingIntent operation(Context context, int requestCode, String action) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(action);
        return PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }

    private static PendingIntent showAppIntent(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return PendingIntent.getActivity(
                context,
                REQUEST_SHOW_APP,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
