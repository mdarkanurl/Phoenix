package com.phoenix.alarm;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowInsetsController;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.phoenix.alarm.view.ClockRingView;
import com.phoenix.alarm.view.ToggleView;

import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends Activity {

    private static final int REQUEST_NOTIFICATION_PERMISSION = 4001;

    private final Handler themeHandler = new Handler(Looper.getMainLooper());
    private final Runnable themeRunnable = new Runnable() {
        @Override
        public void run() {
            applyTheme();
            scheduleThemeRefresh();
        }
    };

    private ThemePalette palette;
    private AlarmPreferences preferences;
    private AlarmPreferences.AlarmData draft;
    private ChimePlayer previewPlayer;

    private FrameLayout root;
    private ScrollView scroll;
    private LinearLayout content;
    private TextView title;
    private ClockRingView clockRing;
    private View timeClickArea;
    private TextView timeText;
    private TextView periodText;
    private LinearLayout dayPicker;
    private final TextView[] dayButtons = new TextView[7];

    private View dividerTop;
    private View rowSounds;
    private TextView soundLabel;
    private TextView soundValue;
    private ImageView chevronSounds;
    private View dividerSounds;
    private View rowRepeat;
    private TextView repeatLabel;
    private TextView repeatValue;
    private ImageView chevronRepeat;
    private View dividerRepeat;
    private View rowSnooze;
    private TextView snoozeLabel;
    private ToggleView toggleSnooze;
    private View dividerSnooze;

    private Button resetButton;
    private Button saveButton;

    private View scrim;
    private LinearLayout sheetContainer;
    private View sheetGrabber;
    private LinearLayout timeSheet;
    private TextView timeSheetTitle;
    private TextView timeSheetCancel;
    private ImageButton hourUp;
    private TextView hourValue;
    private ImageButton hourDown;
    private ImageButton minuteUp;
    private TextView minuteValue;
    private ImageButton minuteDown;
    private LinearLayout periodSwitch;
    private TextView periodAm;
    private TextView periodPm;
    private TextView timeHint;
    private Button setTimeButton;

    private LinearLayout soundSheet;
    private TextView soundSheetTitle;
    private TextView soundSheetDone;
    private LinearLayout soundList;

    private boolean sheetVisible;
    private View activeSheet;
    private int pickerHour = 7;
    private int pickerMinute = 0;
    private boolean pickerPm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = new AlarmPreferences(this);
        draft = preferences.load();
        previewPlayer = new ChimePlayer();

        bindViews();
        setupDayPicker();
        setupListeners();
        applyTheme();
        updateAllFromDraft();
        scheduleThemeRefresh();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyTheme();
        scheduleThemeRefresh();
        if (preferences.hasSavedAlarm()) {
            AlarmScheduler.scheduleNext(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (previewPlayer != null) previewPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        themeHandler.removeCallbacks(themeRunnable);
        if (previewPlayer != null) {
            previewPlayer.stop();
            previewPlayer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (sheetVisible) {
            hideSheet();
        } else {
            super.onBackPressed();
        }
    }

    private void bindViews() {
        root = findViewById(R.id.root);
        scroll = findViewById(R.id.scroll);
        content = findViewById(R.id.content);
        title = findViewById(R.id.title);
        clockRing = findViewById(R.id.clockRing);
        timeClickArea = findViewById(R.id.timeClickArea);
        timeText = findViewById(R.id.timeText);
        periodText = findViewById(R.id.periodText);
        dayPicker = findViewById(R.id.dayPicker);

        dividerTop = findViewById(R.id.dividerTop);
        rowSounds = findViewById(R.id.rowSounds);
        soundLabel = findViewById(R.id.soundLabel);
        soundValue = findViewById(R.id.soundValue);
        chevronSounds = findViewById(R.id.chevronSounds);
        dividerSounds = findViewById(R.id.dividerSounds);
        rowRepeat = findViewById(R.id.rowRepeat);
        repeatLabel = findViewById(R.id.repeatLabel);
        repeatValue = findViewById(R.id.repeatValue);
        chevronRepeat = findViewById(R.id.chevronRepeat);
        dividerRepeat = findViewById(R.id.dividerRepeat);
        rowSnooze = findViewById(R.id.rowSnooze);
        snoozeLabel = findViewById(R.id.snoozeLabel);
        toggleSnooze = findViewById(R.id.toggleSnooze);
        dividerSnooze = findViewById(R.id.dividerSnooze);

        resetButton = findViewById(R.id.resetButton);
        saveButton = findViewById(R.id.saveButton);

        scrim = findViewById(R.id.scrim);
        sheetContainer = findViewById(R.id.sheetContainer);
        sheetGrabber = findViewById(R.id.sheetGrabber);
        timeSheet = findViewById(R.id.timeSheet);
        timeSheetTitle = findViewById(R.id.timeSheetTitle);
        timeSheetCancel = findViewById(R.id.timeSheetCancel);
        hourUp = findViewById(R.id.hourUp);
        hourValue = findViewById(R.id.hourValue);
        hourDown = findViewById(R.id.hourDown);
        minuteUp = findViewById(R.id.minuteUp);
        minuteValue = findViewById(R.id.minuteValue);
        minuteDown = findViewById(R.id.minuteDown);
        periodSwitch = findViewById(R.id.periodSwitch);
        periodAm = findViewById(R.id.periodAm);
        periodPm = findViewById(R.id.periodPm);
        timeHint = findViewById(R.id.timeHint);
        setTimeButton = findViewById(R.id.setTimeButton);

        soundSheet = findViewById(R.id.soundSheet);
        soundSheetTitle = findViewById(R.id.soundSheetTitle);
        soundSheetDone = findViewById(R.id.soundSheetDone);
        soundList = findViewById(R.id.soundList);
    }

    private void setupDayPicker() {
        dayPicker.removeAllViews();
        for (int day = 0; day < 7; day++) {
            final int dayIndex = day;
            TextView dayView = new TextView(this);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(32), dp(32));
            params.setMargins(dp(3), 0, dp(3), 0);
            dayView.setLayoutParams(params);
            dayView.setGravity(Gravity.CENTER);
            dayView.setText(TimeUtils.DAY_LETTERS[day]);
            dayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
            dayView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            dayView.setIncludeFontPadding(false);
            dayView.setClickable(true);
            dayView.setFocusable(true);
            dayView.setContentDescription(TimeUtils.DAY_NAMES[day]);
            dayView.setOnClickListener(view -> {
                draft.toggleDay(dayIndex);
                updateDayButtons();
                updateRepeatValue();
            });
            dayButtons[day] = dayView;
            dayPicker.addView(dayView);
        }
    }

    private void setupListeners() {
        timeClickArea.setOnClickListener(view -> openTimeSheet());
        rowSounds.setOnClickListener(view -> openSoundSheet());
        rowRepeat.setOnClickListener(view -> pulseDays());
        toggleSnooze.setOnCheckedChangeListener((view, checked) -> draft.snooze = checked);

        resetButton.setOnClickListener(view -> resetAlarm());
        saveButton.setOnClickListener(view -> saveAlarm());

        scrim.setOnClickListener(view -> hideSheet());
        timeSheetCancel.setOnClickListener(view -> hideSheet());
        soundSheetDone.setOnClickListener(view -> hideSheet());

        hourUp.setOnClickListener(view -> {
            pickerHour = pickerHour % 12 + 1;
            updatePickerUi();
        });
        hourDown.setOnClickListener(view -> {
            pickerHour = pickerHour == 1 ? 12 : pickerHour - 1;
            updatePickerUi();
        });
        minuteUp.setOnClickListener(view -> {
            pickerMinute = (pickerMinute + 5) % 60;
            updatePickerUi();
        });
        minuteDown.setOnClickListener(view -> {
            pickerMinute = (pickerMinute + 55) % 60;
            updatePickerUi();
        });
        periodAm.setOnClickListener(view -> {
            pickerPm = false;
            updatePickerUi();
        });
        periodPm.setOnClickListener(view -> {
            pickerPm = true;
            updatePickerUi();
        });
        setTimeButton.setOnClickListener(view -> {
            draft.time = TimeUtils.to24Hour(pickerHour, pickerMinute, pickerPm);
            updateTimeText();
            hideSheet();
        });
    }

    private void updateAllFromDraft() {
        updateTimeText();
        updateSoundValue();
        updateRepeatValue();
        updateDayButtons();
        toggleSnooze.setChecked(draft.snooze, false);
    }

    private void updateTimeText() {
        TimeUtils.TimeParts parts = TimeUtils.formatTime(draft.time);
        timeText.setText(String.format(Locale.US, "%d:%02d", parts.hour, parts.minute));
        periodText.setText(parts.period);
    }

    private void updateSoundValue() {
        soundValue.setText(draft.sound);
    }

    private void updateRepeatValue() {
        repeatValue.setText(TimeUtils.formatDays(this, draft.daysMask));
    }

    private void updateDayButtons() {
        if (palette == null) return;
        for (int day = 0; day < dayButtons.length; day++) {
            TextView button = dayButtons[day];
            if (button == null) continue;
            boolean selected = draft.isDaySelected(day);
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(selected ? palette.accent : Color.TRANSPARENT);
            background.setStroke(dp(1.5f), selected ? palette.accent : palette.dayBorder);
            button.setBackground(background);
            button.setTextColor(selected ? palette.daySelectedText : palette.dayText);
        }
    }

    private void pulseDays() {
        dayPicker.animate().cancel();
        dayPicker.animate()
                .scaleX(1.04f)
                .scaleY(1.04f)
                .setDuration(130L)
                .withEndAction(() -> dayPicker.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(180L)
                        .start())
                .start();
    }

    private void openTimeSheet() {
        TimeUtils.TimeParts parts = TimeUtils.formatTime(draft.time);
        pickerHour = parts.hour;
        pickerMinute = parts.minute;
        pickerPm = "PM".equals(parts.period);
        updatePickerUi();
        showSheet(timeSheet);
    }

    private void openSoundSheet() {
        buildSoundRows();
        showSheet(soundSheet);
    }

    private void updatePickerUi() {
        hourValue.setText(String.format(Locale.US, "%02d", pickerHour));
        minuteValue.setText(String.format(Locale.US, "%02d", pickerMinute));
        applyPeriodStyles();
    }

    private void applyPeriodStyles() {
        if (palette == null) return;
        stylePeriodButton(periodAm, !pickerPm);
        stylePeriodButton(periodPm, pickerPm);
    }

    private void stylePeriodButton(TextView view, boolean active) {
        view.setTextColor(active ? palette.saveText : palette.muted);
        if (active) {
            view.setBackground(rounded(palette.accent, 9f));
        } else {
            view.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void showSheet(View sheet) {
        if (sheet == null) return;

        activeSheet = sheet;
        timeSheet.setVisibility(sheet == timeSheet ? View.VISIBLE : View.GONE);
        soundSheet.setVisibility(sheet == soundSheet ? View.VISIBLE : View.GONE);

        if (sheetVisible) return;
        sheetVisible = true;

        scrim.animate().cancel();
        sheetContainer.animate().cancel();
        scrim.setVisibility(View.VISIBLE);
        scrim.setAlpha(0f);
        sheetContainer.setVisibility(View.VISIBLE);
        sheetContainer.setTranslationY(dp(42));

        scrim.animate().alpha(1f).setDuration(180L).start();
        sheetContainer.post(() -> sheetContainer.animate()
                .translationY(0f)
                .setDuration(280L)
                .start());
    }

    private void hideSheet() {
        if (!sheetVisible) return;
        sheetVisible = false;

        if (previewPlayer != null) previewPlayer.stop();

        scrim.animate().cancel();
        sheetContainer.animate().cancel();
        scrim.animate()
                .alpha(0f)
                .setDuration(180L)
                .withEndAction(() -> scrim.setVisibility(View.GONE))
                .start();
        sheetContainer.animate()
                .translationY(Math.max(sheetContainer.getHeight(), dp(42)))
                .setDuration(220L)
                .withEndAction(() -> {
                    sheetContainer.setVisibility(View.GONE);
                    activeSheet = null;
                })
                .start();
    }

    private void buildSoundRows() {
        if (palette == null) return;
        soundList.removeAllViews();

        String[] names = {
                getString(R.string.sound_slow_rise),
                getString(R.string.sound_night_owl),
                getString(R.string.sound_morning_dew),
                getString(R.string.sound_soft_chime)
        };
        String[] descriptions = {
                getString(R.string.sound_slow_rise_desc),
                getString(R.string.sound_night_owl_desc),
                getString(R.string.sound_morning_dew_desc),
                getString(R.string.sound_soft_chime_desc)
        };

        for (int index = 0; index < names.length; index++) {
            final String name = names[index];
            boolean selected = name.equals(draft.sound);

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(10), dp(10), dp(10), dp(10));
            row.setMinimumHeight(dp(52));
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackground(selected
                    ? rounded(palette.accentFaint, 14f)
                    : rounded(Color.TRANSPARENT, 14f));

            LinearLayout copy = new LinearLayout(this);
            copy.setOrientation(LinearLayout.VERTICAL);

            TextView nameView = new TextView(this);
            nameView.setText(name);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            nameView.setTextColor(palette.text);
            nameView.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            nameView.setIncludeFontPadding(false);

            TextView descriptionView = new TextView(this);
            descriptionView.setText(descriptions[index]);
            descriptionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9.5f);
            descriptionView.setTextColor(palette.muted);
            descriptionView.setIncludeFontPadding(false);
            LinearLayout.LayoutParams descriptionParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            descriptionParams.topMargin = dp(2);
            descriptionView.setLayoutParams(descriptionParams);

            copy.addView(nameView);
            copy.addView(descriptionView);
            row.addView(copy, new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            ));

            if (selected) {
                TextView check = new TextView(this);
                check.setText("✓");
                check.setGravity(Gravity.CENTER);
                check.setTextColor(palette.saveText);
                check.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f);
                check.setTypeface(Typeface.DEFAULT_BOLD);
                check.setBackground(rounded(palette.accent, 999f));
                row.addView(check, new LinearLayout.LayoutParams(dp(25), dp(25)));
            }

            row.setOnClickListener(view -> {
                draft.sound = name;
                updateSoundValue();
                buildSoundRows();
                if (previewPlayer != null) previewPlayer.start(name, false);
            });

            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            rowParams.bottomMargin = dp(3);
            soundList.addView(row, rowParams);
        }
    }

    private void saveAlarm() {
        if (previewPlayer != null) previewPlayer.stop();
        preferences.save(draft);
        AlarmScheduler.cancelSnooze(this);
        boolean exactScheduled = AlarmScheduler.scheduleNext(this);

        requestNotificationPermissionIfNeeded();
        if (draft.hasAnyDay() && !exactScheduled) {
            promptForExactAlarmPermission();
        }

        Toast.makeText(this, R.string.alarm_saved, Toast.LENGTH_SHORT).show();
    }

    private void resetAlarm() {
        draft = AlarmPreferences.defaults();
        updateAllFromDraft();
        Toast.makeText(this, R.string.changes_reset, Toast.LENGTH_SHORT).show();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        }
        requestPermissions(
                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_NOTIFICATION_PERMISSION
        );
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_NOTIFICATION_PERMISSION
                && grantResults.length > 0
                && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(
                    this,
                    R.string.notification_permission_denied,
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void promptForExactAlarmPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return;
        if (AlarmScheduler.canScheduleExactAlarms(this)) return;

        Toast.makeText(this, R.string.exact_alarm_message, Toast.LENGTH_LONG).show();
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            intent.setData(Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    private void applyTheme() {
        palette = ThemePalette.forNow();

        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{palette.backgroundTop, palette.backgroundBottom}
        );
        root.setBackground(background);
        scrim.setBackgroundColor(palette.scrim);

        title.setTextColor(palette.text);
        timeText.setTextColor(palette.text);
        periodText.setTextColor(palette.text);
        clockRing.setPalette(
                palette.dark,
                palette.accent,
                palette.track,
                palette.backgroundTop
        );

        soundLabel.setTextColor(palette.label);
        repeatLabel.setTextColor(palette.label);
        snoozeLabel.setTextColor(palette.label);
        soundValue.setTextColor(palette.muted);
        repeatValue.setTextColor(palette.muted);
        chevronSounds.setColorFilter(palette.muted);
        chevronRepeat.setColorFilter(palette.muted);

        dividerTop.setBackgroundColor(palette.line);
        dividerSounds.setBackgroundColor(palette.line);
        dividerRepeat.setBackgroundColor(palette.line);
        dividerSnooze.setBackgroundColor(palette.line);

        toggleSnooze.setPalette(palette.accent, palette.toggleOff);

        resetButton.setTextColor(palette.accentMuted);
        saveButton.setTextColor(palette.saveText);
        saveButton.setBackground(rounded(palette.accent, 999f));

        sheetContainer.setBackground(topRounded(palette.sheetBackground, 29f));
        sheetGrabber.setBackground(rounded(withAlpha(palette.muted, 90), 99f));
        timeSheetTitle.setTextColor(palette.text);
        timeSheetCancel.setTextColor(palette.accent);
        soundSheetTitle.setTextColor(palette.text);
        soundSheetDone.setTextColor(palette.accent);

        int stepperColor = palette.soft;
        hourUp.setBackground(rounded(stepperColor, 9f));
        hourDown.setBackground(rounded(stepperColor, 9f));
        minuteUp.setBackground(rounded(stepperColor, 9f));
        minuteDown.setBackground(rounded(stepperColor, 9f));
        hourUp.setColorFilter(palette.muted);
        hourDown.setColorFilter(palette.muted);
        minuteUp.setColorFilter(palette.muted);
        minuteDown.setColorFilter(palette.muted);

        hourValue.setTextColor(palette.text);
        minuteValue.setTextColor(palette.text);
        periodSwitch.setBackground(rounded(palette.soft, 12f));
        timeHint.setTextColor(palette.muted);
        setTimeButton.setTextColor(palette.saveText);
        setTimeButton.setBackground(rounded(palette.accent, 999f));

        updateDayButtons();
        updatePickerUi();
        if (soundSheet.getVisibility() == View.VISIBLE) {
            buildSoundRows();
        }

        applySystemBars();
    }

    private void applySystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(palette.backgroundTop);
        window.setNavigationBarColor(palette.backgroundBottom);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowInsetsController controller = window.getInsetsController();
            if (controller != null) {
                int mask = WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                        | WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;
                controller.setSystemBarsAppearance(palette.dark ? 0 : mask, mask);
            }
        } else {
            int flags = window.getDecorView().getSystemUiVisibility();
            if (palette.dark) {
                flags &= ~(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            } else {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }
    }

    private void scheduleThemeRefresh() {
        themeHandler.removeCallbacks(themeRunnable);

        Calendar now = Calendar.getInstance();
        Calendar next = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        if (hour < 6) {
            next.set(Calendar.HOUR_OF_DAY, 6);
        } else if (hour < 18) {
            next.set(Calendar.HOUR_OF_DAY, 18);
        } else {
            next.add(Calendar.DAY_OF_YEAR, 1);
            next.set(Calendar.HOUR_OF_DAY, 6);
        }
        next.set(Calendar.MINUTE, 0);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);

        long delay = Math.max(1_000L, next.getTimeInMillis() - System.currentTimeMillis());
        themeHandler.postDelayed(themeRunnable, delay);
    }

    private GradientDrawable rounded(int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable topRounded(int color, float radiusDp) {
        float radius = dp(radiusDp);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadii(new float[]{
                radius, radius,
                radius, radius,
                0f, 0f,
                0f, 0f
        });
        return drawable;
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
