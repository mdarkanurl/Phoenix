package com.phoenix.alarm.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public class ToggleView extends View {

    public interface OnCheckedChangeListener {
        void onCheckedChanged(ToggleView view, boolean checked);
    }

    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint thumbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF trackBounds = new RectF();

    private boolean checked;
    private float progress;
    private int accentColor = Color.rgb(245, 141, 130);
    private int offColor = Color.rgb(237, 231, 226);
    private OnCheckedChangeListener listener;
    private ValueAnimator animator;

    public ToggleView(Context context) {
        this(context, null);
    }

    public ToggleView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ToggleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setClickable(true);
        setFocusable(true);
        thumbPaint.setColor(Color.WHITE);
        thumbPaint.setShadowLayer(dp(2f), 0f, dp(1f), Color.argb(55, 0, 0, 0));
        setWillNotDraw(false);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    public void setPalette(int accent, int off) {
        accentColor = accent;
        offColor = off;
        invalidate();
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener listener) {
        this.listener = listener;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked, boolean animate) {
        if (this.checked == checked) {
            if (!animate) {
                progress = checked ? 1f : 0f;
                invalidate();
            }
            return;
        }
        this.checked = checked;
        float target = checked ? 1f : 0f;

        if (animator != null) animator.cancel();

        if (!animate) {
            progress = target;
            invalidate();
            return;
        }

        animator = ValueAnimator.ofFloat(progress, target);
        animator.setDuration(220L);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float inset = dp(3f);
        float trackHeight = dp(18f);
        float trackLeft = inset;
        float trackRight = getWidth() - inset;
        float trackTop = (getHeight() - trackHeight) / 2f;
        float trackBottom = trackTop + trackHeight;
        float radius = trackHeight / 2f;

        trackBounds.set(trackLeft, trackTop, trackRight, trackBottom);
        trackPaint.setColor(blend(offColor, accentColor, progress));
        canvas.drawRoundRect(trackBounds, radius, radius, trackPaint);

        float thumbRadius = dp(8f);
        float travelStart = trackLeft + thumbRadius + dp(0.5f);
        float travelEnd = trackRight - thumbRadius - dp(0.5f);
        float thumbX = travelStart + (travelEnd - travelStart) * progress;
        canvas.drawCircle(thumbX, trackBounds.centerY(), thumbRadius, thumbPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                setPressed(true);
                return true;
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                return true;
            case MotionEvent.ACTION_UP:
                setPressed(false);
                if (event.getX() >= 0 && event.getX() <= getWidth()
                        && event.getY() >= 0 && event.getY() <= getHeight()) {
                    performClick();
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    @Override
    public boolean performClick() {
        super.performClick();
        setChecked(!checked, true);
        if (listener != null) listener.onCheckedChanged(this, checked);
        return true;
    }

    private int blend(int from, int to, float fraction) {
        float t = Math.max(0f, Math.min(1f, fraction));
        int alpha = (int) (Color.alpha(from) + (Color.alpha(to) - Color.alpha(from)) * t);
        int red = (int) (Color.red(from) + (Color.red(to) - Color.red(from)) * t);
        int green = (int) (Color.green(from) + (Color.green(to) - Color.green(from)) * t);
        int blue = (int) (Color.blue(from) + (Color.blue(to) - Color.blue(from)) * t);
        return Color.argb(alpha, red, green, blue);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
