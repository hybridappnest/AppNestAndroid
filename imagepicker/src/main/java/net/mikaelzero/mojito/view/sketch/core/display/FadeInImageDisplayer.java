package net.mikaelzero.mojito.view.sketch.core.display;

import android.graphics.drawable.Drawable;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.NonNull;

import net.mikaelzero.mojito.view.sketch.core.SketchView;

import java.util.Locale;

/**
 * 渐入动画
 */
public class FadeInImageDisplayer implements ImageDisplayer {
    private static final String KEY = "FadeInImageDisplayer";

    private int duration;
    private boolean alwaysUse;

    public FadeInImageDisplayer(int duration, boolean alwaysUse) {
        this.duration = duration;
        this.alwaysUse = alwaysUse;
    }

    public FadeInImageDisplayer(int duration) {
        this(duration, false);
    }

    public FadeInImageDisplayer(boolean alwaysUse) {
        this(DEFAULT_ANIMATION_DURATION, alwaysUse);
    }

    public FadeInImageDisplayer() {
        this(DEFAULT_ANIMATION_DURATION, false);
    }

    @Override
    public void display(@NonNull SketchView sketchView, @NonNull Drawable newDrawable) {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.setDuration(duration);
        sketchView.clearAnimation();
        sketchView.setImageDrawable(newDrawable);
        sketchView.startAnimation(animation);
    }

    @Override
    public int getDuration() {
        return duration;
    }

    @Override
    public boolean isAlwaysUse() {
        return alwaysUse;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%s(duration=%d,alwaysUse=%s)", KEY, duration, alwaysUse);
    }
}
