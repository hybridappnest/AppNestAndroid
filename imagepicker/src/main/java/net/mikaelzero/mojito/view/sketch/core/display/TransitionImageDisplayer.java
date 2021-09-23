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

package net.mikaelzero.mojito.view.sketch.core.display;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;

import androidx.annotation.NonNull;

import net.mikaelzero.mojito.view.sketch.core.SketchView;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchGifDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchLoadingDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchTransitionDrawable;
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;

import java.util.Locale;

/**
 * 过渡效果的图片显示器
 */
public class TransitionImageDisplayer implements ImageDisplayer {
    private static final String KEY = "TransitionImageDisplayer";

    private int duration;
    private boolean alwaysUse;
    private boolean disableCrossFade;

    public TransitionImageDisplayer(int duration, boolean alwaysUse) {
        this.duration = duration;
        this.alwaysUse = alwaysUse;
    }

    public TransitionImageDisplayer(int duration) {
        this(duration, false);
    }

    public TransitionImageDisplayer(boolean alwaysUse) {
        this(DEFAULT_ANIMATION_DURATION, alwaysUse);
    }

    public TransitionImageDisplayer() {
        this(DEFAULT_ANIMATION_DURATION, false);
    }

    @NonNull
    public TransitionImageDisplayer setDisableCrossFade(boolean disableCrossFade) {
        this.disableCrossFade = disableCrossFade;
        return this;
    }

    @Override
    public void display(@NonNull SketchView sketchView, @NonNull Drawable newDrawable) {
        if (newDrawable instanceof SketchGifDrawable) {
            sketchView.clearAnimation();
            sketchView.setImageDrawable(newDrawable);
        } else {
            Drawable oldDrawable = SketchUtils.getLastDrawable(sketchView.getDrawable());
            if (oldDrawable == null) {
                oldDrawable = new ColorDrawable(Color.TRANSPARENT);
            }

            if (oldDrawable instanceof SketchDrawable
                    && !(oldDrawable instanceof SketchLoadingDrawable)
                    && newDrawable instanceof SketchDrawable
                    && ((SketchDrawable) oldDrawable).getKey().equals(((SketchDrawable) newDrawable).getKey())) {
                sketchView.setImageDrawable(newDrawable);
            } else {
                TransitionDrawable transitionDrawable = new SketchTransitionDrawable(oldDrawable, newDrawable);
                sketchView.clearAnimation();
                sketchView.setImageDrawable(transitionDrawable);
                transitionDrawable.setCrossFadeEnabled(!disableCrossFade);
                transitionDrawable.startTransition(duration);
            }
        }
    }

    @Override
    public boolean isAlwaysUse() {
        return alwaysUse;
    }

    /**
     * 获取持续时间，单位毫秒
     */
    public int getDuration() {
        return duration;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%s(duration=%d,alwaysUse=%s)", KEY, duration, alwaysUse);
    }
}
