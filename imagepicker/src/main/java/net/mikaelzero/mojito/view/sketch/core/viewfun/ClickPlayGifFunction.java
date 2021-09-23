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

package net.mikaelzero.mojito.view.sketch.core.viewfun;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.drawable.SketchGifDrawable;
import net.mikaelzero.mojito.view.sketch.core.request.DisplayOptions;
import net.mikaelzero.mojito.view.sketch.core.request.RedisplayListener;
import net.mikaelzero.mojito.view.sketch.core.state.OldStateImage;
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;


/**
 * 点击播放 gif 功能
 */
@SuppressWarnings("WeakerAccess")
public class ClickPlayGifFunction extends ViewFunction {
    @NonNull
    private FunctionCallbackView view;
    @Nullable
    private Drawable playIconDrawable;

    private boolean canClickPlay;
    @Nullable
    private Drawable lastDrawable;
    private int cacheViewWidth;
    private int cacheViewHeight;
    private int iconDrawLeft;
    private int iconDrawTop;

    @Nullable
    private PlayGifRedisplayListener redisplayListener;

    public ClickPlayGifFunction(@NonNull FunctionCallbackView view) {
        this.view = view;
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        Drawable drawable = view.getDrawable();
        if (drawable != lastDrawable) {
            canClickPlay = canClickPlay(drawable);
            lastDrawable = drawable;
        }

        if (!canClickPlay) {
            return;
        }

        if (cacheViewWidth != view.getWidth() || cacheViewHeight != view.getHeight()) {
            cacheViewWidth = view.getWidth();
            cacheViewHeight = view.getHeight();
            int availableWidth = view.getWidth() - view.getPaddingLeft() - view.getPaddingRight() - playIconDrawable.getBounds().width();
            int availableHeight = view.getHeight() - view.getPaddingTop() - view.getPaddingBottom() - playIconDrawable.getBounds().height();
            iconDrawLeft = view.getPaddingLeft() + (availableWidth / 2);
            iconDrawTop = view.getPaddingTop() + (availableHeight / 2);
        }

        canvas.save();
        canvas.translate(iconDrawLeft, iconDrawTop);
        playIconDrawable.draw(canvas);
        canvas.restore();
    }

    /**
     * 点击事件
     *
     * @param v View
     * @return true：已经消费了，不必往下传了
     */
    public boolean onClick(@SuppressWarnings("UnusedParameters") View v) {
        if (isClickable()) {
            if (redisplayListener == null) {
                redisplayListener = new PlayGifRedisplayListener();
            }
            view.redisplay(redisplayListener);
            return true;
        }
        return false;
    }

    public boolean isClickable() {
        return canClickPlay;
    }

    private boolean canClickPlay(Drawable newDrawable) {
        if (newDrawable == null) {
            return false;
        }
        Drawable endDrawable = SketchUtils.getLastDrawable(newDrawable);
        return SketchUtils.isGifImage(endDrawable) && !(endDrawable instanceof SketchGifDrawable);
    }

    public boolean setPlayIconDrawable(@NonNull Drawable playIconDrawable) {
        if (this.playIconDrawable == playIconDrawable) {
            return false;
        }

        this.playIconDrawable = playIconDrawable;
        this.playIconDrawable.setBounds(0, 0, playIconDrawable.getIntrinsicWidth(), playIconDrawable.getIntrinsicHeight());
        return true;
    }

    private static class PlayGifRedisplayListener implements RedisplayListener {

        @Override
        public void onPreCommit(@NonNull String cacheUri, @NonNull DisplayOptions cacheOptions) {
            cacheOptions.setLoadingImage(new OldStateImage());
            cacheOptions.setDecodeGifImage(true);
        }
    }
}
