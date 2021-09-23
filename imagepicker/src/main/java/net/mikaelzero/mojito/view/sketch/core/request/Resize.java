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

package net.mikaelzero.mojito.view.sketch.core.request;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.Key;

import java.util.Locale;

/**
 * 将图片加载到内存中之后根据 {@link Resize} 进行调整尺寸
 * <p>
 * 修正的原则就是最终返回的图片的比例一定是跟 {@link Resize} 一样的，但尺寸小于等于 {@link Resize} ，如果需要必须同 {@link Resize} 一致可以设置 {@link Mode#ASPECT_RATIO_SAME}
 */
public class Resize implements Key {

    private int width;
    private int height;
    @NonNull
    private Mode mode = Mode.ASPECT_RATIO_SAME;
    @Nullable
    private ImageView.ScaleType scaleType;

    public Resize(int width, int height, @Nullable ImageView.ScaleType scaleType, @Nullable Mode mode) {
        this.width = width;
        this.height = height;
        this.scaleType = scaleType;
        if (mode != null) {
            this.mode = mode;
        }
    }

    public Resize(int width, int height, @Nullable ImageView.ScaleType scaleType) {
        this.width = width;
        this.height = height;
        this.scaleType = scaleType;
    }

    public Resize(int width, int height, @Nullable Mode mode) {
        this.width = width;
        this.height = height;
        if (mode != null) {
            this.mode = mode;
        }
    }

    public Resize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Resize(@NonNull Resize sourceResize) {
        this.width = sourceResize.width;
        this.height = sourceResize.height;
        this.scaleType = sourceResize.scaleType;
        this.mode = sourceResize.mode;
    }

    private Resize() {
    }

    /**
     * 使用 {@link ImageView} 的固定尺寸作为 {@link Resize}
     */
    public static Resize byViewFixedSize(@NonNull Mode mode) {
        if (mode == Mode.EXACTLY_SAME) {
            return ByViewFixedSizeResize.INSTANCE_EXACTLY_SAME;
        } else {
            return ByViewFixedSizeResize.INSTANCE;
        }
    }

    /**
     * 使用 {@link ImageView} 的固定尺寸作为 {@link Resize}
     */
    @NonNull
    public static Resize byViewFixedSize() {
        return ByViewFixedSizeResize.INSTANCE;
    }

    @Nullable
    public ImageView.ScaleType getScaleType() {
        return scaleType;
    }

    void setScaleType(@Nullable ImageView.ScaleType scaleType) {
        this.scaleType = scaleType;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof Resize) {
            Resize other = (Resize) obj;
            return width == other.width && height == other.height && scaleType == other.scaleType;
        }
        return false;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "Resize(%dx%d-%s-%s)", width, height, scaleType != null ? scaleType.name() : "null", mode.name());
    }

    @NonNull
    public Mode getMode() {
        return mode;
    }

    @Nullable
    @Override
    public String getKey() {
        return toString();
    }

    public enum Mode {
        /**
         * 新图片的尺寸不会比 {@link Resize} 大，但宽高比一定会一样
         */
        ASPECT_RATIO_SAME,

        /**
         * 即使原图尺寸比 {@link Resize} 小，也会得到一个跟 {@link Resize} 尺寸一样的 {@link android.graphics.Bitmap}
         */
        EXACTLY_SAME,
    }

    /**
     * 使用 {@link ImageView} 的固定尺寸作为 {@link Resize}
     */
    static class ByViewFixedSizeResize extends Resize {
        static ByViewFixedSizeResize INSTANCE = new ByViewFixedSizeResize();
        static ByViewFixedSizeResize INSTANCE_EXACTLY_SAME = new ByViewFixedSizeResize(Mode.EXACTLY_SAME);

        private ByViewFixedSizeResize(@NonNull Mode mode) {
            super(0, 0, null, mode);
        }

        private ByViewFixedSizeResize() {
        }
    }
}
