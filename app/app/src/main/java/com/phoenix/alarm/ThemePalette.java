package com.phoenix.alarm;

import android.graphics.Color;

import java.util.Calendar;

/**
 * The app deliberately uses one palette at a time. Light mode is shown from
 * 06:00 until 17:59 and dark mode from 18:00 until 05:59.
 */
public final class ThemePalette {

    public final boolean dark;

    public final int backgroundTop;
    public final int backgroundBottom;
    public final int text;
    public final int label;
    public final int muted;
    public final int accent;
    public final int accentMuted;
    public final int accentFaint;
    public final int track;
    public final int line;
    public final int dayBorder;
    public final int dayText;
    public final int daySelectedText;
    public final int toggleOff;
    public final int saveText;
    public final int sheetBackground;
    public final int soft;
    public final int scrim;
    public final int toastBackground;
    public final int toastText;
    public final int ringMuted;

    private ThemePalette(
            boolean dark,
            int backgroundTop,
            int backgroundBottom,
            int text,
            int label,
            int muted,
            int accent,
            int accentMuted,
            int accentFaint,
            int track,
            int line,
            int dayBorder,
            int dayText,
            int daySelectedText,
            int toggleOff,
            int saveText,
            int sheetBackground,
            int soft,
            int scrim,
            int toastBackground,
            int toastText,
            int ringMuted
    ) {
        this.dark = dark;
        this.backgroundTop = backgroundTop;
        this.backgroundBottom = backgroundBottom;
        this.text = text;
        this.label = label;
        this.muted = muted;
        this.accent = accent;
        this.accentMuted = accentMuted;
        this.accentFaint = accentFaint;
        this.track = track;
        this.line = line;
        this.dayBorder = dayBorder;
        this.dayText = dayText;
        this.daySelectedText = daySelectedText;
        this.toggleOff = toggleOff;
        this.saveText = saveText;
        this.sheetBackground = sheetBackground;
        this.soft = soft;
        this.scrim = scrim;
        this.toastBackground = toastBackground;
        this.toastText = toastText;
        this.ringMuted = ringMuted;
    }

    public static ThemePalette forNow() {
        return fromHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY));
    }

    public static ThemePalette fromHour(int hourOfDay) {
        return isDaytimeHour(hourOfDay) ? light() : dark();
    }

    public static boolean isDaytimeHour(int hourOfDay) {
        return hourOfDay >= 6 && hourOfDay < 18;
    }

    public static ThemePalette light() {
        return new ThemePalette(
                false,
                Color.rgb(255, 253, 251),
                Color.rgb(248, 244, 239),
                Color.rgb(36, 34, 40),
                Color.rgb(116, 110, 105),
                Color.rgb(154, 147, 140),
                Color.rgb(245, 141, 130),
                Color.rgb(217, 155, 148),
                Color.argb(30, 245, 141, 130),
                Color.rgb(242, 236, 231),
                Color.rgb(238, 232, 226),
                Color.rgb(233, 226, 220),
                Color.rgb(160, 153, 146),
                Color.rgb(94, 48, 45),
                Color.rgb(237, 231, 226),
                Color.rgb(255, 250, 248),
                Color.rgb(255, 253, 251),
                Color.rgb(245, 240, 235),
                Color.argb(102, 36, 31, 31),
                Color.rgb(44, 41, 46),
                Color.rgb(255, 253, 251),
                Color.rgb(145, 137, 129)
        );
    }

    public static ThemePalette dark() {
        return new ThemePalette(
                true,
                Color.rgb(38, 43, 56),
                Color.rgb(34, 39, 51),
                Color.rgb(244, 246, 255),
                Color.rgb(170, 177, 194),
                Color.rgb(141, 150, 170),
                Color.rgb(148, 171, 246),
                Color.rgb(126, 139, 171),
                Color.argb(30, 148, 171, 246),
                Color.rgb(53, 59, 75),
                Color.rgb(55, 62, 78),
                Color.rgb(61, 68, 84),
                Color.rgb(147, 156, 175),
                Color.rgb(33, 38, 53),
                Color.rgb(61, 68, 84),
                Color.rgb(245, 247, 255),
                Color.rgb(43, 48, 64),
                Color.rgb(50, 56, 71),
                Color.argb(153, 0, 0, 0),
                Color.rgb(243, 245, 255),
                Color.rgb(37, 42, 55),
                Color.rgb(146, 155, 174)
        );
    }
}
