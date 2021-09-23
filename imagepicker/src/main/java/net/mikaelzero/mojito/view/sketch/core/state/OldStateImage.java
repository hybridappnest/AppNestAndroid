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
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import net.mikaelzero.mojito.view.sketch.core.SketchView;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchLoadingDrawable;
import net.mikaelzero.mojito.view.sketch.core.drawable.SketchShapeBitmapDrawable;
import net.mikaelzero.mojito.view.sketch.core.request.DisplayOptions;
import net.mikaelzero.mojito.view.sketch.core.request.ShapeSize;
import net.mikaelzero.mojito.view.sketch.core.shaper.ImageShaper;
import net.mikaelzero.mojito.view.sketch.core.util.SketchUtils;


/**
 * 使用当前 {@link ImageView} 正在显示的图片作为状态图片
 */
public class OldStateImage implements StateImage {
    @Nullable
    private StateImage whenEmptyImage;

    public OldStateImage(@Nullable StateImage whenEmptyImage) {
        this.whenEmptyImage = whenEmptyImage;
    }

    public OldStateImage() {
    }

    @Nullable
    @Override
    public Drawable getDrawable(@NonNull Context context, @NonNull SketchView sketchView, @NonNull DisplayOptions displayOptions) {
        Drawable drawable = SketchUtils.getLastDrawable(sketchView.getDrawable());

        if (drawable instanceof SketchLoadingDrawable) {
            drawable = ((SketchLoadingDrawable) drawable).getWrappedDrawable();
        }

        if (drawable != null) {
            ShapeSize shapeSize = displayOptions.getShapeSize();
            ImageShaper imageShaper = displayOptions.getShaper();
            if (shapeSize != null || imageShaper != null) {
                if (drawable instanceof SketchShapeBitmapDrawable) {
                    drawable = new SketchShapeBitmapDrawable(context, ((SketchShapeBitmapDrawable) drawable).getBitmapDrawable(), shapeSize, imageShaper);
                } else if (drawable instanceof BitmapDrawable) {
                    drawable = new SketchShapeBitmapDrawable(context, (BitmapDrawable) drawable, shapeSize, imageShaper);
                }
            }
        }

        if (drawable == null && whenEmptyImage != null) {
            drawable = whenEmptyImage.getDrawable(context, sketchView, displayOptions);
        }

        return drawable;
    }
}
