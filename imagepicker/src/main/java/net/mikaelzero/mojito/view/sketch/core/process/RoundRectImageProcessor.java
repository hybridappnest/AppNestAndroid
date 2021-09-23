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
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPool;
import net.mikaelzero.mojito.view.sketch.core.decode.ResizeCalculator;
import net.mikaelzero.mojito.view.sketch.core.request.Resize;

import java.util.Arrays;


/**
 * 圆角矩形图片处理器
 */
@SuppressWarnings({"WeakerAccess"})
public class RoundRectImageProcessor extends WrappedImageProcessor {

    @NonNull
    private float[] cornerRadius;

    /**
     * 创建一个圆角矩形图片处理器
     *
     * @param topLeftRadius         左上角圆角角度
     * @param topRightRadius        右上角圆角角度
     * @param bottomLeftRadius      左下角圆角角度
     * @param bottomRightRadius     右下角圆角角度
     * @param wrappedImageProcessor 嵌套一个图片处理器
     */
    public RoundRectImageProcessor(float topLeftRadius, float topRightRadius, float bottomLeftRadius,
                                   float bottomRightRadius, @Nullable WrappedImageProcessor wrappedImageProcessor) {
        super(wrappedImageProcessor);
        cornerRadius = new float[]{topLeftRadius, topLeftRadius,
                topRightRadius, topRightRadius,
                bottomLeftRadius, bottomLeftRadius,
                bottomRightRadius, bottomRightRadius};
    }

    /**
     * 创建一个圆角矩形图片处理器
     *
     * @param topLeftRadius     左上角圆角角度
     * @param topRightRadius    右上角圆角角度
     * @param bottomLeftRadius  左下角圆角角度
     * @param bottomRightRadius 右下角圆角角度
     */
    public RoundRectImageProcessor(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        this(topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius, null);
    }

    /**
     * 创建一个圆角矩形图片处理器
     *
     * @param cornerRadius          圆角角度
     * @param wrappedImageProcessor 嵌套一个图片处理器
     */
    public RoundRectImageProcessor(float cornerRadius, @Nullable WrappedImageProcessor wrappedImageProcessor) {
        this(cornerRadius, cornerRadius, cornerRadius, cornerRadius, wrappedImageProcessor);
    }

    /**
     * 创建一个圆角矩形图片处理器
     *
     * @param cornerRadius 圆角角度
     */
    public RoundRectImageProcessor(float cornerRadius) {
        this(cornerRadius, cornerRadius, cornerRadius, cornerRadius, null);
    }

    @Override
    protected boolean isInterceptResize() {
        return true;
    }

    @NonNull
    @Override
    public Bitmap onProcess(@NonNull Sketch sketch, @NonNull Bitmap bitmap, @Nullable Resize resize, boolean lowQualityImage) {
        if (bitmap.isRecycled()) {
            return bitmap;
        }

        ResizeCalculator resizeCalculator = sketch.getConfiguration().getResizeCalculator();
        ResizeCalculator.Mapping mapping = resizeCalculator.calculator(bitmap.getWidth(), bitmap.getHeight(),
                resize != null ? resize.getWidth() : bitmap.getWidth(),
                resize != null ? resize.getHeight() : bitmap.getHeight(),
                resize != null ? resize.getScaleType() : null,
                resize != null && resize.getMode() == Resize.Mode.EXACTLY_SAME);

        Bitmap.Config config = lowQualityImage ? Bitmap.Config.ARGB_4444 : Bitmap.Config.ARGB_8888;
        BitmapPool bitmapPool = sketch.getConfiguration().getBitmapPool();

        Bitmap roundRectBitmap = bitmapPool.getOrMake(mapping.imageWidth, mapping.imageHeight, config);

        Canvas canvas = new Canvas(roundRectBitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0xFFFF0000);

        // 绘制圆角的罩子
        Path path = new Path();
        path.addRoundRect(new RectF(0, 0, mapping.imageWidth, mapping.imageHeight), cornerRadius, Path.Direction.CW);
        canvas.drawPath(path, paint);

        // 应用遮罩模式并绘制图片
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, mapping.srcRect, mapping.destRect, paint);

        return roundRectBitmap;
    }

    @NonNull
    public float[] getCornerRadius() {
        return cornerRadius;
    }

    @NonNull
    @Override
    public String onToString() {
        return String.format("%s(%s)", "RoundRectImageProcessor", Arrays.toString(cornerRadius));
    }

    @Override
    public String onGetKey() {
        return String.format("%s(%s)", "RoundRect", Arrays.toString(cornerRadius));
    }
}
