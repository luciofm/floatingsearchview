package com.arlib.floatingsearchview.util.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ImageView;

import com.arlib.floatingsearchview.R;

/**
 * Created by luciofm on 04/08/16.
 */

public class BadgeView extends ImageView {
    BadgeDrawable drawable;

    private ViewPropertyAnimatorCompat animator;
    private static final Interpolator INTERPOLATOR = new LinearOutSlowInInterpolator();

    boolean enabled = true;
    boolean autoShowHide;

    public BadgeView(Context context) {
        this(context, null);
    }

    public BadgeView(Context context, AttributeSet attrs) {
        super(context, attrs);

        drawable = new BadgeDrawable();
        drawable.setAutoSetBounds(true);

        if (attrs != null) {
            TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BadgeView);
            try {

                autoShowHide = a.getBoolean(R.styleable.BadgeView_badgeView_autoShowHide, false);

                int color = a.getColor(R.styleable.BadgeView_badgeView_color,
                        BadgeDrawable.DEFAULT_BADGE_COLOR);
                drawable.setBadgeColor(color);

                color = a.getColor(R.styleable.BadgeView_badgeView_textColor,
                        BadgeDrawable.DEFAULT_TEXT_COLOR);
                drawable.setTextColor(color);

                float textSize = a.getDimension(R.styleable.BadgeView_badgeView_textSize,
                        BadgeDrawable.DEFAULT_TEXT_SIZE);
                drawable.setTextSize(textSize);

                int count = a.getInt(R.styleable.BadgeView_badgeView_count, 0);
                drawable.setNumber(count);
                if (autoShowHide && count > 0)
                    setVisibility(VISIBLE);
                else
                    setVisibility(INVISIBLE);
            } finally {
                a.recycle();
            }
        }

        setImageDrawable(drawable);
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewOutlineProvider provider = new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(drawable.getBounds());
                }
            };
            setOutlineProvider(provider);
        }
        ViewCompat.setElevation(this, BadgeDrawable.dipToPixels(2));*/
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled)
            animateIn(0);
        else if (!enabled)
            animateOut();
    }

    public boolean isAutoShowHide() {
        return autoShowHide;
    }

    public void setAutoShowHide(boolean autoShowHide) {
        this.autoShowHide = autoShowHide;
        setCount(drawable.getNumber());
    }

    public void setCount(int count) {
        if (!enabled)
            return;

        int oldCount = drawable.getNumber();
        drawable.setNumber(count);
        if (autoShowHide) {
            autoShowHide(count, oldCount);
        }
    }

    private void autoShowHide(int count, int oldCount) {
        if (count <= 0)
            animateOut();
        else
            animateIn(oldCount);
    }

    private void animateIn(int oldCount) {
        if (oldCount != 0)
            return;
        ensureOrCancelAnimator();
        setScaleX(0f);
        setScaleY(0f);
        setAlpha(0.5f);
        setVisibility(VISIBLE);
        animator.scaleX(1f).scaleY(1f).alpha(1f).start();
    }

    private void animateOut() {
        ensureOrCancelAnimator();
        animator.scaleX(0f).scaleY(0f).alpha(0.5f).start();
    }

    private void ensureOrCancelAnimator() {
        if (animator == null) {
            animator = ViewCompat.animate(this);
            animator.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            animator.setListener(new ViewPropertyAnimatorListenerAdapter(){
                @Override
                public void onAnimationEnd(View view) {
                    if (drawable.getNumber() == 0)
                        setVisibility(INVISIBLE);
                }
            });
        } else {
            animator.cancel();
        }
    }

    public static class BadgeDrawable extends Drawable {

        static final float DEFAULT_TEXT_SIZE = spToPixels(12);

        static final int DEFAULT_BADGE_COLOR = 0xffFF6699;
        static final int DEFAULT_TEXT_COLOR = 0xffFFFFFF;

        private ShapeDrawable backgroundDrawable;
        private int badgeWidth;
        private int badgeHeight;
        private float cornerRadius;
        private float[] outerR = new float[]{0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f};

        private int number = 0;

        private float textSize;
        private int badgeColor;
        private int textColor;
        private Paint paint;
        private Paint.FontMetrics fontMetrics;

        private boolean isAutoSetBounds = false;

        public BadgeDrawable() {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStyle(Paint.Style.FILL);
            paint.setAlpha(255);


            RoundRectShape shape = new RoundRectShape(outerR, null, null);
            backgroundDrawable = new ShapeDrawable(shape);

            setTextSize(DEFAULT_TEXT_SIZE);
            setBadgeColor(DEFAULT_BADGE_COLOR);
            setTextColor(DEFAULT_TEXT_COLOR);
        }

        public void setCornerRadius(float radius) {
            if (cornerRadius != radius) {
                cornerRadius = radius;
                outerR[0] = outerR[1] = outerR[2] = outerR[3] =
                        outerR[4] = outerR[5] = outerR[6] = outerR[7] = cornerRadius;
            }
        }

        public void setBadgeColor(int color) {
            badgeColor = color;
        }

        public int getBadgeColor() {
            return badgeColor;
        }

        public void setTextColor(int color) {
            textColor = color;
        }

        public int getTextColor() {
            return textColor;
        }

        public void setTextSize(float textSize) {
            this.textSize = textSize;
            paint.setTextSize(textSize);
            fontMetrics = paint.getFontMetrics();

            measureBadge();
        }

        public float getTextSize() {
            return textSize;
        }

        public void setNumber(int number) {
            this.number = number;
            measureBadge();
            invalidateSelf();
        }

        public int getNumber() {
            return number;
        }

        public void setAutoSetBounds(boolean autoSetBounds) {
            this.isAutoSetBounds = autoSetBounds;
        }

        private void measureBadge() {
            badgeWidth = badgeHeight = (int) (textSize * 1.5f);
            setCornerRadius(badgeHeight);

            if (isAutoSetBounds)
                setBounds(0, 0, badgeWidth, badgeHeight);
        }

        @Override
        public void draw(Canvas canvas) {
            Rect bounds = getBounds();

            int marginTopAndBottom = (int) ((bounds.height() - badgeHeight) / 2f);
            int marginLeftAndRight = (int) ((bounds.width() - badgeWidth) / 2f);

            backgroundDrawable.setBounds(
                    bounds.left + marginLeftAndRight,
                    bounds.top + marginTopAndBottom,
                    bounds.right - marginLeftAndRight,
                    bounds.bottom - marginTopAndBottom);
            backgroundDrawable.getPaint().setColor(badgeColor);
            backgroundDrawable.draw(canvas);

            float textCx = bounds.centerX();
            float textCy = bounds.centerY();
            float textCyOffset = (-fontMetrics.ascent) / 2f - dipToPixels(1);

            paint.setColor(textColor);
            canvas.drawText(
                    cutNumber(number, badgeWidth),
                    textCx,
                    textCy + textCyOffset,
                    paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
            //invalidateSelf();
        }

        @Override
        public void setColorFilter(ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
            invalidateSelf();
        }

        @Override
        public int getOpacity() {
            return PixelFormat.UNKNOWN;
        }

        private String cutNumber(int number, int width) {
            if (number > 9)
                return "9+";
            return String.valueOf(number);
            /*if (paint.measureText(text) < width)
                return text;

            return "...";*/
        }

        private static float dipToPixels(float dipValue) {
            final float scale = Resources.getSystem().getDisplayMetrics().density;
            return dipValue * scale + 0.5f;
        }

        private static float spToPixels(float spValue) {
            final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
            return spValue * fontScale + 0.5f;
        }
    }
}
