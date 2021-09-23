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

package net.mikaelzero.mojito.view.sketch.core.drawable;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;

import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.decode.ResizeCalculator;
import net.mikaelzero.mojito.view.sketch.core.request.ImageFrom;
import net.mikaelzero.mojito.view.sketch.core.request.ShapeSize;
import net.mikaelzero.mojito.view.sketch.core.shaper.ImageShaper;

/**
 * 可以改变 {@link BitmapDrawable} 的形状和尺寸
 */
public class SketchShapeBitmapDrawable extends Drawable implements SketchRefDrawable {
    private static final int DEFAULT_PAINT_FLAGS = Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG;

    @NonNull
    private BitmapDrawable bitmapDrawable;
    @Nullable
    private ShapeSize shapeSize;
    @Nullable
    private ImageShaper shaper;

    @NonNull
    private Paint paint;
    @NonNull
    private Rect srcRect;
    @Nullable
    private BitmapShader bitmapShader;

    @Nullable
    private SketchRefDrawable refDrawable;
    @Nullable
    private SketchDrawable sketchDrawable;

    @NonNull
    private ResizeCalculator resizeCalculator;

    public SketchShapeBitmapDrawable(@NonNull Context context, @NonNull BitmapDrawable bitmapDrawable, @Nullable ShapeSize shapeSize, @Nullable ImageShaper shaper) {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException(bitmap == null ? "bitmap is null" : "bitmap recycled");
        }

        if (shapeSize == null && shaper == null) {
            throw new IllegalArgumentException("shapeSize is null and shapeImage is null");
        }

        this.bitmapDrawable = bitmapDrawable;
        this.paint = new Paint(DEFAULT_PAINT_FLAGS);
        this.srcRect = new Rect();
        this.resizeCalculator = Sketch.with(context).getConfiguration().getResizeCalculator();

        setShapeSize(shapeSize);
        setShaper(shaper);

        if (bitmapDrawable instanceof SketchRefDrawable) {
            this.refDrawable = (SketchRefDrawable) bitmapDrawable;
        }

        if (bitmapDrawable instanceof SketchDrawable) {
            this.sketchDrawable = (SketchDrawable) bitmapDrawable;
        }
    }

    @SuppressWarnings("unused")
    public SketchShapeBitmapDrawable(Context context, BitmapDrawable bitmapDrawable, ShapeSize shapeSize) {
        this(context, bitmapDrawable, shapeSize, null);
    }

    @SuppressWarnings("unused")
    public SketchShapeBitmapDrawable(Context context, BitmapDrawable bitmapDrawable, ImageShaper shaper) {
        this(context, bitmapDrawable, null, shaper);
    }

    @Override
    public void draw(@SuppressWarnings("NullableProblems") Canvas canvas) {
        Rect bounds = getBounds();
        Bitmap bitmap = bitmapDrawable.getBitmap();
        if (bounds.isEmpty() || bitmap == null || bitmap.isRecycled()) {
            return;
        }

        if (shaper != null && bitmapShader != null) {
            shaper.draw(canvas, paint, bounds);
        } else {
            canvas.drawBitmap(bitmap, !srcRect.isEmpty() ? srcRect : null, bounds, paint);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return shapeSize != null ? shapeSize.getWidth() : bitmapDrawable.getIntrinsicWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return shapeSize != null ? shapeSize.getHeight() : bitmapDrawable.getIntrinsicHeight();
    }

    @Override
    public int getAlpha() {
        return paint.getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {
        final int oldAlpha = paint.getAlpha();
        if (alpha != oldAlpha) {
            paint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public ColorFilter getColorFilter() {
        return paint.getColorFilter();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
        invalidateSelf();
    }

    @Override
    public void setDither(boolean dither) {
        paint.setDither(dither);
        invalidateSelf();
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        paint.setFilterBitmap(filter);
        invalidateSelf();
    }

    @Override
    public int getOpacity() {
        Bitmap bitmap = bitmapDrawable.getBitmap();
        return (bitmap.hasAlpha() || paint.getAlpha() < 255) ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);

        int boundsWidth = bounds.width();
        int boundsHeight = bounds.height();
        int bitmapWidth = bitmapDrawable.getBitmap().getWidth();
        int bitmapHeight = bitmapDrawable.getBitmap().getHeight();

        if (boundsWidth == 0 || boundsHeight == 0 || bitmapWidth == 0 || bitmapHeight == 0) {
            srcRect.setEmpty();
        } else if ((float) bitmapWidth / (float) bitmapHeight == (float) boundsWidth / (float) boundsHeight) {
            srcRect.set(0, 0, bitmapWidth, bitmapHeight);
        } else {
            ImageView.ScaleType scaleType = shapeSize != null ? shapeSize.getScaleType() : ImageView.ScaleType.FIT_CENTER;
            ResizeCalculator.Mapping mapping = resizeCalculator.calculator(bitmapWidth, bitmapHeight, boundsWidth, boundsHeight, scaleType, true);
            srcRect.set(mapping.srcRect);
        }

        if (shaper != null && bitmapShader != null) {
            float widthScale = (float) boundsWidth / bitmapWidth;
            float heightScale = (float) boundsHeight / bitmapHeight;

            // 缩放图片充满bounds
            Matrix shaderMatrix = new Matrix();
            float scale = Math.max(widthScale, heightScale);
            shaderMatrix.postScale(scale, scale);

            // 显示图片中间部分
            if (!srcRect.isEmpty()) {
                shaderMatrix.postTranslate(-srcRect.left * scale, -srcRect.top * scale);
            }

            shaper.onUpdateShaderMatrix(shaderMatrix, bounds, bitmapWidth, bitmapHeight, shapeSize, srcRect);
            bitmapShader.setLocalMatrix(shaderMatrix);
            paint.setShader(bitmapShader);
        }
    }

    @NonNull
    public BitmapDrawable getBitmapDrawable() {
        return bitmapDrawable;
    }

    @Nullable
    public ShapeSize getShapeSize() {
        return shapeSize;
    }

    public void setShapeSize(ShapeSize shapeSize) {
        this.shapeSize = shapeSize;
        invalidateSelf();
    }

    @Nullable
    public ImageShaper getShaper() {
        return shaper;
    }

    public void setShaper(@Nullable ImageShaper shaper) {
        this.shaper = shaper;

        if (this.shaper != null) {
            if (bitmapShader == null) {
                bitmapShader = new BitmapShader(bitmapDrawable.getBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
                paint.setShader(bitmapShader);
            }
        } else {
            if (bitmapShader != null) {
                bitmapShader = null;
                paint.setShader(null);
            }
        }

        invalidateSelf();
    }

    @Nullable
    @Override
    public String getKey() {
        return sketchDrawable != null ? sketchDrawable.getKey() : null;
    }

    @Nullable
    @Override
    public String getUri() {
        return sketchDrawable != null ? sketchDrawable.getUri() : null;
    }

    @Override
    public int getOriginWidth() {
        return sketchDrawable != null ? sketchDrawable.getOriginWidth() : 0;
    }

    @Override
    public int getOriginHeight() {
        return sketchDrawable != null ? sketchDrawable.getOriginHeight() : 0;
    }

    @Nullable
    @Override
    public String getMimeType() {
        return sketchDrawable != null ? sketchDrawable.getMimeType() : null;
    }

    @Override
    public int getExifOrientation() {
        return sketchDrawable != null ? sketchDrawable.getExifOrientation() : ExifInterface.ORIENTATION_UNDEFINED;
    }

    @Override
    public int getByteCount() {
        return sketchDrawable != null ? sketchDrawable.getByteCount() : 0;
    }

    @Nullable
    @Override
    public Bitmap.Config getBitmapConfig() {
        return sketchDrawable != null ? sketchDrawable.getBitmapConfig() : null;
    }

    @Nullable
    @Override
    public ImageFrom getImageFrom() {
        return sketchDrawable != null ? sketchDrawable.getImageFrom() : null;
    }

    @Nullable
    @Override
    public String getInfo() {
        return sketchDrawable != null ? sketchDrawable.getInfo() : null;
    }

    @Override
    public void setIsDisplayed(@NonNull String callingStation, boolean displayed) {
        if (refDrawable != null) {
            refDrawable.setIsDisplayed(callingStation, displayed);
        }
    }

    @Override
    public void setIsWaitingUse(@NonNull String callingStation, boolean waitingUse) {
        if (refDrawable != null) {
            refDrawable.setIsWaitingUse(callingStation, waitingUse);
        }
    }

    @Override
    public boolean isRecycled() {
        return refDrawable == null || refDrawable.isRecycled();
    }
}