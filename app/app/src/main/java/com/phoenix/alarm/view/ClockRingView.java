package com.phoenix.alarm.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;

public class ClockRingView extends View {

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knobFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint knobStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint iconTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ringBounds = new RectF();
    private final Path iconPath = new Path();
    private final Path moonOuter = new Path();
    private final Path moonInner = new Path();

    private boolean dark;
    private int accentColor = Color.rgb(245, 141, 130);
    private int trackColor = Color.rgb(242, 236, 231);
    private int backgroundColor = Color.rgb(255, 253, 251);

    private final float strokeWidth;

    public ClockRingView(Context context) {
        this(context, null);
    }

    public ClockRingView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClockRingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        strokeWidth = dp(7.5f);

        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        knobFillPaint.setStyle(Paint.Style.FILL);
        knobStrokePaint.setStyle(Paint.Style.STROKE);
        knobStrokePaint.setStrokeWidth(dp(2.5f));

        iconPaint.setStyle(Paint.Style.STROKE);
        iconPaint.setStrokeCap(Paint.Cap.ROUND);
        iconPaint.setStrokeJoin(Paint.Join.ROUND);
        iconPaint.setStrokeWidth(dp(2.1f));

        iconTextPaint.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        iconTextPaint.setTextAlign(Paint.Align.LEFT);

        setWillNotDraw(false);
    }

    public void setPalette(boolean dark, int accent, int track, int background) {
        this.dark = dark;
        this.accentColor = accent;
        this.trackColor = track;
        this.backgroundColor = background;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        if (MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            width = (int) dp(260);
        }
        setMeasuredDimension(width, width);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float centerX = width / 2f;
        float centerY = height / 2f;
        float radius = Math.min(width, height) / 2f - strokeWidth / 2f - dp(2f);

        ringBounds.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        trackPaint.setColor(trackColor);
        trackPaint.setStrokeWidth(strokeWidth);
        canvas.drawArc(ringBounds, 0f, 360f, false, trackPaint);

        progressPaint.setColor(accentColor);
        progressPaint.setStrokeWidth(strokeWidth);
        canvas.drawArc(ringBounds, 135f, 275.4f, false, progressPaint);

        double startAngle = Math.toRadians(135.0);
        float startX = centerX + radius * (float) Math.cos(startAngle);
        float startY = centerY + radius * (float) Math.sin(startAngle);

        if (dark) {
            drawMoon(canvas, startX, startY);
            drawZzz(canvas, centerX, centerY, radius);
        } else {
            knobFillPaint.setColor(accentColor);
            knobStrokePaint.setColor(backgroundColor);
            canvas.drawCircle(startX, startY, dp(8.2f), knobFillPaint);
            canvas.drawCircle(startX, startY, dp(8.2f), knobStrokePaint);
            drawMusicNote(canvas, centerX, centerY, radius);
        }
    }

    private void drawMusicNote(Canvas canvas, float centerX, float centerY, float radius) {
        float x = centerX + radius * 0.61f;
        float y = centerY - radius * 0.73f;
        float stem = dp(15f);
        float beam = dp(11f);

        iconPaint.setColor(accentColor);
        iconPath.reset();
        iconPath.moveTo(x, y + stem);
        iconPath.lineTo(x, y - dp(4f));
        iconPath.lineTo(x + beam, y - dp(7f));
        iconPath.lineTo(x + beam, y + stem - dp(7f));
        canvas.drawPath(iconPath, iconPaint);

        canvas.drawOval(
                x - dp(4.2f),
                y + stem - dp(4.4f),
                x + dp(2.8f),
                y + stem + dp(3.2f),
                iconPaint
        );
        canvas.drawOval(
                x + beam - dp(4.2f),
                y + stem - dp(7.4f),
                x + beam + dp(2.8f),
                y + stem + dp(0.2f),
                iconPaint
        );
    }

    private void drawZzz(Canvas canvas, float centerX, float centerY, float radius) {
        iconTextPaint.setColor(accentColor);
        iconTextPaint.setTextSize(sp(22f));
        float x = centerX + radius * 0.55f;
        float y = centerY - radius * 0.68f;
        canvas.drawText("z", x, y, iconTextPaint);

        iconTextPaint.setTextSize(sp(13f));
        canvas.drawText("z", x + dp(10f), y - dp(9f), iconTextPaint);
    }

    private void drawMoon(Canvas canvas, float x, float y) {
        float radius = dp(9.2f);
        moonOuter.reset();
        moonInner.reset();
        moonOuter.addCircle(x, y, radius, Path.Direction.CW);
        moonInner.addCircle(x + radius * 0.48f, y - radius * 0.30f, radius * 0.94f, Path.Direction.CW);
        moonOuter.op(moonInner, Path.Op.DIFFERENCE);

        iconPaint.setStyle(Paint.Style.FILL);
        iconPaint.setColor(accentColor);
        canvas.drawPath(moonOuter, iconPaint);
        iconPaint.setStyle(Paint.Style.STROKE);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    private float sp(float value) {
        return value * getResources().getDisplayMetrics().scaledDensity;
    }
}
