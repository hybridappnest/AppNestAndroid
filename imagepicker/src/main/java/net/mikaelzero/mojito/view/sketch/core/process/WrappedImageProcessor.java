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

package net.mikaelzero.mojito.view.sketch.core.process;

import android.graphics.Bitmap;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPool;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPoolUtils;
import net.mikaelzero.mojito.view.sketch.core.request.Resize;

/**
 * 用于组合两个 {@link ImageProcessor} 一起使用，可以无限嵌套
 */
@SuppressWarnings("WeakerAccess")
public abstract class WrappedImageProcessor extends ResizeImageProcessor {
    @Nullable
    private WrappedImageProcessor wrappedProcessor;

    protected WrappedImageProcessor(@Nullable WrappedImageProcessor wrappedProcessor) {
        this.wrappedProcessor = wrappedProcessor;
    }

    @NonNull
    @Override
    public final Bitmap process(@NonNull Sketch sketch, @NonNull Bitmap bitmap, @Nullable Resize resize, boolean lowQualityImage) {
        //noinspection ConstantConditions
        if (bitmap == null || bitmap.isRecycled()) {
            return bitmap;
        }

        // resize
        Bitmap newBitmap = bitmap;
        if (!isInterceptResize()) {
            newBitmap = super.process(sketch, bitmap, resize, lowQualityImage);
        }

        // wrapped
        if (wrappedProcessor != null) {
            Bitmap wrappedBitmap = wrappedProcessor.process(sketch, newBitmap, resize, lowQualityImage);
            if (wrappedBitmap != newBitmap) {
                if (newBitmap != bitmap) {
                    BitmapPool bitmapPool = sketch.getConfiguration().getBitmapPool();
                    BitmapPoolUtils.freeBitmapToPool(newBitmap, bitmapPool);
                }
                newBitmap = wrappedBitmap;
            }
        }
        return onProcess(sketch, newBitmap, resize, lowQualityImage);
    }

    @NonNull
    public abstract Bitmap onProcess(@NonNull Sketch sketch, @NonNull Bitmap bitmap, @Nullable Resize resize, boolean lowQualityImage);

    @Nullable
    public WrappedImageProcessor getWrappedProcessor() {
        return wrappedProcessor;
    }

    @Nullable
    @Override
    public String getKey() {
        String selfKey = onGetKey();
        String wrappedKey = wrappedProcessor != null ? wrappedProcessor.getKey() : null;

        if (!TextUtils.isEmpty(selfKey)) {
            if (!TextUtils.isEmpty(wrappedKey)) {
                return String.format("%s->%s", selfKey, wrappedKey);
            } else {
                return selfKey;
            }
        } else if (!TextUtils.isEmpty(wrappedKey)) {
            return wrappedKey;
        }
        return null;
    }

    @Nullable
    public abstract String onGetKey();

    @NonNull
    @Override
    public String toString() {
        String selfToString = onToString();
        String wrappedToString = wrappedProcessor != null ? wrappedProcessor.toString() : null;

        if (TextUtils.isEmpty(wrappedToString)) {
            return selfToString;
        }
        return String.format("%s->%s", selfToString, wrappedToString);
    }

    @NonNull
    public abstract String onToString();

    protected boolean isInterceptResize() {
        return false;
    }
}
