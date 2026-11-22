package com.phoenix.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AlarmReceiver extends BroadcastReceiver {

    public static final String ACTION_TRIGGER = "com.phoenix.alarm.action.TRIGGER";
    public static final String ACTION_SNOOZE_TRIGGER = "com.phoenix.alarm.action.SNOOZE_TRIGGER";
    public static final String ACTION_STOP = "com.phoenix.alarm.action.STOP";
    public static final String ACTION_SNOOZE = "com.phoenix.alarm.action.SNOOZE";

    public static final long SNOOZE_DURATION_MS = 5L * 60L * 1000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        if (action == null) return;

        if (ACTION_STOP.equals(action)) {
            context.stopService(new Intent(context, AlarmService.class));
            return;
        }

        if (ACTION_SNOOZE.equals(action)) {
            AlarmScheduler.scheduleSnooze(context, SNOOZE_DURATION_MS);
            context.stopService(new Intent(context, AlarmService.class));
            return;
        }

        if (ACTION_SNOOZE_TRIGGER.equals(action)) {
            startAlarmService(context);
            return;
        }

        if (ACTION_TRIGGER.equals(action)) {
            // Keep the weekly/daily alarm chain alive before ringing this one.
            AlarmScheduler.scheduleNext(context);
            startAlarmService(context);
        }
    }

    private static void startAlarmService(Context context) {
        Intent serviceIntent = new Intent(context, AlarmService.class);
        serviceIntent.setAction(AlarmService.ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent);
        } else {
            context.startService(serviceIntent);
        }
    }
}
