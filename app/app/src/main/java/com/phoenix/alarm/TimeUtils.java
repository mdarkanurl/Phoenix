package com.phoenix.alarm;

import android.content.Context;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Locale;

public final class TimeUtils {

    public static final String[] DAY_NAMES = {
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    };

    public static final String[] DAY_LETTERS = {
            "S", "M", "T", "W", "T", "F", "S"
    };

    private TimeUtils() {
    }

    public static final class TimeParts {
        public final int hour;
        public final int minute;
        public final String period;

        public TimeParts(int hour, int minute, String period) {
            this.hour = hour;
            this.minute = minute;
            this.period = period;
        }
    }

    public static TimeParts formatTime(String time) {
        int hours = 7;
        int minutes = 0;

        try {
            String[] pieces = time.split(":");
            hours = Integer.parseInt(pieces[0]);
            minutes = Integer.parseInt(pieces[1]);
        } catch (Exception ignored) {
            // Keep the safe default above if stored data is malformed.
        }

        if (hours < 0 || hours > 23) hours = 7;
        if (minutes < 0 || minutes > 59) minutes = 0;

        String period = hours >= 12 ? "PM" : "AM";
        int hour = hours % 12;
        if (hour == 0) hour = 12;

        return new TimeParts(hour, minutes, period);
    }

    public static String to24Hour(int hour12, int minute, boolean pm) {
        int hours = hour12 % 12;
        if (pm) hours += 12;
        return String.format(Locale.US, "%02d:%02d", hours, minute);
    }

    public static String formatTimeForDisplay(String time) {
        TimeParts parts = formatTime(time);
        return String.format(Locale.US, "%d:%02d %s", parts.hour, parts.minute, parts.period);
    }

    public static String formatDays(Context context, int daysMask) {
        int mask = daysMask & 0x7F;
        if (mask == 0) {
            return context.getString(R.string.never);
        }
        if (mask == 0x7F) {
            return context.getString(R.string.every_day);
        }
        if (mask == 0x3E) {
            return context.getString(R.string.weekdays);
        }
        if (mask == 0x41) {
            return context.getString(R.string.weekends);
        }

        StringBuilder builder = new StringBuilder();
        for (int day = 0; day < 7; day++) {
            if ((mask & (1 << day)) == 0) continue;
            if (builder.length() > 0) builder.append(' ');
            builder.append(DAY_NAMES[day]);
        }
        return builder.toString();
    }

    public static long nextOccurrenceMillis(AlarmPreferences.AlarmData data, long nowMillis) {
        if (data == null || !data.hasAnyDay()) {
            return -1L;
        }

        TimeParts parts = formatTime(data.time);
        int hour24;
        if ("PM".equals(parts.period)) {
            hour24 = parts.hour % 12 + 12;
        } else {
            hour24 = parts.hour % 12;
        }

        ZoneId zone = ZoneId.systemDefault();
        LocalDateTime now = LocalDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zone);
        LocalTime target = LocalTime.of(hour24, parts.minute);

        for (int offset = 0; offset < 8; offset++) {
            LocalDate date = now.toLocalDate().plusDays(offset);
            int dayIndex = date.getDayOfWeek().getValue() % 7;
            if (!data.isDaySelected(dayIndex)) continue;

            ZonedDateTime candidate = ZonedDateTime.of(date, target, zone);
            long candidateMillis = candidate.toInstant().toEpochMilli();
            if (candidateMillis > nowMillis) {
                return candidateMillis;
            }
        }

        return -1L;
    }
}
