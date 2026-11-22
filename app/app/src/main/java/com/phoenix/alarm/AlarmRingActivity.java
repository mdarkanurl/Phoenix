package com.phoenix.alarm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

public class AlarmRingActivity extends Activity {

    public static final String EXTRA_FROM_ALARM = "from_alarm";

    private View ringRoot;
    private View ringGlow;
    private LinearLayout ringCard;
    private View ringDot;
    private TextView ringStatus;
    private TextView ringTime;
    private TextView ringPeriod;
    private TextView ringName;
    private TextView ringDetail;
    private Button ringSnoozeButton;
    private Button ringStopButton;

    private ThemePalette palette;
    private boolean receiverRegistered;

    private final BroadcastReceiver ringingStoppedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            );
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_alarm_ring);
        bindViews();
        applyTheme();

        AlarmPreferences.AlarmData data = new AlarmPreferences(this).load();
        bindAlarmData(data);
        setupButtons();

        if (getIntent().getBooleanExtra(EXTRA_FROM_ALARM, false)) {
            Intent serviceIntent = new Intent(this, AlarmService.class);
            serviceIntent.setAction(AlarmService.ACTION_START);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent);
            } else {
                startService(serviceIntent);
            }
        }

        registerRingingReceiver();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        AlarmPreferences.AlarmData data = new AlarmPreferences(this).load();
        bindAlarmData(data);
    }

    @Override
    protected void onDestroy() {
        if (receiverRegistered) {
            unregisterReceiver(ringingStoppedReceiver);
            receiverRegistered = false;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        stopAlarm();
    }

    private void bindViews() {
        ringRoot = findViewById(R.id.ringRoot);
        ringGlow = findViewById(R.id.ringGlow);
        ringCard = findViewById(R.id.ringCard);
        ringDot = findViewById(R.id.ringDot);
        ringStatus = findViewById(R.id.ringStatus);
        ringTime = findViewById(R.id.ringTime);
        ringPeriod = findViewById(R.id.ringPeriod);
        ringName = findViewById(R.id.ringName);
        ringDetail = findViewById(R.id.ringDetail);
        ringSnoozeButton = findViewById(R.id.ringSnoozeButton);
        ringStopButton = findViewById(R.id.ringStopButton);
    }

    private void bindAlarmData(AlarmPreferences.AlarmData data) {
        TimeUtils.TimeParts parts = TimeUtils.formatTime(data.time);
        ringTime.setText(String.format(Locale.US, "%d:%02d", parts.hour, parts.minute));
        ringPeriod.setText(parts.period);
        ringName.setText(R.string.alarm);
        ringDetail.setText(
                data.sound + " · " + TimeUtils.formatDays(this, data.daysMask)
        );
        ringSnoozeButton.setVisibility(data.snooze ? View.VISIBLE : View.GONE);
    }

    private void setupButtons() {
        ringStopButton.setOnClickListener(view -> stopAlarm());
        ringSnoozeButton.setOnClickListener(view -> snoozeAlarm());
    }

    private void stopAlarm() {
        stopService(new Intent(this, AlarmService.class));
        finish();
    }

    private void snoozeAlarm() {
        AlarmScheduler.scheduleSnooze(this, AlarmReceiver.SNOOZE_DURATION_MS);
        stopService(new Intent(this, AlarmService.class));
        finish();
    }

    private void applyTheme() {
        palette = ThemePalette.forNow();

        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{palette.backgroundTop, palette.backgroundBottom}
        );
        ringRoot.setBackground(background);

        GradientDrawable glow = new GradientDrawable();
        glow.setShape(GradientDrawable.OVAL);
        glow.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        glow.setColors(new int[]{
                withAlpha(palette.accent, 58),
                Color.TRANSPARENT
        });
        glow.setGradientRadius(dp(190));
        ringGlow.setBackground(glow);

        ringCard.setBackground(rounded(palette.sheetBackground, 40f));
        ringCard.setElevation(dp(24));

        ringDot.setBackground(oval(palette.accent));
        ringStatus.setTextColor(palette.accent);
        ringTime.setTextColor(palette.text);
        ringPeriod.setTextColor(palette.ringMuted);
        ringName.setTextColor(palette.text);
        ringDetail.setTextColor(palette.ringMuted);

        ringSnoozeButton.setTextColor(palette.text);
        ringSnoozeButton.setBackground(outlined(palette.line, 999f));
        ringStopButton.setTextColor(palette.saveText);
        ringStopButton.setBackground(rounded(palette.accent, 999f));

        int cardWidth = Math.min(dp(390), getResources().getDisplayMetrics().widthPixels - dp(48));
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) ringCard.getLayoutParams();
        params.width = Math.max(dp(280), cardWidth);
        ringCard.setLayoutParams(params);
    }

    private void registerRingingReceiver() {
        IntentFilter filter = new IntentFilter(AlarmService.ACTION_RINGING_STOPPED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(ringingStoppedReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(ringingStoppedReceiver, filter);
        }
        receiverRegistered = true;
    }

    private GradientDrawable rounded(int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private GradientDrawable outlined(int color, float radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.TRANSPARENT);
        drawable.setCornerRadius(dp(radiusDp));
        drawable.setStroke(dp(1), color);
        return drawable;
    }

    private GradientDrawable oval(int color) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        return drawable;
    }

    private int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
