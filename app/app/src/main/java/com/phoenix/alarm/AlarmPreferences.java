package com.phoenix.alarm;

import android.content.Context;
import android.content.SharedPreferences;

public final class AlarmPreferences {

    private static final String FILE_NAME = "phoenix_alarm";
    private static final String KEY_TIME = "time";
    private static final String KEY_DAYS_MASK = "days_mask";
    private static final String KEY_SOUND = "sound";
    private static final String KEY_SNOOZE = "snooze";
    private static final String KEY_SAVED = "saved";

    public static final String DEFAULT_TIME = "07:00";
    public static final String DEFAULT_SOUND = "Slow Rise";
    public static final int DEFAULT_DAYS_MASK = (1 << 1) | (1 << 3) | (1 << 5);

    public static final class AlarmData {
        public String time;
        public int daysMask;
        public String sound;
        public boolean snooze;
        public boolean saved;

        public AlarmData(String time, int daysMask, String sound, boolean snooze, boolean saved) {
            this.time = time;
            this.daysMask = daysMask;
            this.sound = sound;
            this.snooze = snooze;
            this.saved = saved;
        }

        public boolean hasAnyDay() {
            return (daysMask & 0x7F) != 0;
        }

        public boolean isDaySelected(int dayIndex) {
            return (daysMask & (1 << dayIndex)) != 0;
        }

        public void toggleDay(int dayIndex) {
            daysMask ^= (1 << dayIndex);
            daysMask &= 0x7F;
        }
    }

    private final SharedPreferences preferences;

    public AlarmPreferences(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
    }

    public AlarmData load() {
        return new AlarmData(
                preferences.getString(KEY_TIME, DEFAULT_TIME),
                preferences.getInt(KEY_DAYS_MASK, DEFAULT_DAYS_MASK),
                preferences.getString(KEY_SOUND, DEFAULT_SOUND),
                preferences.getBoolean(KEY_SNOOZE, true),
                preferences.getBoolean(KEY_SAVED, false)
        );
    }

    public void save(AlarmData data) {
        preferences.edit()
                .putString(KEY_TIME, data.time)
                .putInt(KEY_DAYS_MASK, data.daysMask)
                .putString(KEY_SOUND, data.sound)
                .putBoolean(KEY_SNOOZE, data.snooze)
                .putBoolean(KEY_SAVED, true)
                .apply();
        data.saved = true;
    }

    public boolean hasSavedAlarm() {
        return preferences.getBoolean(KEY_SAVED, false);
    }

    public static AlarmData defaults() {
        return new AlarmData(
                DEFAULT_TIME,
                DEFAULT_DAYS_MASK,
                DEFAULT_SOUND,
                true,
                false
        );
    }
}
