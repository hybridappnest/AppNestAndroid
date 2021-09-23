/*
 * Copyright (C) 2019 Peng fei Pan <panpfpanpf@outlook.me>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.mikaelzero.mojito.view.sketch.core.util;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DrawableWrapper extends Drawable implements Drawable.Callback {

    @Nullable
    private Drawable wrappedDrawable;

    public DrawableWrapper(@Nullable Drawable drawable) {
        setWrappedDrawable(drawable);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        if (wrappedDrawable != null) {
            wrappedDrawable.draw(canvas);
        }
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        if (wrappedDrawable != null) {
            wrappedDrawable.setBounds(bounds);
        }
    }

    @Override
    public int getChangingConfigurations() {
        return wrappedDrawable != null ? wrappedDrawable.getChangingConfigurations() : super.getChangingConfigurations();
    }

    @Override
    public void setChangingConfigurations(int configs) {
        if (wrappedDrawable != null) {
            wrappedDrawable.setChangingConfigurations(configs);
        }
    }

    @Override
    public void setDither(boolean dither) {
        if (wrappedDrawable != null) {
            wrappedDrawable.setDither(dither);
        }
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        if (wrappedDrawable != null) {
            wrappedDrawable.setFilterBitmap(filter);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        if (wrappedDrawable != null) {
            wrappedDrawable.setAlpha(alpha);
        }
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        if (wrappedDrawable != null) {
            wrappedDrawable.setColorFilter(cf);
        }
    }

    @Override
    public boolean isStateful() {
        return wrappedDrawable != null ? wrappedDrawable.isStateful() : super.isStateful();
    }

    @Override
    public boolean setState(@NonNull final int[] stateSet) {
        return wrappedDrawable != null ? wrappedDrawable.setState(stateSet) : super.setState(stateSet);
    }

    @NonNull
    @Override
    public int[] getState() {
        return wrappedDrawable != null ? wrappedDrawable.getState() : super.getState();
    }

    @Override
    public void jumpToCurrentState() {
        if (wrappedDrawable != null) {
            wrappedDrawable.jumpToCurrentState();
        }
    }

    @NonNull
    @Override
    public Drawable getCurrent() {
        return wrappedDrawable != null ? wrappedDrawable.getCurrent() : super.getCurrent();
    }

    @Override
    public boolean setVisible(boolean visible, boolean restart) {
        return super.setVisible(visible, restart) || (wrappedDrawable != null && wrappedDrawable.setVisible(visible, restart));
    }

    @Override
    public int getOpacity() {
        return wrappedDrawable != null ? wrappedDrawable.getOpacity() : PixelFormat.UNKNOWN;
    }

    @Override
    public Region getTransparentRegion() {
        return wrappedDrawable != null ? wrappedDrawable.getTransparentRegion() : super.getTransparentRegion();
    }

    @Override
    public int getIntrinsicWidth() {
        return wrappedDrawable != null ? wrappedDrawable.getIntrinsicWidth() : super.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return wrappedDrawable != null ? wrappedDrawable.getIntrinsicHeight() : super.getIntrinsicHeight();
    }

    @Override
    public int getMinimumWidth() {
        return wrappedDrawable != null ? wrappedDrawable.getMinimumWidth() : super.getMinimumWidth();
    }

    @Override
    public int getMinimumHeight() {
        return wrappedDrawable != null ? wrappedDrawable.getMinimumHeight() : super.getMinimumHeight();
    }

    @Override
    public boolean getPadding(@NonNull Rect padding) {
        return wrappedDrawable != null ? wrappedDrawable.getPadding(padding) : super.getPadding(padding);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return wrappedDrawable != null ? wrappedDrawable.setLevel(level) : super.onLevelChange(level);
    }

    @Override
    public boolean isAutoMirrored() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && wrappedDrawable != null && wrappedDrawable.isAutoMirrored();
    }

    @Override
    public void setAutoMirrored(boolean mirrored) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && wrappedDrawable != null) {
            wrappedDrawable.setAutoMirrored(mirrored);
        }
    }

    @Override
    public void setTint(int tint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && wrappedDrawable != null) {
            wrappedDrawable.setTint(tint);
        }
    }

    @Override
    public void setTintList(ColorStateList tint) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && wrappedDrawable != null) {
            wrappedDrawable.setTintList(tint);
        }
    }

    @Override
    public void setTintMode(@NonNull PorterDuff.Mode tintMode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && wrappedDrawable != null) {
            wrappedDrawable.setTintMode(tintMode);
        }
    }

    @Override
    public void setHotspot(float x, float y) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && wrappedDrawable != null) {
            wrappedDrawable.setHotspot(x, y);
        }
    }

    @Override
    public void setHotspotBounds(int left, int top, int right, int bottom) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && wrappedDrawable != null) {
            wrappedDrawable.setHotspotBounds(left, top, right, bottom);
        }
    }


    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        invalidateSelf();
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        scheduleSelf(what, when);
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        unscheduleSelf(what);
    }


    @Nullable
    public Drawable getWrappedDrawable() {
        return wrappedDrawable;
    }

    public void setWrappedDrawable(@Nullable Drawable drawable) {
        if (drawable == this) {
            return;
        }

        if (wrappedDrawable != null) {
            wrappedDrawable.setCallback(null);
        }

        this.wrappedDrawable = drawable;

        if (wrappedDrawable != null) {
            wrappedDrawable.setCallback(this);
        }
    }
}