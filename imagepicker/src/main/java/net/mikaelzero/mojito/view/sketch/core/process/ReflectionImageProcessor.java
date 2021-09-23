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
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPool;
import net.mikaelzero.mojito.view.sketch.core.request.Resize;

import java.util.Locale;

/**
 * 倒影图片处理器
 */
@SuppressWarnings("WeakerAccess")
public class ReflectionImageProcessor extends WrappedImageProcessor {

    private static final int DEFAULT_REFLECTION_SPACING = 2;
    private static final float DEFAULT_REFLECTION_SCALE = 0.3f;

    private int reflectionSpacing;
    private float reflectionScale;

    /**
     * 创建一个倒影图片处理器
     *
     * @param reflectionSpacing     倒影和图片之间的距离
     * @param reflectionScale       倒影的高度所占原图高度比例，取值为 0 到 1
     * @param wrappedImageProcessor 嵌套一个图片处理器
     */
    public ReflectionImageProcessor(int reflectionSpacing, float reflectionScale, @Nullable WrappedImageProcessor wrappedImageProcessor) {
        super(wrappedImageProcessor);
        this.reflectionSpacing = reflectionSpacing;
        this.reflectionScale = reflectionScale;
    }

    /**
     * 创建一个倒影图片处理器
     *
     * @param reflectionSpacing 倒影和图片之间的距离
     * @param reflectionScale   倒影的高度所占原图高度比例，取值为 0 到 1
     */
    public ReflectionImageProcessor(int reflectionSpacing, float reflectionScale) {
        this(reflectionSpacing, reflectionScale, null);
    }

    /**
     * 创建一个倒影图片处理器，默认倒影和图片之间的距离是 2 个像素，倒影的高度所占原图高度比例是 0.3
     *
     * @param wrappedImageProcessor 嵌套一个图片处理器
     */
    public ReflectionImageProcessor(@Nullable WrappedImageProcessor wrappedImageProcessor) {
        this(DEFAULT_REFLECTION_SPACING, DEFAULT_REFLECTION_SCALE, wrappedImageProcessor);
    }

    /**
     * 创建一个倒影图片处理器，默认倒影和图片之间的距离是 2 个像素，倒影的高度所占原图高度比例是 0.3
     */
    public ReflectionImageProcessor() {
        this(DEFAULT_REFLECTION_SPACING, DEFAULT_REFLECTION_SCALE, null);
    }

    @NonNull
    @Override
    public Bitmap onProcess(@NonNull Sketch sketch, @NonNull Bitmap bitmap, @Nullable Resize resize, boolean lowQualityImage) {
        if (bitmap.isRecycled()) {
            return bitmap;
        }

        int srcHeight = bitmap.getHeight();
        int reflectionHeight = (int) (srcHeight * reflectionScale);
        int reflectionTop = srcHeight + reflectionSpacing;

        Bitmap.Config config = lowQualityImage ? Bitmap.Config.ARGB_4444 : Bitmap.Config.ARGB_8888;
        BitmapPool bitmapPool = sketch.getConfiguration().getBitmapPool();

        Bitmap reflectionBitmap = bitmapPool.getOrMake(bitmap.getWidth(), reflectionTop + reflectionHeight, config);

        // 在上半部分绘制原图
        Canvas canvas = new Canvas(reflectionBitmap);
        canvas.drawBitmap(bitmap, 0, 0, null);

        // 在下半部分绘制倒影
        Matrix matrix = new Matrix();
        matrix.postScale(1, -1);
        matrix.postTranslate(0, srcHeight + reflectionTop);
        canvas.drawBitmap(bitmap, matrix, null);

        // 在倒影部分绘制半透明遮罩，让倒影部分产生半透明渐变的效果
        Paint paint = new Paint();
        paint.setShader(new LinearGradient(0, reflectionTop, 0, reflectionBitmap.getHeight(), 0x70ffffff, 0x00ffffff, TileMode.CLAMP));
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawRect(0, reflectionTop, reflectionBitmap.getWidth(), reflectionBitmap.getHeight(), paint);

        return reflectionBitmap;
    }

    public float getReflectionScale() {
        return reflectionScale;
    }

    public int getReflectionSpacing() {
        return reflectionSpacing;
    }

    @NonNull
    @Override
    public String onToString() {
        return String.format(Locale.US, "%s(scale=%s,spacing=%d)", "ReflectionImageProcessor", reflectionScale, reflectionSpacing);
    }

    @Override
    public String onGetKey() {
        return String.format(Locale.US, "%s(scale=%s,spacing=%d)", "Reflection", reflectionScale, reflectionSpacing);
    }
}
