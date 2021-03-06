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

package net.mikaelzero.mojito.view.sketch.core.state;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.Configuration;
import net.mikaelzero.mojito.view.sketch.core.ErrorTracker;
import net.mikaelzero.mojito.view.sketch.core.Sketch;
import net.mikaelzero.mojito.view.sketch.core.SketchView;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPool;
import net.mikaelzero.mojito.view.sketch.core.cache.BitmapPoolUtils;
import net.mikaelzero.mojito.view.sketch.core.cache.MemoryCache;
import net.mikaelzero.mojito.view.sketch.core.decode.ImageAttrs;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchBitmapDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchRefBitmap;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchShapeBitmapDrawable;
import net.mikaelzero.mojito.view.sketch.core.process.ImageProcessor;
import net.mikaelzero.mojito.view.sketch.core.request.DisplayOptions;
import net.mikaelzero.mojito.view.sketch.core.request.ImageFrom;
import net.mikaelzero.mojito.view.sketch.core.request.Resize;
import net.mikaelzero.mojito.view.sketch.core.request.ShapeSize;
import net.mikaelzero.mojito.view.sketch.core.shaper.ImageShaper;
import net.mikaelzero.mojito.view.sketch.core.uri.DrawableUriModel;
import net.mikaelzero.mojito.view.sketch.core.uri.UriModel;
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;

/**
 * ???????????? Options ???????????? {@link ImageProcessor} ??? {@link Resize} ?????????????????????????????? {@link ShapeSize} ??? {@link ImageShaper}
 */
// TODO: 2017/10/30 ???????????? MakerDrawableStateImage ?????? DrawableStateImage ???????????? drawable
public class MakerStateImage implements StateImage {
    private int resId;

    public MakerStateImage(int resId) {
        this.resId = resId;
    }

    public int getResId() {
        return resId;
    }

    @Nullable
    @Override
    public Drawable getDrawable(@NonNull Context context, @NonNull SketchView sketchView, @NonNull DisplayOptions displayOptions) {
        Drawable drawable = makeDrawable(Sketch.with(context), displayOptions);

        ShapeSize shapeSize = displayOptions.getShapeSize();
        ImageShaper imageShaper = displayOptions.getShaper();
        if ((shapeSize != null || imageShaper != null) && drawable instanceof BitmapDrawable) {
            drawable = new SketchShapeBitmapDrawable(context, (BitmapDrawable) drawable, shapeSize, imageShaper);
        }

        return drawable;
    }

    @Nullable
    private Drawable makeDrawable(@NonNull Sketch sketch, @NonNull DisplayOptions options) {
        Configuration configuration = sketch.getConfiguration();

        ImageProcessor processor = options.getProcessor();
        Resize resize = options.getResize();
        BitmapPool bitmapPool = configuration.getBitmapPool();

        // ????????????????????????????????????????????????
        if (processor == null && resize == null) {
            return configuration.getContext().getResources().getDrawable(resId);
        }

        // ?????????????????????
        String imageUri = DrawableUriModel.makeUri(resId);
        UriModel uriModel = UriModel.match(sketch, imageUri);
        String memoryCacheKey = null;
        if (uriModel != null) {
            memoryCacheKey = SketchUtils.makeRequestKey(imageUri, uriModel, options.makeStateImageKey());
        }
        MemoryCache memoryCache = configuration.getMemoryCache();
        SketchRefBitmap cachedRefBitmap = null;
        if (memoryCacheKey != null) {
            cachedRefBitmap = memoryCache.get(memoryCacheKey);
        }
        if (cachedRefBitmap != null) {
            if (!cachedRefBitmap.isRecycled()) {
                return new SketchBitmapDrawable(cachedRefBitmap, ImageFrom.MEMORY_CACHE);
            } else {
                memoryCache.remove(memoryCacheKey);
            }
        }

        // ????????????
        Bitmap bitmap;
        boolean allowRecycle = false;
        boolean tempLowQualityImage = configuration.isLowQualityImageEnabled() || options.isLowQualityImage();
        //noinspection deprecation
        Drawable drawable = configuration.getContext().getResources().getDrawable(resId);
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = SketchUtils.drawableToBitmap(drawable, tempLowQualityImage, bitmapPool);
            allowRecycle = true;
        }
        if (bitmap == null || bitmap.isRecycled()) {
            return null;
        }

        // ????????????
        //noinspection ConstantConditions
        if (processor == null && resize != null) {
            processor = sketch.getConfiguration().getResizeProcessor();
        }
        Bitmap newBitmap;
        try {
            newBitmap = processor.process(sketch, bitmap, resize, tempLowQualityImage);
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            ErrorTracker errorTracker = sketch.getConfiguration().getErrorTracker();
            errorTracker.onProcessImageError(e, DrawableUriModel.makeUri(resId), processor);
            if (allowRecycle) {
                BitmapPoolUtils.freeBitmapToPool(bitmap, bitmapPool);
            }
            return null;
        }

        // bitmap????????????????????????????????????????????????????????????????????????????????????
        if (newBitmap != bitmap) {
            if (allowRecycle) {
                BitmapPoolUtils.freeBitmapToPool(bitmap, bitmapPool);
            }

            // ??????????????????????????????????????????????????????????????????null??????
            if (newBitmap.isRecycled()) {
                return null;
            }

            bitmap = newBitmap;
            allowRecycle = true;
        }

        // ??????????????????????????????????????????????????????????????????????????????res????????????BitmapDrawable??????????????????
        if (allowRecycle) {
            BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
            boundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(configuration.getContext().getResources(), resId, boundsOptions);

            String uri = DrawableUriModel.makeUri(resId);
            ImageAttrs imageAttrs = new ImageAttrs(boundsOptions.outMimeType, boundsOptions.outWidth, boundsOptions.outHeight, 0);

            SketchRefBitmap newRefBitmap = new SketchRefBitmap(bitmap, memoryCacheKey, uri, imageAttrs, bitmapPool);
            memoryCache.put(memoryCacheKey, newRefBitmap);
            return new SketchBitmapDrawable(newRefBitmap, ImageFrom.LOCAL);
        } else {
            return drawable;
        }
    }
}